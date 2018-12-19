package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.interfaces.DecodedJWT;
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
    // If something is incorrect, it will return the response status 400
    if (user != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not find the user with id: " + idUser).build();
    }
  }

  /**
   * @return Responses
   */
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
      // The Cache will be updated
      userCache.getUsers(true);
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system. FIX
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)

  public Response loginUser(String body) {
    // The method reads the details from the json body
    User loginUser = new Gson().fromJson(body, User.class);

    // login method returns a token if the login is completed and sets it = token
    // The method uses the body from loginUser
    String token = UserController.login(loginUser);

    //If the token is incorrect returns 200 and 400 if it's correct.
    if (token != null) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Email and password are correct. You are now logged in with token: \n " + token).build();
    } else {
      return Response.status(400).entity("Incorrect password or email - Try again").build();
    }
  }


  // TODO: Make the system able to deleteUser users FIX
  @DELETE
  @Path("/deleteUser/{userId}")

  public Response deleteUser(@PathParam("userId") int id, String body) {
    // Runs the method to check if token/body matches the logged in users token
    DecodedJWT token = UserController.verifier(body);
    if (token.getClaim("etest").asInt() == id) {
      // Uses the user id from the token
      Boolean delete = UserController.deleteUser(token.getClaim("etest").asInt());


      if (delete) {
        userCache.getUsers(true);
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User " + id + " has been deleted ").build();

      }
      return Response.status(400).entity("User has not been found").build();
    } else {
      return Response.status(400).entity("You cannot delete other users").build();
    }
  }


  // TODO: Make the system able to updateUser users FIX
  @POST
  @Path("/updateUser/{userId}/{token}")
  public Response updateUser(@PathParam("userId") int userId, @PathParam("token") String token, String body) {
    User user = new Gson().fromJson(body, User.class);

    DecodedJWT jwt = UserController.verifier(token);

    if (jwt.getClaim("etest").asInt() == userId) {

      Boolean update = UserController.updateUser(user, jwt.getClaim("etest").asInt());

      userCache.getUsers(true);


      // Return a response with status 200 and JSON as type
      if (update) {
        return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User  has been updated: " + body).build();
      } else {
        return Response.status(400).entity("User has not been updated").build();
      }
    } else {
      return Response.status(400).entity("You cannot update other users").build();
    }
  }
}

