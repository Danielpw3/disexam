package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  private static UserController userController;


  public UserEndpoints () {
    userController = new UserController();
  }

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    User user = null;
    try {
      // Use the ID to get the user from the controller.
      user = UserController.getUser(idUser);

      // TODO: Add Encryption to JSON - FIXED
      // Convert the user object to json in order to return the object
      String json = new Gson().toJson(user);
      json = Encryption.encryptDecryptXOR(json); //add encryption to JSON -D

      // Return the user with the status OK code 200
      // If user is not found
      // TODO: What should happen if something breaks down? - Fixed
        return Response.status(user != null ? Response.Status.OK : Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } catch (Exception ex) {
      Log.writeLog(UserEndpoints.class.getName(), user,"getUser Message=" + ex.getMessage(), 1);
      return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).entity("").build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = UserController.getUsers();

    // TODO: Add Encryption to JSON - FIXED
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    json = Encryption.encryptDecryptXOR(json); //add encryption to JSON -D

    // Return the users with the status code 200
    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(json).build();
  }




  @POST
  @Path("/createUser")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = userController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(Response.Status.OK).entity("The user is created").build();
    } else {
      // Return a response with status 400 and JSON as type
      return Response.status(Response.Status.BAD_REQUEST).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system. -Fixed
  @POST
  @Path("/loginUser")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    // Read the json from body and transfer it to a user class
    User user = new Gson().fromJson(body, User.class);

    // Get the user back with added id and return to the user
    String token = userController.loginUser(user);

    // Return the data to the user
    if (token != "") {
      // Return the users with the status code 200
      return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(token).build();
    } else {
      // Return a response with status 400 and JSON as type
      return Response.status(Response.Status.BAD_REQUEST).entity("Could not login").build();
    }
  }

  // TODO: Make the system able to delete users - Fixed
  @DELETE
  @Path("/deleteUser")
  public Response deleteUser(String body) {

      User user = new Gson (). fromJson(body, User.class);

    // Return the data to the user
    if (userController.deleteUser(user.getToken())) {

      // Return a response with status 200 and JSON as type
      return Response.status(Response.Status.OK).entity("The user is deleted from the system").build();
    } else {
      // Return a response with status 400 and JSON as type
      return Response.status(Response.Status.BAD_REQUEST).entity("The user cannot be found in the system").build();
    }
  }

  // TODO: Make the system able to update users -Fixed
  @POST
  @Path("/updateUser")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(String body) {

    User user = new Gson().fromJson(body, User.class);

    // Return the data to the user
    if (userController.updateUser(user, user.getToken())) {
      // Return a response with status 200 and JSON as type
      return Response.status(Response.Status.OK).entity("The user is updated in the system").build();
    } else {
      // Return a response with status 400 and JSON as type
      return Response.status(Response.Status.BAD_REQUEST).entity("The user cannot be updatede").build();
    }
  }
}
