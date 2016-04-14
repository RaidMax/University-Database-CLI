import java.util.Set;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * Class to keep track of the permissions for database users
 * A lot of this class is project specific
 * @author Michael Snyder
 */

public class UserPermission {
  private Position userPosition;
  private HashMap<String, EnumSet<UserInterface.Command>> availableCommands;

  public enum Position {
    Staff,
    Student,
    None,
  }

  /**
   * Constructor to load the user permissions based on the user's position 
   * @param userPosition Position given to the user
   */
  public UserPermission(Position userPosition)
  {
    this.userPosition = userPosition;
    availableCommands = new HashMap<String, EnumSet<UserInterface.Command>>();

    // Below is project specific
    switch(userPosition) {
      case Staff:
        availableCommands.put("course", EnumSet.of(UserInterface.Command.Retrieve, UserInterface.Command.Create, UserInterface.Command.Update, UserInterface.Command.Delete));
        availableCommands.put("section", EnumSet.of(UserInterface.Command.Retrieve, UserInterface.Command.Create, UserInterface.Command.Update, UserInterface.Command.Delete));
        availableCommands.put("department", EnumSet.of(UserInterface.Command.Retrieve));
        break;
      case Student:
        availableCommands.put("takes", EnumSet.of(UserInterface.Command.Register, UserInterface.Command.Retrieve, UserInterface.Command.Drop));
        availableCommands.put("transcript", EnumSet.of(UserInterface.Command.Retrieve));
        break;
    }
  }

  /**
   * Get the commands available to a user based on the database table
   * @param table Get the user's permissions for this table
   * @return EnumSet of Commands
   */
  public EnumSet<UserInterface.Command> getAvailableCommands(String table) {
    if (availableCommands.get(table) != null)
      return availableCommands.get(table);

    return EnumSet.of(UserInterface.Command.None);
  }

  /**
   * Set of table(s) this user has access to
   * @return Set of strings of available tables
   */
  public Set<String> getAvailableTables() {
    return availableCommands.keySet();
  }

  /**
   * Get the user's position/level
   * @return User's position
   */
  public Position getPosition() {
    return userPosition;
  }
}
