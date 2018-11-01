package cache;

import controllers.UserController;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it. Fix
public class UserCache {

    // List of users
    private ArrayList<User> users;

    // The time the cache should live
    private long ttl;

    // Sets when the cache has been created
    private long created;

    public UserCache(){
        this.ttl= Config.getUser_TTL();
    }


    public ArrayList<User> getUsers (Boolean forceUpdate) {
        if (forceUpdate
                || ((this.created + this.ttl) >= (System.currentTimeMillis() / 1000L))
                || this.users.isEmpty()) {

            // Get users from controller, since we wish to update.
            ArrayList<User> users = UserController.getUsers();

            // Set users for the instance and set created timestamp
            this.users = users;
            this.created = System.currentTimeMillis() / 1000L;
        }

        // Return the documents
        return this.users;
    }

}
