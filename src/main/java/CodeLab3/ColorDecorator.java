package CodeLab3;

public class ColorDecorator implements ITextDecorator {
    private String color;

    public ColorDecorator(String color) {
        this.color = color;
    }

    @Override
    public String decorate(String text) {
        return color + text + "\u001B[0m";  // Tilføjer escape-kode for at nulstille farven
    }
}
