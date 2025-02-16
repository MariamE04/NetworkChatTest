package CodeLab3;

import java.io.IOException;
import java.util.ArrayList;

//Klassen Commands indeholder forskellige kommandoer.
public class Commands {
    private static ArrayList<String> bannedWords = new ArrayList<>(); //En statisk liste, der gemmer bandeord.

    //Indre klasse, som implementerer Command.
    //Implementerer execute() metoden.

    // Kommando for at tilslutte chatten
    public static class JoinCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            String[] parts = message.split(" ", 2); //Deler beskeden op i to dele: kommando (#JOIN) og brugernavn.
            if (parts.length < 2) { //Hvis beskeden ikke indeholder et navn, sender den en fejlmeddelelse.
                client.sendMessage("Usage: #JOIN <name>");
            } else {
                client.setName(parts[1]); //Sætter klientens navn og sender en besked til alle i chatten.
                client.getServer().broadcast("A new person joined the chat. Welcome " + parts[1]);
            }
        }
    }

    // Kommando for at sende en besked til alle
    public static class MessageCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            if (message.length() < 9) { //Hvis beskeden er for kort, sendes en fejlmeddelelse.
                client.sendMessage("Invalid message format. Use: #MESSAGE <text>");
                return;
            }
            String messageContent = message.substring(9).trim(); //Beskeden hentes uden #MESSAGE kommandoen.

            // Tjek om beskeden indeholder bandeord og tilføj dem til listen
            checkForBannedWords(messageContent);

            client.getServer().broadcast(client.getName() + ": " + messageContent); //Sender beskeden til alle i chatten.
        }
    }

    // Kommando for at sende en privat besked
    public static class PrivateCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            String[] parts = message.split(" ", 3); //Deler beskeden i tre dele: kommando (#PRIVATE), modtager og besked.
            if (parts.length < 3) { //Hvis beskeden ikke er komplet, sendes en fejlmeddelelse.
                client.sendMessage("Usage: #PRIVATE <receiver> <message>");
                return;
            }
            //Henter modtagerens navn og beskeden.
            String receiver = parts[1];
            String privateMessage = parts[2];  // Undgå navnekonflikt
            //Sender beskeden til modtageren.
            client.getServer().sendPrivateMessage(client.getName(), receiver, privateMessage);
        }
    }

    // Kommando for at få listen over brugere
    public static class GetListCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            client.sendMessage("Online users: " + client.getServer().getUserList()); //Sender en liste over online brugere til klienten.
        }
    }

    // Kommando for at få en privat liste
    public static class PrivateSubListCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler sender, String message) {
            String[] parts = message.split(" ", 2); //deler beskeden i 2 del
            if (parts.length < 2) {
                sender.sendMessage("Usage: #PRIVATESUBLIST \"nickname1,nickname2\" <message>");
                return;
            }

            // Udtræk navne og besked
            String[] data = parts[1].split("\" ", 2);
            if (data.length < 2) { //Hvis inputtet er forkert, sendes en fejlmeddelelse.
                sender.sendMessage("Error: Invalid format. Use #PRIVATESUBLIST \"nickname1,nickname2\" <message>");
                return;
            }

            //Deler input i to: kommando (#PRIVATESUBLIST) og resten af beskeden.
            String[] recipients = data[0].replace("\"", "").split(",");//Finder listen af modtagere i citationstegn. Fjerner citationstegn og deler brugernavne ved ,.
            String msgContent = data[1];

            // Send beskeden til alle i listen
            ChatServerDemo server = sender.getServer();
            for (String recipient : recipients) { //looper igen r
                ChatServerDemo.ClientHandler client = server.getClientByName(recipient.trim()); //Finder den specifikke klient baseret på navn.
                if (client != null) {
                    client.sendMessage("Private group message from " + sender.getName() + ": " + msgContent);
                } else {
                    sender.sendMessage("User " + recipient + " not found.");
                } //Sender beskeden til hver modtager eller giver en fejl, hvis en bruger ikke findes.
            }
        }
    }


    // Kommando for at vise hjælpemenu
    public static class HelpCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            //Sender en liste over tilgængelige kommandoer.
            client.sendMessage("Available commands: #JOIN, #MESSAGE, #PRIVATE, #GETLIST, #PRIVATESUBLIST, #HELP, #STOPSERVER #LEAVE, #COLORMESSAGE");
        }
    }

    // Kommando for at stoppe serveren
    public static class StopServerCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            //Informerer klienterne og lukker serveren.
            client.sendMessage("Server shutting down...");
            client.getServer().shutdown();
        }
    }

    // Kommando for at forlade chatten
    public static class LeaveCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            client.getServer().broadcast(client.getName() + " has left the chat."); //Sender en besked til alle i chatten om, at brugeren har forladt chatten.
            client.sendMessage("Goodbye, " + client.getName() + ". You have left the chat."); //Sender en farvelbesked til klienten.

            // Fjern klienten fra serverens klientliste og luk forbindelsen
            client.getServer().removeClient(client);
            try {
                client.getClientSocket().close();
                client.getOut().close();
                client.getIn().close();
            } catch (IOException e) { //Lukker klientens forbindelse og håndterer eventuelle fejl.
                e.printStackTrace();
            }
        }
    }


    // Ukendt kommando
    public static class UnknownCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            client.sendMessage("Unknown command. Type #HELP for available commands."); //sender besked til klient
        }
    }

    // Metode til at tjekke om en besked indeholder bandeord
    private static void checkForBannedWords(String message) {
        String[] banned = {"fuck", "bitch", "shit", "whore"};  // Eksempler på bandeord
        for (String word : banned) {
            if (message.toLowerCase().contains(word)) {
                // Tilføj bandeordet til en central liste i serveren, hvis det findes
                Commands.addBadWord(word);  // Kald på addBadWord fra Commands
            }
        }
    }


    public static void addBadWord(String word) {
        if (!bannedWords.contains(word)) {
            bannedWords.add(word);
            System.out.println("Bandeord opdaget og tilføjet: " + word);
        }
    }

 //_____________________________________________________________________________________________________________
    //Denne metode implementerer den funktionalitet, der er defineret i Command-interfacet. Den tager to parametre:
    //client: En handler for den klient, der sender kommandoen.
    //message: En streng, der repræsenterer den besked, klienten forsøger at sende med farve.

    public static class ColorMessageCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            if (message.length() < 15) {
                client.sendMessage("Invalid message format. Use: #COLORMESSAGE <color> <message>");
                return;
                //Hvis beskedens længde er mindre end 15 tegn (hvilket er for kort til at indeholde både farve og besked),
                // sendes en fejlmeddelelse til klienten, og metoden stopper (return).
            }
            //Beskeden opdeles i tre dele (farve, beskedtype, og selve beskeden) ved at bruge split(" ", 3).
            // Dette opdeler strengen i maksimalt tre dele. Hvis resultatet ikke indeholder tre dele,
            // sendes en vejledning om, hvordan kommandoen skal bruges, og metoden stopper.
            String[] parts = message.split(" ", 3);
            if (parts.length < 3) {
                client.sendMessage("Usage: #COLORMESSAGE <color> <message>");
                return;
            }

            String color = parts[1];  // Farven
            String msgContent = parts[2];  // Selve beskeden

            //switch til at vælge farven
            String colorCode;
            switch (color.toUpperCase()) { //gør farven uafhængig af store og små bogstaver.
                case "RED":
                    colorCode = "\u001B[31m";  // Rød farve /ANSI escape-sekvens
                    break;
                case "GREEN":
                    colorCode = "\u001B[32m";  // Grøn farve
                    break;
                case "BLUE":
                    colorCode = "\u001B[34m";  // Blå farve
                    break;
                case "YELLOW":
                    colorCode = "\u001B[33m";  // Gul farve
                    break;
                case "CYAN":
                    colorCode = "\u001B[36m";  // Cyan farve
                    break;
                case "MAGENTA":
                    colorCode = "\u001B[35m";  // Magenta farve
                    break;
                default:
                    colorCode = "\u001B[37m";  // Standard (hvid farve) hvis ukendt farve
                    client.sendMessage("Unknown color. Defaulting to white.");
                    break;
            }

            // Dekorér beskeden med den ønskede farve
            ITextDecorator colorDecorator = new ColorDecorator(colorCode);
            String coloredMessage = colorDecorator.decorate(msgContent);

            // Send den farvede besked til alle brugere ved hjælp af metoden broadcast i ChatServerDemo.
            //Den sender beskeden sammen med klientens navn.
            client.getServer().broadcast(client.getName() + ": " + coloredMessage);
        }
    }
}
