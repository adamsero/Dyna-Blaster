package Client.Game.Utilities;

import Client.Utils;

import java.awt.*;

/**
 * Klasa reprezentująca rozmywający się napis po zabicu przeciwnika
 */
public class FadingLabel {
    private final static int lifespan = 180;
    private final String text;
    private int lifeLeft;
    private final Vector pos;
    private boolean isDead;

    /**
     * Konstruktor
     * @param text {@code String} tekst napisu
     * @param x położenie w x
     * @param y położenie w y
     */
    public FadingLabel(String text, int x, int y) {
        this.text = text;
        pos = new Vector(x, y);
        lifeLeft = lifespan;
    }

    /**
     * Metoda wyswietlająca rozmywany napis
     * @param g2d obiekt typu {@code Graphics2D} reprezentujący obecny kontekst graficzny
     * @param font czcionka która ma być użyta dla napisu
     */
    public void displayAndUpdate(Graphics2D g2d, Font font) {
        Color color = new Color(255, 255, 255, (int)(lifeLeft * 255d / lifespan));
        g2d.setColor(color);
        Utils.drawCenteredString(g2d, text, pos, font);

        lifeLeft--;
        if(lifeLeft <= 0)
            isDead = true;
    }

    /**
     * Getter pola {#link isDead}
     * @return Zmienna {@link #isDead}
     */
    public boolean isDead() {
        return isDead;
    }
}
