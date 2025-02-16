package CodeLab3;

//Metoden execute() skal implementeres af alle kommandoer.
public interface Command {
    void execute(ChatServerDemo.ClientHandler client, String message);
}

//Den tager en ClientHandler (repræsenterer en klient i chatten).
//Den tager en message (kommandoens indhold).