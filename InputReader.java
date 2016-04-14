import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.NumberFormatException;

/**
 * Simple class utilizing the buffered reader to obtain input from the user
 * Implements Singleton design pattern ( can also be statically accessed )
 * @author Michael Snyder
 */

public class InputReader {

  private static InputReader instance;
  private BufferedReader buffInput;

  /**
   * Get the input reader
   * @return InputReader Singleton
   */
  public static InputReader getReader() {
    if (instance == null)
      instance = new InputReader();
    return instance;
  }

  private InputReader() {
    buffInput = new BufferedReader(new InputStreamReader(System.in));
  }

  /**
   * Prompt the user to enter input
   * @param prompt String to print out to the user
   * @return Input from user via cin
   */
  public String readString(String prompt) {
    System.out.print(prompt);
    try {
      String inputStr = buffInput.readLine();
      return inputStr;
    }
    catch (IOException E) {
      return "";
    }
  }

  /**
   * Prompt the user to enter an unsigned integer (-1 means invalid)
   * More of a convenience menthod for the CLI
   * @param prompt String to print out to the user
   * @return Unsigned integer the user entered if valid, -1 otherwise
   */
  public int readUnsignedInt(String prompt) {
    String input = readString(prompt);
    try {
    	int num = Integer.parseInt(input);
    	if (num < 0)
    		throw new NumberFormatException();
    	return num;
    }
    catch (NumberFormatException E) {
      return -1;
    }
  }

  /**
   * Static method to prompt the user to enter an input ( does not need an instance of the class)
   * @param prompt String to print out to the user
   * @return INput from the user via cin
   */
  public static String _readString(String prompt) {
    BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
    System.out.print(prompt);
    try {
      String inputStr = userInput.readLine();
      return inputStr;
    }
    catch (IOException E) {
      return "";
    }
  }
}
