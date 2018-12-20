package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

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
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getLong("created_at"));

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
                        rs.getLong("created_at"));

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
    Log.writeLog(UserController.class.getName(), user, "Creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // The password is hashed with salt through the sha256 method
    // TODO: Hash the user password before saving it. FIX
    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    + Hashing.shaWithSalt(user.getPassword())
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static String login (User loginUser) {
    Log.writeLog(UserController.class.getName(), loginUser, "Logging in", 0);

    //Creates an instance of the user cache
    UserCache userCache = new UserCache();
    ArrayList<User> users = userCache.getUsers(false);

    // Timestamp for the token
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    //The for loop iterates the user cache through.
    for(User user: users) {
      //If it finds the matching email and password it will proceed to the try catch.
      if (user.getEmail().equals(loginUser.getEmail()) && user.getPassword().equals(Hashing.shaWithSalt(loginUser.getPassword()))) {

        try {
          // creating an instance of "Algorithm"
          Algorithm algorithm = Algorithm.HMAC256("JWT_TOKEN_KEY");

          //Sets a timestamp and a user-id for the token
          String token = JWT.create().withIssuer("auth0").withClaim("Test", timestamp).withClaim("userid", user.getId()).sign(algorithm);

          user.setToken(token);
          return token;

        } catch (JWTCreationException exception) {

          //Invalid signature/claims
          exception.getMessage();
        }
      }
    }
    return null;
  }

// The implemented deleteUser method

  public static boolean deleteUser(int id) {
    Log.writeLog(UserController.class.getName(), id, "Deleting user", 0);

    // Checks the connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
    // Creating a user object
    User user = UserController.getUser(id);

    // if the user exist the if statement will return true or otherwise false.
    if (user != null) {
      dbCon.deleteUpdate("DELETE FROM user WHERE id=" + id);
      return true;
    } else {
      return false;
    }
  }

  // implementing updateUser method
  public static boolean updateUser(User user, int userid) {
    Log.writeLog(UserController.class.getName(), user, "Updating user", 0);

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    if (user != null) {

      dbCon.deleteUpdate("UPDATE user SET first_name ='"+ user.getFirstname() +
              "', last_name ='" + user.getLastname() +
              "', password='" + Hashing.shaWithSalt(user.getPassword()) +
              "', email='" + user.getEmail() +
              "'Where id=" + userid);
      return true;
    } else {
      return false;
    }
  }

  public static DecodedJWT verifier (String user) {

    Log.writeLog(UserController.class.getName(), user, "Verifying token", 0);
    // Enables the method to verify the token, the string is the inserted token.
    String token = user;

    try {
      // Verifying the token
      Algorithm algorithm = Algorithm.HMAC256("JWT_TOKEN_KEY");
      JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
      DecodedJWT jwt = verifier.verify(token);

      return jwt;
    } catch (JWTVerificationException exception){

      //Incorrect claims
      exception.getMessage();
    }

    return null;
  }

}