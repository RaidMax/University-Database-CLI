/**
 * Main class for the CSC3300 University Database CLI
 * Initializes the database connection, adds the project specific users
 * and starts the main CLI
 * @author Michael Snyder
 */

public class Program {

	public static void main(String[] args)
	{
		Database rootDatabase = null;
		try {
			rootDatabase = new Database("localhost", 3306, "root", "new-password");
		}
		catch (DatabaseException E) {
			System.out.println(E.getMessage());
			return;
		}

		if (!rootDatabase.selectDatabase("university")) {
			System.out.println("Could not select the university database. Exiting...");
			rootDatabase.close();
			return;
		}

		// add our test users with specified permissions
		rootDatabase.addUser("brown", "brown123", UserPermission.Position.Staff);
		rootDatabase.addUser("grey", "grey123", UserPermission.Position.Student);

		System.out.println("Welcome to the CSC3300 University Database CLI!\nPlease log in.");
		boolean validAuth = false;
		do {
			String username = InputReader._readString("Enter username: ");
			String password = InputReader._readString("Enter password: ");
			validAuth = rootDatabase.authUser(username, password);
			if (!validAuth)
				System.out.println("Invalid credentials supplied. Please try again.");
		} while (!validAuth);

		UserInterface userInteraction = new UserInterface(rootDatabase);
		userInteraction.mainMenu();

		System.out.println("Thanks you for using the CSC3300 University Database CLI!");
		rootDatabase.close();
	}
}
