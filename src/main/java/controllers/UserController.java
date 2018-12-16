package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import model.User;
import utils.Hashing;
import utils.Log;



public class UserController {

  private static DatabaseController dbCon;
  private static UserCache userCache;

  public UserController() {
    dbCon = new DatabaseController();
    userCache = new UserCache();
  }

  private static Hashing hash = new Hashing();  //Hashing objekt -D

  public static User getUser(int id) {
    for (User user : userCache.getUsers(false)) {
      if (user.getId() == id) {
        // user found in cache
        return user;
      }
    }

    // User not found in cache
    User userFoundInDb = getUserFromDb(id);
    if (userFoundInDb != null) {
      // Add user found in database into cache
      userCache.addUser(userFoundInDb);
    }
    return userFoundInDb;
  }

  public static User getUserFromDb(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"), // remove komma
                rs.getString("password"),
                rs.getString("email"),
                rs.getInt("phone_number"),
                rs.getInt("salt"),
                rs.getTimestamp("created_at"));

        // return the create object
        return user;
      } else {
        Log.writeLog(UserController.class.getName(), null,"No user found", 0);

      }
    } catch (SQLException ex) {
      Log.writeLog(UserController.class.getName(), null,"getUser Message=" + ex.getMessage(), 1);
    }

    // Return null
    return user;
  }

  // get all users from cache
  public static ArrayList<User> getUsers() {
    return userCache.getUsers(false);
  }
  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsersFromDb() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getInt("phone_number"),
                rs.getInt("salt"),
                rs.getTimestamp("created_at"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      Log.writeLog(UserController.class.getName(), null,"getUsers Message=" + ex.getMessage(), 1);
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    if (user.getId() != 0) {
      // User already created -D
      return user;
    }

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user - now set in constructor
    //user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }


    // Insert the user in the DB
    // TODO: Hash the user password before saving it. - FIXED
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, phone_number, salt, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + hash.hashWithSalt(user.getPassword()) // hashed password -D
            + "', '"
            + user.getEmail()
            + "', "
            + user.getPhoneNumber()
            + ", "
            + user.getSalt()
            + ", '"
            + user.getCreatedTime()
            + "')");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
      // Add user to cache
      userCache.addUser(user);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static String loginUser(User user) {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB'
    //String sqlsalt = "SELECT salt FROM user where email = '" + user.getEmail() + "'";
    //dbCon.insert(sqlsalt);
    //ResultSet rs = dbCon.query(sqlsalt);

    // Build the query for DB'
    String sql = "SELECT * FROM user where email = '" + user.getEmail() + "'AND password = '" + hash.hashWithSalt(user.getPassword()) + "'";

    dbCon.insert(sql);

    ResultSet rs = dbCon.query(sql);
    User userlogin;
    String token = null;

    try {
      // Get first object, since we only have one
      if (rs.next()){
        userlogin =
                new User (
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getInt("phone_number"),
                        rs.getInt("salt"));
                if (userlogin != null) {
                  try {
                    Algorithm algorithm = Algorithm.HMAC256("secret");
                    token = JWT.create()
                            .withClaim("userid", userlogin.getId())
                            .withIssuer("auth0")
                            .sign(algorithm);
                  } catch (JWTCreationException e) {
                    //Invalid Signing configuration / Couldn't convert Claims.
                    Log.writeLog(UserController.class.getName(), null,"loginUser Message=" + e.getMessage(), 1);
                  } finally {
                    return token;
                  }
                }
      } else {
        Log.writeLog(UserController.class.getName(), null,"Login - No user found", 0);
      }
    } catch (SQLException ex) {
      Log.writeLog(UserController.class.getName(), null,"loginUser Message=" + ex.getMessage(), 1);
    }
    return "";
  }

  public static boolean deleteUser (String token) {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Comment
    DecodedJWT jwt = null;

    try {
      Algorithm algorithm = Algorithm.HMAC256("secret");
      JWTVerifier verifier = JWT.require(algorithm)
              .withIssuer("auth0").build(); //reusable verifier indstance
      jwt = verifier.verify(token);

    } catch (JWTCreationException e) {
      // invalid signature claims
    }
    int id = jwt.getClaim("userid").asInt();
    String sql = "DELETE FROM user  WHERE id = " + id;
    boolean result = dbCon.insert(sql) == 1;
    if (result) {
      // delete user from cache
      userCache.deleteUser(id);
    }
    return result;
  }

  public static boolean updateUser (User user, String token) {

    Hashing hashing = new Hashing();

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // comment
    DecodedJWT jwt = null;

    try {
      Algorithm algorithm = Algorithm.HMAC256("secret");
      JWTVerifier verifier = JWT.require(algorithm)
              .withIssuer("auth0")
              .build(); //Reusable verifier instance
      jwt = verifier.verify(token);
    } catch (JWTCreationException e) {
      // invalid signature claims
    }

    String sql = "UPDATE user SET first_name = '" + user.getFirstname()
            + "', last_name = '" + user.getLastname()
            + "', password = '" + hashing.hashWithSalt(user.getPassword())
            + "', email = '" + user.getEmail()
            + "' WHERE id = " + jwt.getClaim("userid").asInt();

    boolean result = dbCon.insert(sql) == 1;
    if (result) {
      // update user in cache
      userCache.updateUser(user);
    }
    return result;
  }
}
