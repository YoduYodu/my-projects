package models;

/* UsersServlet */
public class User {
    private String username, usersalt, password;

    /* Constructor */
    public User(String username, String usersalt, String password) {
        this.username = username;
        this.usersalt = usersalt;
        this.password = password;
    }

    /* Username getter */
    public String getUsername() {
        return username;
    }

    /* Usersalt getter */
    public String getUsersalt() {
        return usersalt;
    }

    /* Password getter */
    public String getPassword() {
        return password;
    }

    /* String representation */
    @Override
    public String toString() {
        return String.format("username: %s, usersalt: %s, password: %s", username, usersalt, password);
    }
}
