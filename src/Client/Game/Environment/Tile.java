package Client.Game.Environment;

import Client.Game.Managers.Level;
import Client.Game.Managers.GameManager;
import Client.Game.Utilities.Vector;
import Client.Utils;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Klasa reprezentuąca komórkę planszy
 */
public class Tile
{
    /**
     * Właściwości klasy
     */
    private static Properties properties;

    /**
     * Limit bomb, które mogą być obecne na mapie jednocześnie
     */
    private static int bombLimit;

    /**
     * Ilość bomb, które obecnie znajdują się na mapie
     */
    private static int bombsCurrently = 0;

    /**
     * Referencja do obecnego poziomu gry
     */
    private Level currentLevel;

    /**
     * Składowa x pozycji komórki na siatce komórek mapy
     */
    private int x;

    /**
     * Składowa y pozycji komórki na siatce komórek mapy
     */
    private int y;

    /**
     * Typ komórki
     */
    private TileType tileType;

    /**
     * Referencja do bomby, która może znajdować się na tej komórce
     */
    private Bomb bomb = null;

    /**
     * Referencja do eksplozji, która może znajdować się na tej komórce
     */
    private Explosion explosion = null;

    /**
     * Referencja do znajdzki, ktora moze znajdowac się na tej komórce
     */
    private Collectable collectable = null;

    /**
     * Znacznik czasowy (w klatkach gry), mówi kiedy następna eksplozja ma wystąpić na tej komórce
     */
    private int explosionTS = -1;

    /**
     * Mówi, czy komorka może zawierac znajdzke
     */
    boolean boostable;

