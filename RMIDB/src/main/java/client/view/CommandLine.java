package client.view;

/**
 * Wrapper class for console inputs
 *
 * @author Perttu Jääskeläinen
 */
public class CommandLine {

    private final String DELIMETER = " ";
    private Command cmd;
    public String[] message; // the entire message

    public CommandLine(String command) {
        extract(command);
    }
    public Command getCommand() {
        return this.cmd;
    }
    private void extract(String message) {
        this.message = message.split(DELIMETER);
        this.cmd = Command.valueOf(this.message[0].toUpperCase());
    }
}
