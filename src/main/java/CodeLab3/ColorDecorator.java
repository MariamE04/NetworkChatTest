package CodeLab3;

public class ColorDecorator implements ITextDecorator {
    private String color; // holder farvekoden, der skal bruges til at dekorere teksten.

    public ColorDecorator(String color) {
        this.color = color; //Når en instans af ColorDecorator oprettes,
        // tages farvekoden som parameter og gemmes i color-variablen
    }

    @Override
    public String decorate(String text) {
        return color + text + "\u001B[0m";
        //Når metoden decorate kaldes, tilføjer den den gemte farvekode til starten af den tekst, der sendes som parameter.
        //Efter teksten tilføjes "\u001B[0m", som er en ANSI escape-sekvens, der nulstiller farven tilbage til standardfarven, så ikke al efterfølgende tekst bliver farvet.
    }
}

//Formål: ColorDecorator er en konkret implementering af ITextDecorator, der dekorerer tekst med en farve.
// Denne klasse bruger en farvekode (som en ANSI escape-sekvens) for at ændre farven på teksten i konsollen.