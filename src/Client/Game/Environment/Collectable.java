package Client.Game.Environment;

import Client.Game.Entities.Entity;
import Client.Game.Managers.GameManager;
import Client.Game.Managers.Level;
import Client.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Klasa reprezentująca znajdzki na planszy gry
 */
public class Collectable extends Entity{

    /**
     * Komórka na której znajduje się znajdzka
     */
    private Tile tile;

    /**
     * Zmienna zawierajaco informacje czy znajdzka widoczna na planszy
     */
    private boolean visible = false;

    /**
     * Właściwości klasy
     */
    public static Properties properties;

    /**
     * Przechowuje obrazek znajdźki
     */
    private ImageIcon imageFrame;

    /**
     * Szerokość fizycznej reprezentacji znajdzki
     */
    private static double colliderWidth;

    /**
     * Wysokość fizycznej reprezentacji znajdzki
     */
    private static double colliderHeight;

    /**
     * Określa czy znajdzka juz zebrana
     */
    protected boolean collected = false;

    /**
     * Kontruktor
     * @param posX Składowa x pozycji
     * @param posY Składowa y pozycji
     * @param level Okresla referencję do poziomu na ktorym znajduje sie znajdzka
     * @param tile Kratka, na której znajduje się znajdźka
     * @param imageFrame Ikona znajdźki
     */
    protected Collectable(int posX, int posY, Level level, Tile tile, ImageIcon imageFrame) {
        super(level);
        super.position = Utils.coordsToPosition(posX,posY);
        this.tile = tile;
        this.imageFrame = imageFrame;
    }

    /**
     * Wczytuje właściwości tej klasy z pliku, zapisuje do zmiennej {@link #properties}
     * @param serverProperties Właściowści pobrane z serwera, jeśli gra jest w trybie online - w
     *                         przeciwnym wypadku ma wartość null
     */
    public static void loadProperties(Properties serverProperties)
    {
        if(serverProperties == null){
            try{
                properties = new Properties();
                FileInputStream fileInputStream = new FileInputStream("./data/info/entities/collectable.properties");
                properties.load(fileInputStream);
            } catch (IOException e) {
                System.out.println("Nie udalo sie odnalezc pliku .properties klasy Collectable");
            }
        }
        else{
            properties = serverProperties;
        }

        try
        {
            colliderWidth = GameManager.DEFAULT_TILE_WIDTH * Double.parseDouble(properties.getProperty("colliderWidthRatio"));
            colliderHeight = GameManager.DEFAULT_TILE_WIDTH * Double.parseDouble(properties.getProperty("colliderHeightRatio"));

        } catch(IllegalArgumentException e) {
            System.out.println("Wystapil problem przy parsowaniu pliku .properties klasy Collectable");
        }
    }

    /**
     * Ustawia widoczność znajdźki
     * @param visible określający czy ma być widoczne czy nie
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Metoda wyświetla znajdzke na planszy gry
     * @param g typu {@code Graphics} pozwalający wyswietlac znajdzke na planszy
     */
    public void display(Graphics g) {
        if (this.tile.boostable && !collected){
            ImageIcon currentFrame = imageFrame;
            int posCornerX = (int)(super.position.x - currentFrame.getIconWidth() / 2);
            int posCornerY = (int)(super.position.y - currentFrame.getIconHeight() / 2);
            currentFrame.paintIcon(tile.getCurrentLevel().gamePanel, g, posCornerX, posCornerY);
        }
    }

    /**
     * Metoda implementowana anonimowo przy wczytywaniu znajdziek na poziom gry.
     * Obsługuje podnoszsenie znajdziek
     */
    public void collect(){}
}
