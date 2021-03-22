package Client.Game.Entities;

import Client.Game.Managers.Level;
import Client.Game.Managers.AnimationManager;
import Client.Game.Managers.GameManager;
import Client.Game.Environment.Tile;
import Client.Game.Utilities.Vector;
import Client.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Klasa abstrakcyjna, reprezentuje nieokreślony typ przeciwnika
 */
public abstract class Enemy extends Entity
{
    /**
     * Właściwości klasy
     */
    public static Properties properties;

    /**
     * Szybkość poruszania się przeciwnika
     */
    private static double movementSpeed;

    /**
     * Szerokość fizycznej reprezentacji przeciwnika
     */
    private static double colliderWidth;

    /**
     * Wysokość fizycznej reprezentacji przeciwnika
     */
    private static double colliderHeight;

    /**
     * Wartość punktów, jaką otrzymuje gracz za zabicie tego przeciwnika
     */
    double pointValue;

    /**
     * Menedżer animacji klasy - przechowuje animacje poruszania się przeciwnika
     * w czterech kierunkach
     */
    AnimationManager walkingAnimations;

    /**
     * Cel poruszania się przeciwnika - na jego podstawie ustalany jest kierunek poruszania się
     */
    Vector objective;

    /**
     * Kierunek poruszania się - używany przy przełączaniu animacji w menedżerze
     */
    String facing;

    /**
     * Znacznik czasowy (w klatkach gry), mówi kiedy przeciwnik obrał cel.
     * Używany do mechanizmu zabezpieczającego - jeśli przeciwnik zbyt długo po obraniu nie
     * dotrze do celu, prawdopodobnie został on źle obrany i obierany jest nowy
     */
    int objectiveSetTS;

    /**
     * Ustala początkowe parametry przeciwnika. Wywołuje {@link #setAnimations()}
     * @param x Składowa x pozycji komórki, na której przseciwnik się pojawił
     * @param y Składowa y pozycji komórki, na której przseciwnik się pojawił
     * @param level Referencja do obecnego poziomu gry
     */
    Enemy(int x, int y, Level level)
    {
        super(level);
        lives = 1;
        position = Utils.coordsToPosition(x, y);
        objective = position.copy();
        objectiveSetTS = 0;
        facing = "down";
        setAnimations();
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
                FileInputStream fileInputStream = new FileInputStream("./data/info/entities/enemy.properties");
                properties.load(fileInputStream);
            } catch (IOException e) {
                System.out.println("Nie udalo sie odnalezc pliku .properties klasy Enemy");
            }
        }
        else{
            properties = serverProperties;
        }

        try
        {
            colliderWidth = GameManager.DEFAULT_TILE_WIDTH * Double.parseDouble(properties.getProperty("colliderWidthRatio"));
            colliderHeight = GameManager.DEFAULT_TILE_WIDTH * Double.parseDouble(properties.getProperty("colliderHeightRatio"));
            movementSpeed = Double.parseDouble(properties.getProperty("movementSpeed"));
        } catch(IllegalArgumentException e) {
            System.out.println("Wystapil problem przy parsowaniu pliku .properties klasy Enemy");
        }
    }

    /**
     * Wyświetla obecną klatkę animacji
     * @param g Obiekt, na którym rysowane są obrazki
     */
    public void display(Graphics g)
    {
        ImageIcon currentFrame = walkingAnimations.getFrame();
        int posCornerX = (int)(position.x - currentFrame.getIconWidth() / 2);
        int posCornerY = (int)(position.y - currentFrame.getIconHeight() / 2);
        currentFrame.paintIcon(currentLevel.gamePanel, g, posCornerX, posCornerY);
    }

    /**
     * Metoda odpowiedzialna za poruszanie się. Bazując na zmiennej {@link #objective}, ustala
     * kierunek poruszania się przeciwnika. Następnie sprawdza, czy ustalona zmiana pozycji
     * jest prawidłowa (nie wchodzi w przeszkody). Jeśli jest, dodaje zmianę do pozycji.
     * @param player Referencja do postaci gracza
     */
    public void move(Player player)
    {
        if (Utils.dist(position.x, position.y, objective.x, objective.y) < 1 || GameManager.frameCount - objectiveSetTS > 180)
            setObjective(player);

        Vector velocity = objective.copy().sub(position);
        velocity.setMag(movementSpeed);
        if(velocity.mag() > 0)
            walkingAnimations.setCurrentAnimation(facing);

        int currentX = (int)Math.floor(position.x / GameManager.DEFAULT_TILE_WIDTH);
        int currentY = (int)Math.floor(position.y / GameManager.DEFAULT_TILE_WIDTH);

        boolean isMoveLegalX = true;
        boolean isMoveLegalY = true;
        Tile[][] map = currentLevel.map;
        for (int x = currentX - 1; x <= currentX + 1; x++)
        {
            for (int y = currentY - 1; y <= currentY + 1; y++)
            {
                if ((x == 0 && y == 0)||(x < 0 || x >= map.length || y < 0 || y >= map[0].length))
                    continue;
                if (map[x][y].collideRect(position.copy().add(new Vector(velocity.x, 0)), colliderWidth, colliderHeight, true))
                    isMoveLegalX = false;
                if (map[x][y].collideRect(position.copy().add(new Vector(0, velocity.y )), colliderWidth, colliderHeight, true))
                    isMoveLegalY = false;
            }
        }

        if (isMoveLegalX)
            position.x += velocity.x;
        if (isMoveLegalY)
            position.y += velocity.y;

        walkingAnimations.step();
//        if(collideEntity(player.position, Player.colliderWidth, Player.colliderHeight))
//        {
//            //player.hurt();
//        }
    }

    /**
     * Getter zmiennej {@link #isDead}
     * @return Zmienna {@link #isDead}
     */
    public boolean isDead()
    {
        return isDead;
    }

    /**
     * Dodaje animacje do menedżera
     */
    abstract void setAnimations();

    /**
     * Metda, w któej ustalany jest cel poruszania się - w zależności od typu przeciwnika.
     * W zależności od obranego celu, ustala zmienną {@link #facing}
     * @param player Referencja do postaci gracza
     */
    abstract void setObjective(Player player);

    /**
     * Ustawia flagę {@link #isDead}
     */
    public void setDead(){
        isDead = true;
    }

    /**
     * Getter zmiennej {@link #pointValue}
     * @return Zmienna {@link #pointValue}
     */
    public double getPointValue() {
        return pointValue;
    }
}
