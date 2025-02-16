package CodeLab3;

public interface ITextDecorator {
    String decorate(String text);
}

//Formål: ITextDecorator er et interface, som definerer en metode, decorate, som skal implementeres af alle klasser,
// der ønsker at dekorere tekst på en bestemt måde.

//Metoden decorate(String text) tager en tekststreng som parameter og returnerer en dekoreret version af teksten.
// Det er en abstrakt metode, hvilket betyder, at hver klasse,
// der implementerer dette interface, skal definere, hvordan de dekorerer teksten.