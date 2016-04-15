import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
/**
 * Database class to handle connections and queries
 * @author Michael Snyder
 */
public class Database {
  private Connection dbConnection;
  private Exception lastError;
  private List<User> userList;
  private User currentUser;
  private Statement queryStatement;
  private ResultSet queryResult;

  /**
   * Constructor to intialize the connection to the database
   * @param dbAddress Address of the SQL Server
   * @param dbPort Port of the SQL Server
   * @param dbUser Username to authenticate with
   * @param dbPass Password to authenticate with
   * @throws DatabaseException If the server information is bad or server is offline or wrong credentials
   */
  public Database(String dbAddress, int dbPort, String dbUser, String dbPass) throws DatabaseException {
    if (dbAddress != null && dbPort > 0 && dbUser != null && dbPass != null) {
      String connectionStr = String.format("jdbc:mysql://%s:%d", dbAddress, dbPort);
      try {
        dbConnection = DriverManager.getConnection(connectionStr, dbUser, dbPass);
      }
      catch (SQLException E) {
        throw new DatabaseException("Invalid database address/port or credentials entered.");
      }
      userList = new ArrayList<User>();
    }
    else
      throw new DatabaseException("Bad database information entered.");
  }

  /**
   * Get the last error the Database class encounterd
   * @return Last exception, or null if no exception have occurred
   */
  public Exception getLastError() {
	  return lastError;
  }

  /**
   * Close the potentially opened ResultSet or Statement
   */
  private void closeQueries() {
    try {
      queryResult.close();
      queryStatement.close();
    }
    catch (SQLException | NullPointerException E) {
      lastError = E;
    }
  }

  /**
   * Execute the specified query and save results
   * @param query Valid SQL query string
   * @return True if the query was executed succcessfully, false otherwise
   */
  private boolean executeQuery(String query) {
    try {
      if (!dbConnection.isClosed()) {
        //System.out.println("***" + query + "***");
        queryStatement = dbConnection.createStatement();
        queryResult = queryStatement.executeQuery(query);
        return true;
      }
    }
    catch (SQLException E) {
      //System.out.println("***" + E.getMessage() + "***");
      closeQueries();
      return false;
    }

    return false;
  }

  /**
   * Execute a command with no result
   * @param command Valid sql command string
   * @return True if the command was executed succcessfully, false otherwise
   */
  private boolean executeCommand(String command) {
    try {
      if (!dbConnection.isClosed()) {
        Statement commandStatement = dbConnection.createStatement();
        commandStatement.execute(command);
        commandStatement.close();
        return true;
      }
    }
    catch (SQLException E) {
      //System.out.println(E.getMessage());
      closeQueries();
      return false;
    }

    return false;
  }

  /**
   * Get the specified attribute of the latest query
   * @param attributeTitle Attribute to get
   * @return List of attributes from query, empty list if there are no open results
   */
  private List<String> retrieveAttribute(String attributeTitle) {
    ArrayList<String> attributes = new ArrayList<String>();
    try {
      while (queryResult.next()){
          attributes.add(queryResult.getString(attributeTitle));
      }
        closeQueries();
    }

    catch (SQLException E) {
      closeQueries();
      lastError = E;
    }

    return attributes;
  }

  /**
   * Get multiple attributes from the latest query
   * @param attributeTitles Attributes to get
   * @return List of attributes from query, empty list if there are no open results
   */
  private List<String> retrieveAttributes(String... attributeTitles) {
    List<String> attributeList = new ArrayList<String>();
    try {
      while (queryResult.next()) {
        StringBuilder sb = new StringBuilder();
        for (String title : attributeTitles)
          sb.append(queryResult.getString(title) + ":");
        String attributes = sb.toString();
        attributeList.add(attributes.substring(0, attributes.length() - 1));
      }
      closeQueries();
    }
    catch (SQLException E) {
      lastError = E;
      closeQueries();
    }

    return attributeList;
  }

  /**
   * Select a specified database
   * @param database Database to switch to
   * @return True if success, false otherwise
   */
  public boolean selectDatabase(String database) {
    if (database == null || database.length() < 1)
      return false;

    try {
      dbConnection.setCatalog(database);
      return true;
    }
    catch (SQLException E) {
      return false;
    }
  }

  /**
   * Close the SQL connection
   */
  public void close() {
    try {
      closeQueries();
      dbConnection.close();
    }
    catch (SQLException E) {
      return;
    }
  }

  /**
   * Add user to this database instance
   * @param user Username
   * @param pass Password
   * @param pos User's Position
   * @return True if success, false otherwise
   */
  public boolean addUser(String user, String pass, UserPermission.Position pos) {
    try {
      User newUser = new User(user, pass, pos);
      userList.add(newUser);
      if (pos == UserPermission.Position.Student)
        return addStudent(newUser);
      return true;
    }
    catch (UserException E) {
      return false;
    }
  }

