package client.view;

/**
 * Wrapper class for console inputs
 *
 * @author Perttu Jääskeläinen
 */
public class CommandLine {

    private final String DELIMETER = " ";
    private String message; // the entire message
    private Command cmd;    // the commad in the message
    private String body;    // the body of the message

    public CommandLine(String command) {
        extract(command);
    }

    public String getMessage() {
        return this.message;
    }

    public Command getCommand() {
        return this.cmd;
    }

    public String getBody() {
        return this.body;
    }

    private void extract(String message) {
        this.message = message;
    }
}
