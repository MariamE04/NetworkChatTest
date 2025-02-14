package CodeLab3;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static final Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("#JOIN", new Commands.JoinCommand());
        commands.put("#MESSAGE", new Commands.MessageCommand());
        commands.put("#PRIVATE", new Commands.PrivateCommand());
        commands.put("#GETLIST", new Commands.GetListCommand());
        commands.put("#PRIVATESUBLIST", new Commands.PrivateSubListCommand());
        commands.put("#HELP", new Commands.HelpCommand());
        commands.put("#LEAVE", new Commands.LeaveCommand());
        commands.put("#STOPSERVER", new Commands.StopServerCommand());
    }

    public static Command getCommand(String commandKey) {
        return commands.getOrDefault(commandKey, new Commands.UnknownCommand());
    }
}
