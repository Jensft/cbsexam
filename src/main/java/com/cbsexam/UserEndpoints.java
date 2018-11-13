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
  // Implementing an instance of userCache
  private static UserCache userCache = new UserCache();


  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON FIX
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down? FIX
    if (user != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not find the user with id: " +idUser).build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users with cache
    //ArrayList<User> users = UserController.getUsers();
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON FIX
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)

  public Response loginUser(String body) {
    User loginUser = new Gson().fromJson(body, User.class);
    String token = UserController.login(loginUser);

      if (token!=null) {
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Email and password are correct. You are now logged in with token: \n " + token ).build();
      } else {
        return Response.status(400).entity("Incorrect password or email - Try again").build();
      }
    }




  // TODO: Make the system able to deleteUser users FIX
  @DELETE
  @Path("/deleteUser/{userId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(@PathParam("userId") int id) {
    Boolean delete = UserController.deleteUser(id);

    userCache.getUsers(true);

    if(delete) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User " + id +" has been deleted ").build();
    }else {
      return Response.status(400).entity("User has not been found").build();
    }
  }

  // TODO: Make the system able to updateUser users FIX
  @POST
  @Path("/updateUser/{userId}")
  public Response updateUser(@PathParam("userId") int userId,String body) {
    User user = new Gson().fromJson(body,User.class);

    Boolean update =UserController.updateUser(user, userId);

    userCache.getUsers(true);


    // Return a response with status 200 and JSON as type
    if(update) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User  has been updated: " + body).build();
    }else {
      return Response.status(400).entity("User has not been updated").build();
    }
  }
}
