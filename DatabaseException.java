import java.lang.Exception;

public class DatabaseException extends Exception {

	private static final long serialVersionUID = 412342326837574723L;

	public DatabaseException(String message) {
		super(message);
	}
}
