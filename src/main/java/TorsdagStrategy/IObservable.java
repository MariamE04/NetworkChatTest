package TorsdagStrategy;

// Interface for klasser, der kan sende beskeder til observatører (clients)
public interface IObservable {
    void addObserver(IObserver observer);

    void removeObserver(IObserver observer);

    void broadcast(String message);
}