    /**
     * Klasa reprezentuąca typ komórki planszy
     * @param x Składowa x pozycji komórki na siatce komórek mapy
     * @param y Składowa y pozycji komórki na siatce komórek mapy
     * @param tileType Typ komórki
     * @param currentLevel Referencja do obecnego poziomu gry
     */
    public Tile(int x, int y, TileType tileType, Level currentLevel)
    {
        this.x = x;
        this.y = y;
        this.currentLevel = currentLevel;
        this.tileType = tileType;
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
                FileInputStream fileInputStream = new FileInputStream("./data/info/environment/tile.properties");
                properties.load(fileInputStream);
            } catch (IOException e) {
                System.out.println("Nie udalo sie odnalezc pliku .properties klasy Tile");
            }
        }
        else{
            properties = serverProperties;
        }

        try {
            bombLimit = Integer.parseInt(properties.getProperty("bombLimit"));
        } catch(IllegalArgumentException e) {
            System.out.println("Wystapil problem przy parsowaniu pliku .properties klasy Tile");
        }
    }

    /**
     * Wyświetla obraz tej komórki.
     * Jeśli zmienne {@link #bomb} lub {@link #explosion} nie są równe {@code null},
     * wywołuje metody je wyświetlające
     * @param g Obiekt, na którym rysowane są obrazki
     */
    public void display(Graphics g)
    {
        if (GameManager.frameCount == explosionTS)
        {
            explosion = new Explosion(this);
            if (tileType.destructible) {
                tileType = Utils.tileTypes.get(0);
                this.boostable = true;
            }
        }

        if(tileType.texture == null)
            return;
        tileType.texture.paintIcon(currentLevel.gamePanel, g, getX() * GameManager.DEFAULT_TILE_WIDTH, getY() * GameManager.DEFAULT_TILE_WIDTH);

        if(bomb != null)
            bomb.display(g);

        if(explosion != null)
            explosion.display(g);
    }

    /**
     * Określa, czy jednostka koliduje z komórką
     * @param entityPos Pozycja jednostki
     * @param colliderWidth Szerokość fizycznej reprezentacji jednostki
     * @param colliderHeight Wysokość fizycznej reprezentacji jednostki
     * @param physicalCollision Mówi, czy ma to być kolizja fizyczna. Koliza niefizyczna to np.
     *                          kolizja gracza z komórką na której jest eksplozja.
     * @return Wartość logiczna kolizji
     */
    public boolean collideRect(Vector entityPos, double colliderWidth, double colliderHeight, boolean physicalCollision)
    {
        if (physicalCollision && !tileType.obstacle && !tileType.destructible)
            return false;

        float r1x = getX() * GameManager.DEFAULT_TILE_WIDTH, r1y = getY() * GameManager.DEFAULT_TILE_WIDTH;
        float r1w = GameManager.DEFAULT_TILE_WIDTH, r1h = GameManager.DEFAULT_TILE_WIDTH;

        double r2x = entityPos.x - colliderWidth / 2, r2y = entityPos.y - colliderHeight / 2;
        double r2w = colliderWidth, r2h = colliderHeight;

        return (r1x + r1w >= r2x && r1x <= r2x + r2w && r1y + r1h >= r2y && r1y <= r2y + r2h);
    }

    /**
     * Kładzie bombę na komórce
     */
    public void plantBomb()
    {
        if (tileType.obstacle || bomb != null || bombsCurrently >= bombLimit)
            return;

        bomb = new Bomb(this);
        this.getCurrentLevel().gamePanel.getGameManager().getMainActivity().getUiManager().getSoundManager().playBombSound();
        bombsCurrently++;
    }

    /**
     * Usuwa bombę z komórki
     */
    void killBomb()
    {
        if (bomb == null)
            return;

        bomb = null;
        bombsCurrently--;
    }

    /**
     * Tworzy eksplozję w danej komórce. Wywołuje {@link #explode1D(int, int, int, int, int)}
     * dla komóek po czterech stronach od obecnej
     * @param ogX Składowa x początkowej pozycji eksplozji
     * @param ogY Składowa y początkowej pozycji eksplozji
     * @param range Zasięg eksplozji w liczbie komórek
     */
    void explodeCenter(int ogX, int ogY, int range)
    {
        explosion = new Explosion(this);
        this.getCurrentLevel().gamePanel.getGameManager().getMainActivity().getUiManager().getSoundManager().playExplosionSound();
        currentLevel.map[x - 1][y].explode1D(-1, 0, ogX, ogY, range);
        currentLevel.map[x + 1][y].explode1D(1, 0, ogX, ogY, range);
        currentLevel.map[x][y - 1].explode1D(0, -1, ogX, ogY, range);
        currentLevel.map[x][y + 1].explode1D(0, 1, ogX, ogY, range);
    }

    /**
     * Rozprzestrzenia eksplozję w jednym kierunku, do momentu aż wyjdzie poza zasięg
     * @param dx Składowa x prędkości rozchodzenia się eksplozji
     * @param dy Składowa y prędkości rozchodzenia się eksplozji
     * @param ogX Składowa x początkowej pozycji eksplozji
     * @param ogY Składowa y początkowej pozycji eksplozji
     * @param range Zasięg eksplozji w liczbie komórek
     */
    private void explode1D(int dx, int dy, int ogX, int ogY, int range)
    {
        int maxDist = Math.max(Math.abs(x - ogX), Math.abs(y - ogY));

        if ((tileType.obstacle && !tileType.destructible) || maxDist > range)
            return;

        explosionTS = GameManager.frameCount + maxDist * Explosion.delay;

        if (tileType.destructible)
            return;

        currentLevel.map[x + dx][y + dy].explode1D(dx, dy, ogX, ogY, range);
    }

    /**
     * Usuwa eksplozję z komórki
     */
    void extinguish()
    {
        if (explosion != null)
            explosion = null;
    }

    /**
     * Getter zmiennej {@link #x}
     * @return Zmienna {@link #x}
     */
    public int getX() {
        return x;
    }

    /**
     * Getter zmiennej {@link #y}
     * @return Zmienna {@link #y}
     */
    public int getY() {
        return y;
    }

    /**
     * Getter zmiennej {@link #currentLevel}
     * @return Zmienna {@link #currentLevel}
     */
    public Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Getter zmiennej {@link #tileType}
     * @return Zmienna {@link #tileType}
     */
    public TileType getTileType() {
        return tileType;
    }

    /**
     * Getter zmiennej {@link #explosion}
     * @return Zmienna {@link #explosion}
     */
    public Explosion getExplosion() {
        return explosion;
    }

    /**
     * Getter znajdzki
     * @return {@link #collectable}
     */
    public Collectable getCollectable() {
        return collectable;
    }

    /**
     * Metoda ustawia znajdzkę dla danej komorki
     * @param collectable znajdzka do ustawienia
     */
    public void setCollectable(Collectable collectable) {
        this.collectable = collectable;
    }

}
