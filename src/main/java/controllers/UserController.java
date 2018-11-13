package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import cache.UserCache;
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
                        rs.getString("email"));

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
                        rs.getString("email"));

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

    UserCache userCache = new UserCache();
    ArrayList<User> users = userCache.getUsers(false);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    for(User user: users) {
      if (user.getEmail().equals(loginUser.getEmail()) && user.getPassword().equals(Hashing.shaWithSalt(loginUser.getPassword()))) {

        try {
          Algorithm algorithm = Algorithm.HMAC256("JWT_TOKEN_KEY");
          //String token = JWT.create().withIssuer("auth0").sign(algorithm);
          String token = JWT.create().withClaim("Test",timestamp).sign(algorithm);

          return token;

        } catch (JWTCreationException exception) {
          exception.getMessage();
        }
      }
    }
    return null;
  }

// implementing deleteUser method

  public static boolean deleteUser(int id) {
    Log.writeLog(UserController.class.getName(), id, "Deleting user", 0);

    // Checks the connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }
    // Creating a user object
    User user = UserController.getUser(id);

    // if user exist the if statement will return true or otherwise it will return false.
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
}