package Client.Game.Entities;

import Client.Game.Environment.Collectable;
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
import java.util.ArrayList;
import java.util.Properties;

/**
 * Klasa reprezentująca postać gracza
 */
public class Player extends Entity
{
    /**
     * Właściwości klasy
     */
    public static Properties properties;

    /**
     * Szybkość poruszania się gracza
     */
    private static double movementSpeed;

    /**
     * Szerokość fizycznej reprezentacji gracza
     */
    static double colliderWidth;

    /**
     * Wysokość fizycznej reprezentacji gracza
     */
    static double colliderHeight;

    /**
     * Menedżer animacji klasy - przechowuje animacje poruszania się gracza
     * w ośmiu kierunkach
     */
    private AnimationManager walkingAnimations;

    /**
     * Zmienne pomocnicze, mówiące czy gracz porusza się w wybranym kierunku - reagują na sygnały z klawiatury
     */
    public boolean left, right, up, down;

    /**
     * Czas niesmiertelnosci gracza liczony w klatkach
     */
    private static int immortalTime;

    /**
     * Maksymalna liczba żyć
     */
    private static int maxLives;

    /**
     * Flaga mówiąca o tym, czy gracz jest nieśmiertelny
     */
    private boolean immortal = false;

    /**
     * Flaga mówiąca o tym, czy prędkość gracza została zwiększona
     */
    private static boolean movementSpeedIncreased = false;

    /**
     * Ikona serduszka, jednego z przedmiotów, używana do
     * wyświetlania paska życia
     */
    private ImageIcon heartCollectableIcon;

    /**
     * Znacznik czasowy używany do określenia okresu nieśmiertelności
     */
    private int endFrameCount = 0;

