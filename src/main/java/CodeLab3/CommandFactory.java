package CodeLab3;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static final Map<String, Command> commands = new HashMap<>(); //Importerer HashMap og Map, som bruges til at gemme kommandoer.

    //commands: En HashMap, der gemmer kommandoer med deres tilhørende objekter.
    //Lægger alle kommandoer i commands-mappen, så de nemt kan findes.
    static {
        commands.put("#JOIN", new Commands.JoinCommand());
        commands.put("#MESSAGE", new Commands.MessageCommand());
        commands.put("#PRIVATE", new Commands.PrivateCommand());
        commands.put("#GETLIST", new Commands.GetListCommand());
        commands.put("#PRIVATESUBLIST", new Commands.PrivateSubListCommand());
        commands.put("#HELP", new Commands.HelpCommand());
        commands.put("#LEAVE", new Commands.LeaveCommand());
        commands.put("#STOPSERVER", new Commands.StopServerCommand());
        commands.put("#COLORMESSAGE", new Commands.ColorMessageCommand());
    }

    //Finder en kommando baseret på dens nøgle (#JOIN, #MESSAGE osv.).
    //Hvis kommandoen ikke findes, returneres UnknownCommand.
    public static Command getCommand(String commandKey) {
        return commands.getOrDefault(commandKey, new Commands.UnknownCommand());
    }
}