  /**
   * Basic 'authentication' of a user with the internal userlist
   * @return True if valid user, false otherwise
   */
  public boolean authUser(String username, String password) {
      try {
        for (User U : userList) {
          if (U.equals(new User(username, password))) {
            this.currentUser = U;
            return true;
          }
        }
      }

      catch (UserException E) {
        return false;
      }

      return false;
  }

  /**
   * Retrieve the currently logged in user
   * @return The current user
   */
  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * Return a list of 'tuples' containing attributes corresponding to the table attribute titles requested
   * @param tableName Name of table to retrieve from
   * @param optArgs Optional arguments such as (order/sort) appended to the end of query
   * @param tableFields Title(s) of the desired attributes
   * @return
   */
  private List<String> getTableInformation(String tableName, String optArgs, String... tableFields) {
    ArrayList<String> result = new ArrayList<String>();
    if (optArgs == null || optArgs.length() == 0)
      optArgs = "";
    if (executeQuery(String.format("select * from `%s` %s", tableName, optArgs))) {
      try {
        while (queryResult.next()){
          String line = "";
          for (String field : tableFields) {
            line += queryResult.getString(field) + ", ";
          }
          result.add(line.substring(0,line.length()-2));
        }
        closeQueries();
      }
      catch (SQLException E) {
        closeQueries();
        lastError = E;
      }
    }
    return result;
  }

  /**
   * Update a specified table in the current database
   * @param tableName Table to update
   * @param primaryKeys Primary key(s) of the tuple to update
   * @param attributes Attribute title(s) and value(s) that are being updated
   * @return True if update succeeds, false otherwise
   */
  public boolean updateTable(String tableName, HashMap<String, String> primaryKeys, HashMap<String, String> attributes) {
      if (tableName == null || tableName.length() == 0)
        return false;

      StringBuilder sb = new StringBuilder();

      // format our new updated values for the query
      for (String key : attributes.keySet())
        sb.append(String.format("`%s`='%s', ", cleanInput(key), cleanInput(attributes.get(key))));

      String attributeValues = sb.toString();

      sb = new StringBuilder();

      // format our primary keys for the query
      for (String pKey : primaryKeys.keySet())
        sb.append(String.format("`%s`='%s' and ", cleanInput(pKey), cleanInput(primaryKeys.get(pKey))));

      String primaryKeyValues = sb.toString();

      // make sure that primary keys and attributes have at least one element
      // if not, will get a IOOB exception and this is easier than try/catch
      if (attributeValues.length() < 2 || primaryKeyValues.length() < 5)
        return false;

      attributeValues = attributeValues.substring(0, attributeValues.length() - 2);
      primaryKeyValues = primaryKeyValues.substring(0, primaryKeyValues.length() - 5);
      // need to clean input
      String updateCommand = String.format("update `%s` set %s where %s", tableName, attributeValues, primaryKeyValues);
      //System.out.println(updateCommand);
      return executeCommand(updateCommand);
  }

  /**
   * Add a new tuple to the requested table
   * @param tableName Name of the table to add to
   * @param tupleValues Values of the tuple attributes
   * @return True if succeeds, false otherwise
   */
  public boolean insertTuple(String tableName, String... tupleValues) {
    tupleValues = cleanInput(tupleValues);
    for (String value : tupleValues)
      if (value == null || value.length() == 0)
        return false;

    String insertCommand = String.format("insert into `%s` values %s", tableName, asSQLArray(tupleValues));
    //System.out.println(insertCommand);
    return executeCommand(insertCommand);
  }

  /**
   * Remove a tuple from the database
   * @param tableName Table to delete from
   * @param primaryKeys Primary key titles and values
   * @return True if the tuple was deleted, false otherwise
   */
  public boolean deleteTuple(String tableName, HashMap<String, String> primaryKeys) {
    StringBuilder sb = new StringBuilder();
    for (String pKey : primaryKeys.keySet())
      sb.append(String.format("`%s`='%s' and ", cleanInput(pKey), cleanInput(primaryKeys.get(pKey))));

    String primaryKeyValues = sb.toString();

    // make sure we have at least one primary key
    if (primaryKeyValues.length() < 10)
      return false;

    primaryKeyValues = primaryKeyValues.substring(0, primaryKeyValues.length() - 4);

    String deleteCommand = String.format("delete from `%s` where %s", tableName, primaryKeyValues);
    return executeCommand(deleteCommand);
  }

  /* MISC */
  /**
   * Create a SQL formatted 'value array' from individual values
   * @param array List of values
   * @return "one", "two", "three" -> ('one', 'two', 'three')
   */
  public String asSQLArray(String... array) {
    array = cleanInput(array);
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (String S : array) {
      // NULL can't be in encapulated with quotes
      if (!S.equals("null"))
        sb.append(String.format("'%s',", S));
      else
        sb.append("NULL ,");
    }
    String arr = sb.toString();
    // remove the last comma
    arr = arr.substring(0, arr.length() - 1);
    return arr + ")";
  }

  /**
  * Clean the input of special charaters
  */
  private String[] cleanInput(String... inputs) {
    for (int i = 0; i < inputs.length; i++)
      inputs[i] = inputs[i].replaceAll("`|'|\"|;", "");
    return inputs;
  }

