package CodeLab3;

import java.io.IOException;
import java.util.ArrayList;

public class Commands {
    private static ArrayList<String> bannedWords = new ArrayList<>();

    // Kommando for at tilslutte chatten
    public static class JoinCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            String[] parts = message.split(" ", 2);
            if (parts.length < 2) {
                client.sendMessage("Usage: #JOIN <name>");
            } else {
                client.setName(parts[1]);
                client.getServer().broadcast("A new person joined the chat. Welcome " + parts[1]);
            }
        }
    }

    // Kommando for at sende en besked til alle
    public static class MessageCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            if (message.length() < 9) {
                client.sendMessage("Invalid message format. Use: #MESSAGE <text>");
                return;
            }
            String messageContent = message.substring(9).trim();

            // Tjek om beskeden indeholder bandeord og tilføj dem til listen
            checkForBannedWords(messageContent);

            client.getServer().broadcast(client.getName() + ": " + messageContent);
        }
    }

        // Kommando for at sende en privat besked
        public static class PrivateCommand implements Command {
            @Override
            public void execute(ChatServerDemo.ClientHandler client, String message) {
                String[] parts = message.split(" ", 3);
                if (parts.length < 3) {
                    client.sendMessage("Usage: #PRIVATE <receiver> <message>");
                    return;
                }
                String receiver = parts[1];
                String privateMessage = parts[2];  // Undgå navnekonflikt
                client.getServer().sendPrivateMessage(client.getName(), receiver, privateMessage);
            }
        }

        // Kommando for at få listen over brugere
        public static class GetListCommand implements Command {
            @Override
            public void execute(ChatServerDemo.ClientHandler client, String message) {
                client.sendMessage("Online users: " + client.getServer().getUserList());
            }
        }

        // Kommando for at få en privat liste
        public static class PrivateSubListCommand implements Command {
            @Override
            public void execute(ChatServerDemo.ClientHandler client, String message) {
                client.sendMessage("Private sublist: " + client.getServer().getPrivateSubList(client.getName()));
            }
        }

        // Kommando for at vise hjælpemenu
        public static class HelpCommand implements Command {
            @Override
            public void execute(ChatServerDemo.ClientHandler client, String message) {
                client.sendMessage("Available commands: #JOIN, #MESSAGE, #PRIVATE, #GETLIST, #PRIVATESUBLIST, #HELP, #STOPSERVER #LEAVE");
            }
        }

        // Kommando for at stoppe serveren
        public static class StopServerCommand implements Command {
            @Override
            public void execute(ChatServerDemo.ClientHandler client, String message) {
                client.sendMessage("Server shutting down...");
                client.getServer().shutdown();
            }
        }

    // Kommando for at forlade chatten
    public static class LeaveCommand implements Command {
        @Override
        public void execute(ChatServerDemo.ClientHandler client, String message) {
            client.getServer().broadcast(client.getName() + " has left the chat.");
            client.sendMessage("Goodbye, " + client.getName() + ". You have left the chat.");

            // Fjern klienten fra serverens klientliste og luk forbindelsen
            client.getServer().removeClient(client);
            try {
                client.getClientSocket().close();
                client.getOut().close();
                client.getIn().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Ukendt kommando
        public static class UnknownCommand implements Command {
            @Override
            public void execute(ChatServerDemo.ClientHandler client, String message) {
                client.sendMessage("Unknown command. Type #HELP for available commands.");
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

}
