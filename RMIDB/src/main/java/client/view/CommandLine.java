package client.view;

/**
 * Wrapper class for console inputs
 *
 * @author Perttu Jääskeläinen
 */
public class CommandLine {

    public static final String DELIMETER = " ";
    private Command cmd;
    public String[] message; // the entire message

    public CommandLine(String command) {
        message = command.split(DELIMETER);
        try {
            cmd = Command.valueOf(message[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new FormatException(message[0].toUpperCase() + "Error when reading command" + command);
        }
        if (message.length != cmd.getLength()) {
            throwException(cmd.getDescription());
        }
    }
    private void throwException(String desc) {
        throw new FormatException("Invalid format, usage: " + desc);
    }
    public Command getCommand() {
        return this.cmd;
    }
}
