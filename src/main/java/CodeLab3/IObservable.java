package CodeLab3;

    // Interface for klasser, der kan sende beskeder til observatører (clients)
    public interface IObservable {
        void broadcast(String message);
    }

