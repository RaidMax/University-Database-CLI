import java.util.Set;
import java.util.EnumSet;
import java.util.List;
import java.util.HashMap;

/**
 * User Interface class to prompt the user to navigate the CLI 
 * This class is project specific
 * @author Michael Snyder
 */

public class UserInterface {
  private InputReader input;
  private Database rootDatabase;

  public enum Command {
    Retrieve,
    Create,
    Update,
    Delete,
    Drop,
    Register,
    Transcript,
    None,
  }

  public UserInterface(Database rootDatabase) {
    input = InputReader.getReader();
    this.rootDatabase = rootDatabase;
  }

  /**
   * Helper method to ensure what the user has entered is a valid selection
   * @param min Minimum selection value permissable
   * @param max Maximum selection value permissable
   * @param value Value submitted by user
   * @return True if valid selection, false otherwise
   */
  private boolean validateSelection(int min, int max, int value) {
      return value >= min && value <= max;
  }

  /**
   * Print out a semi-formatted table of database query values
   * @param header Message to print in table header
   * @param content Table content stuff
   */
  private void boxOutput(String header, Object[] content) {
    System.out.println("============================================");
    System.out.println(header);
    System.out.println("============================================");
    int choiceIndex = 1;
    for (Object S : content) {
      System.out.println(choiceIndex + "] " + S);
      choiceIndex++;
    }
    // added to inform the user how to return/exit
    System.out.println("0] --return--");
    System.out.println("============================================");
  }

  /**
   * Print a 'table' corresponding to an sql query
   * @param table List of query results in string form
   */
  private void printTable(List<String> table) {
    System.out.println("--------------------------------------------");
    for (String S : table)
      System.out.println(S);
    System.out.println("--------------------------------------------");
  }

  /**
   * Display the main menu for the user
   * Blocks the program until they are finished
   */
  public void mainMenu() {
	 if (rootDatabase.getCurrentUser() == null)
		 return; // No one has actually logged in

	Set<String> availableTables = rootDatabase.getCurrentUser().getPermissions().getAvailableTables();
    System.out.println("Hello " + rootDatabase.getCurrentUser() + ", you have successfully logged in!");

    // the max menu selection for the CLI = the # of tables that the user can access and/or modify
    int maxChoice = availableTables.size();

    int choice = -1;
    while ( choice != 0 ) {
      boxOutput("Available Tables", availableTables.toArray(new String[0]));
      choice = input.readUnsignedInt("Choose an option: ");
      while(!validateSelection(0, maxChoice, choice))
        choice = input.readUnsignedInt("Please choose a valid option: ");

      if (choice != 0) // user does not want to quit
        commandMenu(availableTables.toArray()[choice - 1].toString());
    }
  }

  /**
   * Read input and process command of a user once they have entered the table to use
   * @param table Table the user has selected from the main menu
   */
  private void commandMenu(String table) {
    EnumSet<Command> availableCommands = rootDatabase.getCurrentUser().getPermissions().getAvailableCommands(table);
    boxOutput(String.format("Available Commands for \"%s\"", table), availableCommands.toArray());

    int maxChoice = availableCommands.size();
    int choice = input.readUnsignedInt("Choose a command: ");
    while(!validateSelection(0, maxChoice, choice))
      choice = input.readUnsignedInt("Please choose a valid command: ");

    if (choice != 0) // user does not want to quit
      actionMenu((Command)availableCommands.toArray()[choice - 1], table);
  }

  /**
   * Method to prompt the user to perform an insert
   * @param currentTable Table that the user is currently working with
   */
  private void insertMenu(String currentTable) {
    if (currentTable.equals("course")) {
      String course_id  = input.readString("Enter course_id: ");
      String title      = input.readString("Enter title: ");
      String dept_name  = input.readString("Enter department_name: ");
      String credits    = input.readString("Enter credit hours: ");

      // the below method call can return false for multiple reasons
      // i've decided to not print out the potentional SQL error messages,
      // but they are accessible via rootDatabase.getLastError()
      if (rootDatabase.insertTuple(currentTable, course_id, title, dept_name, credits))
        System.out.println("Course successfully added!");
      else {
        System.out.println("Could could not be added. Please try again.");
      }
    }

    else if (currentTable.equals("section")) {
      String course_id    = input.readString("Enter course_id: ");
      String sec_id       = input.readString("Enter sec_id: ");
      String semester     = input.readString("Enter semester: ");
      String year         = input.readString("Enter year: ");
      String building     = input.readString("Enter building: ");
      String room_number  = input.readString("room_number: ");
      String time_slot_id = input.readString("time_slot_id: ");

      if (rootDatabase.insertTuple(currentTable, course_id, sec_id, semester, year, building, room_number, time_slot_id))
        System.out.println("Section successfully added!");
      else {
        System.out.println("Section could not be added. Please try again.");
      }
    }
  }

