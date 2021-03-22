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
 * Klasa reprezentująca bombę na planszy
 */
public class Bomb
{
    /**
     * Właściwości klasy
     */
    public static Properties properties;

    /**
     * Zasięg wybuchu bomby - w liczbie komórek planszy
     */
    public static int range;

    /**
     * Ilość klatek gry, przez które bomba istnieje w świecie gry
     */
    private static int lifespan;

    /**
     * Ilość klatek (życia), która zastała bombie 
     */
    private int lifeLeft;

    /**
     * Menedżer animacji klasy - w tym przypadku przechowuje jedynie animację płonięcia lontu
     */
    private AnimationManager burningAnimations;

    /**
     * Wektor reprezentujący pozycję bomby na planszy
     */
    private Vector position;

    /**
     * Komórka na której znajduje się kesplozja
     */
    private Tile tile;
    
    /**
     * Konstruktor, przypisuje wartość komórce planszy, ustawia pozycję
     * bomby na środku komórki, ustala początkowe życie na maksimum.
     * Wywołuje {@link #setAnimations()}
     * @param tile Komórka na której znajduje się kesplozja
     */
    public Bomb(Tile tile)
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
                FileInputStream fileInputStream = new FileInputStream("./data/info/environment/bomb.properties");
                properties.load(fileInputStream);
            } catch (IOException e) {
                System.out.println("Nie udalo sie odnalezc pliku .properties klasy Bomb");
            }
        }
        else{
            properties = serverProperties;
        }

        try {
            range = Integer.parseInt(properties.getProperty("range"));
            lifespan = Integer.parseInt(properties.getProperty("lifespan"));
        } catch(IllegalArgumentException e) {
            System.out.println("Wystapil problem przy parsowaniu pliku .properties klasy Bomb");
        }
    }

    /**
     * Dodaje animacje do menedżera, wykorzystuje zmienną {@link Client.Utils#bombFrames}
     */
    private void setAnimations()
    {
        int animationSpeed = Integer.parseInt(properties.getProperty("animationSpeed"));
        burningAnimations = new AnimationManager();
        burningAnimations.addAnimation(Utils.bombFrames, animationSpeed, "burning");
    }

    /**
     * Wyświetla obecną klatkę animacji 
     * @param g Obiekt, na którym rysowane są obrazki
     */
    public void display(Graphics g)
    {
        ImageIcon currentFrame = burningAnimations.getFrame();
        int posCornerX = (int)(position.x - currentFrame.getIconWidth() / 2);
        int posCornerY = (int)(position.y - currentFrame.getIconHeight() / 2);
        currentFrame.paintIcon(tile.getCurrentLevel().gamePanel, g, posCornerX, posCornerY);

        tick();
    }

    /**
     * Aktualizuje obiekt. Wykonuje {@link AnimationManager#step()}, upływa życie.
     * Po spadnięciu życia do 0, usuwa bombę z poziomu klasy {@link Tile} wywołując {@link Tile#explodeCenter(int, int, int)}
     */
    private void tick()
    {
        lifeLeft--;
        burningAnimations.step();

        if (lifeLeft <= 0)
        {
            tile.explodeCenter(tile.getX(), tile.getY(), range);
            tile.killBomb();
        }
    }

    /**
     * Metoda zwiększająca zasięg bomby
     */
    public static void increaseRange(){
        range++;
    }
}


