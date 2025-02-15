package CodeLab3;

public interface Command {
    void execute(ChatServerDemo.ClientHandler client, String message);
}