  /**
  * Method to prompt the user to perform an update
  * @param currentTable Table that the user is currently working with
  */
  private void updateMenu(String currentTable) {
    HashMap<String, String> newValues = new HashMap<String, String>();
    HashMap<String, String> primaryKeys = new HashMap<String, String>();
    // query will fail without the correct primary key specified
    System.out.println("Attributes denoted with '*' are REQUIRED.");

    if (currentTable.equals("course")) {
      String primaryKey = input.readString("Enter the course_id of the course to update*: ");
      String title      = input.readString("Enter new title: ");
      String dept_name  = input.readString("Enter new department_name: ");
      String credits    = input.readString("Enter new credit hours: ");
      
      // checking primary key (course_id)
      if (primaryKey.length() > 0)
        primaryKeys.put("course_id", primaryKey);

      // we only want to update fields that were entered
      if (title.length() > 0)
        newValues.put("title", title);
      if (dept_name.length() > 0)
        newValues.put("dept_name", dept_name);
      if (credits.length() > 0)
        newValues.put("credits", credits);

      if (rootDatabase.updateTable(currentTable, primaryKeys, newValues))
        System.out.println("Course successfully updated!");
      else {
        System.out.println("Could could not be updated. Please try again.");
      }
    }

    // foreign key constraints
    else if (currentTable.equals("section")) {
      String primaryKey   = input.readString("Enter the course_id of the section to update*: ");
      String sec_id       = input.readString("Enter the sec_id of the section to update*: ");
      String year         = input.readString("Enter new year: ");
      String building     = input.readString("Enter new building: ");
      String room_number  = input.readString("Enter new room_number: ");
      String time_slot_id = input.readString("Enter new time_slot_id: ");

      if (sec_id.length() > 0)
        primaryKeys.put("sec_id", sec_id);
      if (primaryKey.length() > 0)
        primaryKeys.put("course_id", primaryKey);

      if (building.length() > 0)
        newValues.put("building", building);
      if (room_number.length() > 0)
          newValues.put("room_number", room_number);
      if (time_slot_id.length() > 0)
          newValues.put("time_slot_id", time_slot_id);

      if (rootDatabase.updateTable(currentTable, primaryKeys, newValues))
        System.out.println("Section successfully updated!");
      else {
        System.out.println("Section could not be updated. Please try again.");
      }
    }
  }

  /**
  * Method to prompt the user to perform a delete
  * @param currentTable Table that the user is currently working with
  */
  public void deleteMenu(String currentTable) {
    HashMap<String, String> primaryKeys = new HashMap<String, String>();

    if (currentTable.equals("course")) {
      System.out.println("You will be unable to delete courses that are pre-reqs for other classes.");
      String course_id = input.readString("Enter the course_id to be deleted: ");

      if (course_id.length() > 0)
        primaryKeys.put("course_id", course_id);
      
      // make sure the user really wants to do this
      String response = input.readString(String.format("You would liked to remove \"%s\", is this correct? [y/n]: ", course_id));

      // this could be refactored into another method, but it's really not THAT long
      if (response.length() > 0 && (response.charAt(0) == 'Y' || response.charAt(0) == 'y')) {
        if (rootDatabase.deleteTuple(currentTable, primaryKeys))
          System.out.println("Course successfully deleted!");
        else
          System.out.println("Course could not be deleted. Please try again.");
      }
    }

    else if (currentTable.equals("section")) {
      System.out.println("You will be unable to delete sections that are already assigned for a class.");
      String course_id  = input.readString("Enter the course_id to be deleted: ");
      String sec_id     = input.readString("Enter the sec_id to be deleted: ");
      String semester   = input.readString("Enter the semester to be deleted: ");
      String year       = input.readString("Enter the year to be deleted: ");

      if (course_id.length() > 0)
        primaryKeys.put("course_id", course_id);
      if (sec_id.length() > 0)
        primaryKeys.put("sec_id", sec_id);
      if (semester.length() > 0)
        primaryKeys.put("semester", semester);
      if (year.length() > 0)
        primaryKeys.put("year", year);

      String response = input.readString(String.format("You would liked to remove \"%s\" - Section %s, is this correct? [y/n]: ", course_id, sec_id));

      if (response.length() > 0 && (response.charAt(0) == 'Y' || response.charAt(0) == 'y')) {
        if (rootDatabase.deleteTuple(currentTable, primaryKeys))
          System.out.println("Section successfully deleted!");
        else
          System.out.println("Section could not be deleted. Please try again.");
      }
    }
  }

  /**
  * Method to prompt the user to enroll for a new section
  */
  private void registerMenu() {
	// these are required
    String course_id  = input.readString("Enter course_id to register for: ");
    String sec_id     = input.readString("Enter sec_id to register for: ");

    if (rootDatabase.registerForSection(course_id, sec_id))
      System.out.println("Successfully registered for " + course_id);
    else
      System.out.println("There is no matching section available this semester or you are already enrolled.");
  }

  /**
  * Method to prompt the user to drop a section enrollment
  */
  private void dropMenu() {
    String course_id  = input.readString("Enter course_id to drop: ");
    String response   = input.readString(String.format("You would liked to drop \"%s\", is this correct? [y/n]: ", course_id));

    if (response.length() > 0 && (response.charAt(0) == 'Y' || response.charAt(0) == 'y')) {
      if (rootDatabase.dropSection(course_id))
        System.out.println("Section enrollment successfully dropped");
      else
        System.out.println("You do not appear to be enrolled in this class. Please try again.");
    }
  }
  /**
   * Once the user has selected a table and command, this method `executes` the command's function
   * @param userCMD Command the user entered
   * @param currentTable Table the user is currently working with
   */
  private void actionMenu(Command userCMD, String currentTable) {
    switch (userCMD) {
      case Retrieve:
        if (currentTable.equals("department"))
          printTable(rootDatabase.getDepartmentInfo());
        else if (currentTable.equals("course"))
          printTable(rootDatabase.getCourseInfo());
        else if (currentTable.equals("section"))
          printTable(rootDatabase.getSectionInfo());
        else if (currentTable.equals("takes"))
          printTable(rootDatabase.getCurrrentlyEnrolledSections());
        else if (currentTable.equals("transcript"))
          printTable(rootDatabase.getTranscript());
        break;
      case Create:
        insertMenu(currentTable);
        break;
      case Update:
        updateMenu(currentTable);
        break;
      case Delete:
        deleteMenu(currentTable);
        break;
      case Register:
        registerMenu();
        break;
      case Drop:
        dropMenu();
        break;
    }
    // we need to return to the command menu to prompt the user to enter another command or return
    commandMenu(currentTable);
  }
}