  private String cleanInput(String input) {
    if (input != null)
      return input.replaceAll("`|'|\"|;", "");
    return input;
  }

  /* PROJECT SPECIFIC METHODS */

  /**
  * Add a student to the database if they don't exist
  * @param student The student to be added
    @return Truee if added or already exists, false otherwise
  */
  private boolean addStudent(User student) {
    if (executeQuery(String.format("select `ID` from `student` where `ID` = %d", student.getID()))) {
      if (!retrieveAttribute("ID").contains(student.getID().toString()))
        if (executeCommand(String.format("insert into student values(%d, '%s', 'Biology', '0')", student.getID(), student.getName())))
          return true;
      return true;
    }

    return false;
  }

  /**
  * Get the information on all departments in the department relation ( minus budget attribute )
  * @return Department Information
  */
  public List<String> getDepartmentInfo() {
    return getTableInformation("department", null, "dept_name", "building");
  }

  /**
  * Get the course id, title, department name, and credits for all courses in the course relation
  * @return See above
  */
  public List<String> getCourseInfo() {
    return getTableInformation("course", null, "course_id", "title", "dept_name", "credits");
  }

  /**
  * Get the course id, section id, semester, year, building, room number, and time slot id from all sections in the section relation
  * @return See above
  */
  public List<String> getSectionInfo() {
    return getTableInformation("section", null, "course_id", "sec_id", "semester", "year", "building", "room_number", "time_slot_id");
  }

  /**
  * Get all attributes of sections offered this year
  * @return See above
  */
  public List<String> getCurrentSections() {
    return getTableInformation("section", "where `year` = 2016", "course_id", "sec_id", "semester", "year", "building", "room_number", "time_slot_id");
  }

  /**
  * Get the classes that the current user is enrolled for
  * @return See above
  */
  public List<String> getCurrrentlyEnrolledSections() {
	  return getTableInformation("takes", String.format("where `ID` = %d and `grade` is NULL", currentUser.getID()), "course_id", "sec_id", "semester", "year");
  }

  /**
  * Register the current user for the given section
  * @param course_id ID of the course to register for
  * @param sec_id Section of the course to register
  * @return True if the student successfully registered for the section, false otherwise.
  */
  public boolean registerForSection(String course_id, String sec_id) {
    if (currentUser.getPermissions().getPosition() == UserPermission.Position.Student)
      return insertTuple("takes", currentUser.getID().toString(), course_id, sec_id, "Spring", "2016", "null");
    return false;
  }

  /**
  * Drop the current user's section enrollment matching the given course id
  * @param course_id ID of the course to drop
  * @return See above
  */
  public boolean dropSection(String course_id) {
    if (currentUser.getPermissions().getPosition() == UserPermission.Position.Student)
      return executeCommand(String.format("delete from `takes` where `course_id` = '%s' and ID = %d and `grade` is NULL", course_id, currentUser.getID()));
    return false;
  }

  /**
  * Get the GPA and courses taken of the current user if they are a student
  * @return see above
  */
  public List<String> getTranscript() {
    if (currentUser.getPermissions().getPosition() == UserPermission.Position.Student) {
      List<String> transcript = new ArrayList<String>();
      if (executeQuery(String.format("select * from `takes` natural join `course` where ID = %d and `grade` is not NULL order by year desc, case semester when 'Spring' then 1 when 'Summer' then 2 when 'Fall' then 3 end desc", currentUser.getID()))) {
        List<String> takenCourses = retrieveAttributes("title", "course_id", "semester", "year", "grade", "credits");
        double studentGPA = 0;
        double qualityPoints = 0;
        int totalCreditHours = 0;
        for (String classTaken : takenCourses) {
          String[] separate = classTaken.split(":");
          String formatted = String.format("Took %s (%s) in %s of %s and received grade of '%s' | %s credits", separate[0], separate[1], separate[2], separate[3], separate[4], separate[5]);
          transcript.add(formatted);
          int creditHours = Integer.parseInt(separate[5]);
          totalCreditHours += creditHours;
          switch (separate[4]) {
            case "A":
            case "A+":
            case "A-":
              qualityPoints += 4.0 * creditHours;
              break;
            case "B":
            case "B+":
            case "B-":
              qualityPoints += 3.0 * creditHours;
              break;
            case "C":
            case "C+":
            case "C-":
              qualityPoints += 2.0 * creditHours;
              break;
            case "D":
            case "D+":
            case "D-":
              qualityPoints += 1.0 * creditHours;
              break;
            default: // withdrawn or failed!
              qualityPoints += 0.0;
          }
        }
        if (totalCreditHours > 0) // we don't want to divide by zero!
          studentGPA = qualityPoints / totalCreditHours;
        transcript.add(0, "***Transcript for: " + currentUser.getName() + "***");
        transcript.add(1, String.format("GPA: %.2f", studentGPA));
      }
      return transcript;
    }
    // not a student
    return new ArrayList<String>();
  }
}