    /**
     * Przypisuje wartości zmiennym. Wywołuje {@link #setAnimations()}
     * @param level Referencja do obecnego poziomu gry
     */
    public Player(Level level)
    {
        super(level);
        setAnimations();
        heartCollectableIcon = Utils.collectableFrames.get("heart");
        lives = 3;
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
                FileInputStream fileInputStream = new FileInputStream("./data/info/entities/player.properties");
                properties.load(fileInputStream);
            } catch (IOException e) {
                System.out.println("Nie udalo sie odnalezc pliku .properties klasy Player");
            }
        }
        else {
            properties = serverProperties;
        }

        try
        {
            colliderWidth = GameManager.DEFAULT_TILE_WIDTH * Double.parseDouble(properties.getProperty("colliderWidthRatio"));
            colliderHeight = GameManager.DEFAULT_TILE_WIDTH * Double.parseDouble(properties.getProperty("colliderHeightRatio"));
            movementSpeed = Double.parseDouble(properties.getProperty("movementSpeed"));
            immortalTime = Integer.parseInt(properties.getProperty("immortalTime"));
            maxLives = Integer.parseInt(properties.getProperty("maxLives"));
        } catch(IllegalArgumentException e) {
            System.out.println("Wystapil problem przy parsowaniu pliku .properties klasy Player");
        }
    }



    /**
     * Ustala współrzędne gracza
     * @param x Składowa x pozycji
     * @param y Składowa y pozycji
     */
    public void setCoords(int x, int y)
    {
        position = Utils.coordsToPosition(x, y);
    }

    /**
     * Dodaje animacje do menedżera. Wykorzystuje zmienną {@link Utils#playerFrames}
     */
    private void setAnimations()
    {
        int animationSpeed = Integer.parseInt(properties.getProperty("animationSpeed"));
        walkingAnimations = new AnimationManager();
        ImageIcon[][] arr = Utils.playerFrames;
        walkingAnimations.addAnimation(arr[0], animationSpeed, "Down");
        walkingAnimations.addAnimation(arr[1], animationSpeed, "Left");
        walkingAnimations.addAnimation(arr[2], animationSpeed, "Right");
        walkingAnimations.addAnimation(arr[3], animationSpeed, "Up");
        walkingAnimations.addAnimation(arr[4], animationSpeed, "DownLeft");
        walkingAnimations.addAnimation(arr[5], animationSpeed, "UpLeft");
        walkingAnimations.addAnimation(arr[6], animationSpeed, "DownRight");
        walkingAnimations.addAnimation(arr[7], animationSpeed, "UpRight");
    }

    /**
     * Metoda odpowiedzialna za poruszanie się. Bazując na zmiennych {@link #left}, {@link #right}, {@link #up} i {@link #down}
     * ustala kierunek poruszania się gracza. Następnie sprawdza, czy ustalona zmiana pozycji
     * jest prawidłowa (nie wchodzi w przeszkody). Jeśli jest, dodaje zmianę do pozycji.
     */
    public void move()
    {
        if (!left && !right && !down && !up)
            return;

        Vector velocity = new Vector(0, 0);
        if (left)
            velocity.x = -1;
        if (right)
            velocity.x = 1;
        if (down)
            velocity.y = 1;
        if (up)
            velocity.y = -1;

        velocity.setMag(movementSpeed);

        String[] animationKeys = { "Right", "DownRight", "Down", "DownLeft", "Left", "UpLeft", "Up", "UpRight" };
        double orientation = velocity.heading() >= 0 ? velocity.heading() : velocity.heading() + Math.PI * 2;
        int index = (int)Math.floor((orientation / (Math.PI * 2)) * animationKeys.length);
        walkingAnimations.setCurrentAnimation(animationKeys[index]);

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
                if (map[x][y].collideRect(position.copy().add(new Vector(0, velocity.y)), colliderWidth, colliderHeight, true))
                    isMoveLegalY = false;
            }
        }

        if (isMoveLegalX)
            position.x += velocity.x;
        if (isMoveLegalY)
            position.y += velocity.y;

        walkingAnimations.step();
    }

    /**
     * Wyświetla obecną klatkę animacji
     * @param g Obiekt, na którym rysowane są obrazki
     */
    public void display(Graphics2D g)
    {
        if(((endFrameCount - GameManager.frameCount)%8 > 4 && immortal) || !immortal || currentLevel.gamePanel.timeManager.paused){
            ImageIcon currentFrame = walkingAnimations.getFrame();
            int posCornerX = (int)(position.x - currentFrame.getIconWidth() / 2);
            int posCornerY = (int)(position.y - currentFrame.getIconHeight() / 2);
            currentFrame.paintIcon(currentLevel.gamePanel, g, posCornerX, posCornerY);
        }

        int x = 500;
        for (int i = 0; i<lives ;i++){
            heartCollectableIcon.paintIcon(currentLevel.gamePanel, g, x,0);
            x = x + 50;
        }

    }

    /**
     * Metoda sprawdzająca kolizję przeciwnika z graczem
     * @param enemy obiekt klasy {@code Enemy} - dany przeciwnik
     * @return informacja czy zaszła kolizja z przeciwnikiem typu {@code boolean}
     */
    public boolean collideEnemies(Enemy enemy){
        return this.collideEntity(enemy.position,Player.colliderWidth,Player.colliderHeight);
    }

    /**
     * Metoda sprawdzająca kolizję ze znajdzką
     * @param collectable obiekt klasy {@code Collectable} - dana znajdzka
     * @return informacja czy zaszła kolizja ze znajdzką typu {@code boolean}
     */
    public boolean collideCollectable(Collectable collectable){
        return collectable != null && collectable.collideEntity(this.position, Player.colliderWidth, Player.colliderHeight);
    }

    /**
     * Metoda obsługująca dodawanie życia graczowi
     */
    public  void addLife(){
        if(lives < maxLives)
            lives++;
    }

    /**
     * Metoda obsługująca zwiększanie predkości gracza pod wpływem znajdzki
     */
    public static void increaseMovementSpeed(){
        if (!movementSpeedIncreased){
            movementSpeed++;
            movementSpeedIncreased = true;
        }
    }

    /**
     * Metoda ustawiająca stan niesmiertelnosci gracza
     * @param status informacja {@code boolean} czy gracz ma byc niesmiertelny
     * @param frameCount liczba klatek gry
     */
    public void setImmortal(boolean status,int frameCount){
        if (status) {
            this.endFrameCount = frameCount + immortalTime;
        }
        immortal = status;
    }

    /**
     * Metoda ustawiająca smiertelnosc gracza
     */
    public void setMortal() {
        immortal = false;
    }

    /**
     * Getter
     * @return {@link #endFrameCount}
     */
    public int getEndFrameCount() {
        return endFrameCount;
    }

    /**
     * Metoda obsługująca tracenie życia przez gracza
     * @param frameCount liczba klatek gry
     */
    public void loseLife(int frameCount){
        if (!immortal){
            super.loseLife();
            setImmortal(true,frameCount);
        }
    }

    /**
     * Metoda typu getter.
     * @return Zwraca {@code boolean} zawierającego informację czy gracz jest niesmiertelny
     */
    public boolean isImmortal() {
        return immortal;
    }
}
