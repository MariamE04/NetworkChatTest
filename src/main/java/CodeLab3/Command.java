package CodeLab3;

public interface Command {
    void execute(ChatServerDemo.ClientHandler client, String message);
}

class JoinCommand implements Command {
    @Override
    public void execute(ChatServerDemo.ClientHandler client, String message) {
        String[] parts = message.split(" ");
        if (parts.length > 1) {
            client.setName(parts[1]);
            client.getServer().broadcast("A new person joined the chat. Welcome " + client.getName());
        } else {
            client.sendMessage("Invalid format. Use: #JOIN <nickname>");
        }
    }
}

class MessageCommand implements Command {
    @Override
    public void execute(ChatServerDemo.ClientHandler client, String message) {
        if (message.length() > 9) {
            client.getServer().broadcast(client.getName() + ": " + message.substring(9).trim());
        } else {
            client.sendMessage("Invalid message format. Use: #MESSAGE <your message>");
        }
    }
}

class PrivateMessageCommand implements Command {
    @Override
    public void execute(ChatServerDemo.ClientHandler client, String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            ChatServerDemo.ClientHandler recipient = ((ChatServerDemo) client.getServer()).getClientByName(parts[1]);
            if (recipient != null) {
                recipient.notify(client.getName() + " (private): " + parts[2]);
            } else {
                client.sendMessage("User " + parts[1] + " not found.");
            }
        } else {
            client.sendMessage("Invalid private message format. Use: #PRIVATE <nickname> <message>");
        }
    }
}

class GetListCommand implements Command {
    @Override
    public void execute(ChatServerDemo.ClientHandler client, String message) {
        // Cast serveren til ChatServerDemo og brug getClients
        ChatServerDemo server = (ChatServerDemo) client.getServer();
        for (ChatServerDemo.ClientHandler c : server.getClients()) {
            client.sendMessage(c.getName());
        }
    }
}

class HelpCommand implements Command {
    @Override
    public void execute(ChatServerDemo.ClientHandler client, String message) {
        client.sendMessage("List of available commands: #JOIN, #MESSAGE, #PRIVATE, #GETLIST, #HELP, #STOPSERVER");
    }
}

class StopServerCommand implements Command {
    @Override
    public void execute(ChatServerDemo.ClientHandler client, String message) {
        ((ChatServerDemo) client.getServer()).shutdown();
    }
}
