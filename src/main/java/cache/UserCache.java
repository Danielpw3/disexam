package cache;

import controllers.UserController;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it. - Fixed
public class UserCache {

    private ArrayList<User> users;

    private long ttl;

    private long created;

    public UserCache () {
        this.ttl = Config.getUserTtl();
    }

    public ArrayList<User> getUsers(boolean forceUpdate) {

        if (forceUpdate
                ||((this.created + this.ttl) <= (System.currentTimeMillis()/1000L))
                || this.users == null) {
            // Read users from the database
            ArrayList <User> users = UserController.getUsersFromDb();

            this.users = users;
            this.created = System.currentTimeMillis()/1000L;

        }
        return this.users;
    }

    // Add user to cache
    public void addUser(User user) {
        this.users.add(user);
    }

    // Delete user from cache
    public void deleteUser(int id) {
        for (User user : this.users) {
            if (user.getId() == id) {
                this.users.remove(user);
                return;
            }
        }
    }

    // Update user in cache
    public void updateUser(User user) {
        int index = this.users.indexOf(user);
        // -1 is used to indicate not found
        if (index != -1) {
            // User found, update user information
            this.users.set(index, user);
        }
    }
}
