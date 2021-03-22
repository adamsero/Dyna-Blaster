package Client.Game.Environment;

import Client.Game.Managers.AnimationManager;
import Client.Game.Utilities.Vector;
import Client.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Klasa reprezentująca eksplozję na planszy po wybuchu bomby
 */
public class Explosion
{
    /**
     * Właściwości klasy
     */
    public static Properties properties;

    /**
     * Menedżer animacji klasy - w tym przypadku przechowuje jedynie animację eksplozji
     */
    private AnimationManager explosionAnimations;

    /**
     * Ilość klatek gry pomiędzy kolejnymi wybuchami przy łańcuchowym
     * rozchodzeniu się wybuchu
     */
    public static int delay;

    /**
     * Ilość klatek gry, przez które eksplozja istnieje w świecie gry
     */
    private static int lifespan;

    /**
     * Ilość życia, jakie zostało eksplozji, po którym przestaje ranić garcza i duchy
     */
    private static int dangerThreshold;

    /**
     * Ilość klatek (życia), która zastała eksplozji
     */
    private int lifeLeft;

    /**
     * Komórka na której znajduje się eksplozja
     */
    private Tile tile;

    /**
     * Wektor reprezentujący pozycję eksplozji na planszy
     */
     public Vector position;

    /**
     * Konstruktor, przypisuje wartość komórce planszy, ustawia pozycję
     * eksplozji na środku komórki, ustala początkowe życie na maksimum.
     * Wywołuje {@link #setAnimations()}
     * @param tile Komórka na której znajduje się eksplozja
     */
    public Explosion(Tile tile)
    {
        this.tile = tile;
        lifeLeft = lifespan;
        position = Utils.coordsToPosition(tile.getX(), tile.getY());
        setAnimations();
    }

    /**
     * Wczytuje właściwości tej klasy z pliku, zapisuje do zmiennej {@link #properties}
     * @param serverProperties Właściowści pobrane z serwera, jeśli gra jest w trybie online - w
     *                         przeciwnym wypadku ma wartość null
     */
    public static void loadProperties(Properties serverProperties)
    {
        if (serverProperties == null){
            try{
                properties = new Properties();
                FileInputStream fileInputStream = new FileInputStream("./data/info/environment/explosion.properties");
                properties.load(fileInputStream);
            } catch (IOException e) {
                System.out.println("Nie udalo sie odnalezc pliku .properties klasy Explosion");
            }
        }
        else{
            properties = serverProperties;
        }

        try {
            delay = Integer.parseInt(properties.getProperty("delay"));
            lifespan = Integer.parseInt(properties.getProperty("lifespan"));
            dangerThreshold = Integer.parseInt(properties.getProperty("dangerThreshold"));
        } catch(IllegalArgumentException e) {
            System.out.println("Wystapil problem przy parsowaniu pliku .properties klasy Explosion");
        }
    }

    /**
     * Dodaje animacje do menedżera, wykorzystuje zmienną {@link Client.Utils#explosionFrames}
     */
    private void setAnimations()
    {
        explosionAnimations = new AnimationManager();
        explosionAnimations.addAnimation(Utils.explosionFrames, 1, "exploding");
    }

    /**
     * Wyświetla obecną klatkę animacji
     * @param g Obiekt, na którym rysowane są obrazki
     */
    public void display(Graphics g)
    {
        ImageIcon currentFrame = explosionAnimations.getFrame();
        int posCornerX = (int)(position.x - currentFrame.getIconWidth() / 2);
        int posCornerY = (int)(position.y - currentFrame.getIconHeight() / 2);
        currentFrame.paintIcon(tile.getCurrentLevel().gamePanel, g, posCornerX, posCornerY);

        tick();
    }

    /**
     * Aktualizuje obiekt. Wykonuje {@link AnimationManager#step()}, upływa życie.
     * Po spadnięciu życia do 0, usuwa eksplozję z poziomu klasy {@link Tile} wywołując {@link Tile#extinguish()}
     */
    private void tick()
    {
        lifeLeft--;
        explosionAnimations.step();
        if(lifeLeft <= 0)
            tile.extinguish();
    }

    /**
     * Mówi, czy eksplozja może zranić gracza lub ducha
     * @return Wartość logiczna
     */
    public boolean canHurt() {
        return lifespan > dangerThreshold;
    }
}
