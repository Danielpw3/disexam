package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import model.User;
import utils.Hashing;
import utils.Log;



public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  private static Hashing hash = new Hashing();  //Hashing objekt -D

  public static User getUser(int id) {

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
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

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
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

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
            + ", "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static String logingUser (User user) {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //TODO readsalt first
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
                    System.out.println(e.getMessage());
                  } finally {
                    return token;
                  }
                }
      } else {
        System.out.println("no user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    return "";
  }
}
