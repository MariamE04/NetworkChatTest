package ChatServerDemo;

// Interface for klasser, der kan modtage beskeder fra en observerbar kilde
public interface IObserver {
    void notify(String message);
}