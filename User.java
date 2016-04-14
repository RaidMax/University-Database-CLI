/**
 * User class allowing for authentication and ID retrieval
 * @author Michael Snyder
 */

public class User {
  private String username;
  private String password;
  private UserPermission permissions;

  /**
   * Constructor for USER class
   * @param username User's username
   * @param password User's password
   * @param userPosition Position to set the user as
   * @throws UserException If the user name or password is bad
   */
  public User(String username, String password, UserPermission.Position userPosition) throws UserException {
    if (username != null && username.length() > 0 && password != null && password.length() > 0) {
      this.username = username;
      this.password = password;
      this.permissions = new UserPermission(userPosition);
    }
    else
      throw new UserException("Invalid user information");
  }

  /**
   * Overloaded constructor used by the authentication in @see Database
   * @param username User's username
   * @param password User's password
   * @throws UserException If the user or password is bad
   */
  public User(String username, String password) throws UserException {
	  this(username, password, UserPermission.Position.None);
  }

  /**
   * Get the user's permissions
   * @return User's permissions
   */
  public UserPermission getPermissions() {
    return permissions;
  }

  /**
   * Overridden method to authenticate a user in @see Database
   * @param comp User to compare to
   * @return True if equal, false otherwise
   */
  public boolean equals(User comp) {
      return (comp.username.equals(this.username) && comp.password.equals(this.password));
  }

  /**
   * Get the user's name
   * @return User's name
   */
  public String getName() {
    return this.username;
  }

  public String toString() {
    return String.format("[%s] \"%s\"", permissions.getPosition(), username);
  }

  /**
   * Get the user's ID (bit shitfted hash)
   * @return User's ID
   */
  public Integer getID() {
    // this is not really a good method because of collisions and truncation
    // but it works fine for this project's purpose.
    return username.hashCode() >> 8;
  }
}

