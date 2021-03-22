package Client;

import Client.Game.Entities.Enemy;
import Client.Game.Entities.Player;
import Client.Game.Entities.SmartEnemy;
import Client.Game.Entities.StupidEnemy;
import Client.Game.Environment.*;
import Client.Game.Managers.GameManager;
import Client.Game.Utilities.Vector;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Klasa przechowywująca wszystkie metody pomocnicze oraz kontenery danych.
 */
public class Utils
{
    /**
     * Kontener przechowujący wszystkie możliwe typy komórek na mapie
     */
    public static HashMap<Integer, TileType> tileTypes = new HashMap<>();

    /**
     * Tablica z obrazkami animacji gracza
     */
    public static ImageIcon[][] playerFrames;

    /**
     * Tablica z obrazkami animacji jednego z typów przeciwników
     */
    public static ImageIcon[][] stupidEnemyFrames;

    /**
     * Tablica z obrazkami animacji jednego z typów przeciwników
     */
    public static ImageIcon[][] smartEnemyFrames;

    /**
     * Tablica z obrazkami animacji bomby
     */
    public static ImageIcon[] bombFrames;

    /**
     * Tablica z obrazkami animacji eksplozji
     */
    public static ImageIcon[] explosionFrames;

    /**
     * Tablica z obrazkami przediotów do podniesienia
     */
    public static HashMap<String,ImageIcon> collectableFrames;

    /**
     * Zmienna określająca czy gra ma być online
     */
    private final static AtomicBoolean onlineGaming = new AtomicBoolean();

    /**
     * Tworzy plik czcionki o wybranym rozmiarze i stylu
     * @param name Nazwa pliku typu TrueTypeFont w folderze z czcionkami
     * @param size Rozmiar czcionki
     * @param style Styl czcionki
     * @return {@code Font} Plik czcionki
     */
    public static Font createCustomFont(String name, int size, int style)
    {
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("./data/fonts/" + name))).deriveFont(style, size);
        }catch(FontFormatException | IOException e) {e.printStackTrace();}
        return font;
    }

    /**
     * Metoda wczytuje z pliku planszę/mapę gry
     * @throws IOException Występuje przy błędzie wczytywania plików
     * @throws IllegalArgumentException Występuje przy błędzie parsowania danych
     */
    public static void loadTileTypes() throws IOException, IllegalArgumentException
    {
        File[] files = new File("./data/info/tileTypes").listFiles();
        assert files != null;
        for(File file : files)
        {
            Properties tileProperties = new Properties();
            tileProperties.load(new FileInputStream(file));

            int ID = Integer.parseInt(tileProperties.getProperty("ID"));
            boolean obstacle = Boolean.parseBoolean(tileProperties.getProperty("obstacle"));
            boolean destructible = Boolean.parseBoolean(tileProperties.getProperty("destructible"));

            String name = tileProperties.getProperty("textureName");
            ImageIcon icon = new ImageIcon("./data/img/textures/" + name);
            ImageIcon scaledIcon = resizeImageIcon(icon, GameManager.DEFAULT_TILE_WIDTH, GameManager.DEFAULT_TILE_WIDTH);

            tileTypes.put(ID, new TileType(ID, scaledIcon, obstacle, destructible));
        }
    }

    /**
     * Wczytuje obrazki animacji gracza
     */
    public static void loadPlayerFrames()
    {
        int numberOfAnimations = Integer.parseInt(Player.properties.getProperty("numberOfAnimations"));
        int numberOfFrames = Integer.parseInt(Player.properties.getProperty("numberOfFrames"));
        double imageWidthRatio = Double.parseDouble(Player.properties.getProperty("imageWidthRatio"));
        double imageHeightRatio = Double.parseDouble(Player.properties.getProperty("imageHeightRatio"));

        playerFrames = new ImageIcon[numberOfAnimations][numberOfFrames];
        for (int i = 0; i < playerFrames.length; i++)
        {
            for (int j = 0; j < playerFrames[i].length; j++)
            {
                int index  = i * numberOfFrames + j;
                ImageIcon frame = new ImageIcon("./data/img/player/player" + index + ".png");
                ImageIcon resizedFrame = resizeImageIcon(frame, (int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
                playerFrames[i][j] = resizedFrame;
            }
        }
    }

    /**
     * Wczytuje obrazki animacji przeciwników
     */
    public static void loadEnemyFrames()
    {
        String[] directions = { "left", "right", "up", "down" };
        int numberOfAnimations = Integer.parseInt(Enemy.properties.getProperty("numberOfAnimations"));
        int numberOfFrames = Integer.parseInt(Enemy.properties.getProperty("numberOfFrames"));
        double imageWidthRatio = Double.parseDouble(Enemy.properties.getProperty("imageWidthRatio"));
        double imageHeightRatio = Double.parseDouble(Enemy.properties.getProperty("imageHeightRatio"));

        stupidEnemyFrames = new ImageIcon[numberOfAnimations][numberOfFrames];
        for (int i = 0; i < stupidEnemyFrames.length; i++)
        {
            for (int j = 0; j < stupidEnemyFrames[i].length; j++)
            {
                ImageIcon frame = new ImageIcon("./data/img/enemies/ghostWhite/" + directions[i] + j + ".png");
                ImageIcon resizedFrame = resizeImageIcon(frame, (int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
                stupidEnemyFrames[i][j] = resizedFrame;
            }
        }

        smartEnemyFrames = new ImageIcon[numberOfAnimations][numberOfFrames];
        for (int i = 0; i < smartEnemyFrames.length; i++)
        {
            for (int j = 0; j < smartEnemyFrames[i].length; j++)
            {
                ImageIcon frame = new ImageIcon("./data/img/enemies/ghostBlue/" + directions[i] + j + ".png");
                ImageIcon resizedFrame = resizeImageIcon(frame, (int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
                smartEnemyFrames[i][j] = resizedFrame;
            }
        }
    }

    /**
     * Wczytuje obrazki animacji bomby
     */
    public static void loadBombFrames()
    {
        int numberOfFrames = Integer.parseInt(Bomb.properties.getProperty("numberOfFrames"));
        double imageWidthRatio = Double.parseDouble(Bomb.properties.getProperty("imageWidthRatio"));
        double imageHeightRatio = Double.parseDouble(Bomb.properties.getProperty("imageHeightRatio"));

        bombFrames = new ImageIcon[numberOfFrames];
        for(int i = 0; i < bombFrames.length; i++)
        {
            ImageIcon frame = new ImageIcon("./data/img/bomb/bomb" + i + ".png");
            ImageIcon resizedFrame = resizeImageIcon(frame, (int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
            bombFrames[i] = resizedFrame;
        }
    }

    /**
     * Wczytuje obrazki animacji eksplozji
     */
    public static void loadExplosionFrames()
    {
        int numberOfFrames = Integer.parseInt(Explosion.properties.getProperty("numberOfFrames"));
        double imageWidthRatio = Double.parseDouble(Explosion.properties.getProperty("imageWidthRatio"));
        double imageHeightRatio = Double.parseDouble(Explosion.properties.getProperty("imageHeightRatio"));

        explosionFrames = new ImageIcon[numberOfFrames];
        for(int i = 0; i < explosionFrames.length; i++)
        {
            ImageIcon frame = new ImageIcon("./data/img/explosion/explosion" + i + ".png");
            ImageIcon resizedFrame = resizeImageIcon(frame, (int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
            explosionFrames[i] = resizedFrame;
        }
    }

    /**
     * Metoda ładująca obrazki znajdziek
     */
    public static void loadCollectableFrames(){

        double imageWidthRatio = Double.parseDouble(Collectable.properties.getProperty("imageWidthRatio"));
        double imageHeightRatio = Double.parseDouble(Collectable.properties.getProperty("imageHeightRatio"));

        collectableFrames = new HashMap<>();

        ImageIcon frame;

        frame = new ImageIcon("./data/img/collectable/boots.png");
        frame = resizeImageIcon(frame,(int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
        collectableFrames.put("boots",frame);

        frame = new ImageIcon("./data/img/collectable/gunpowder.png");
        frame = resizeImageIcon(frame,(int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
        collectableFrames.put("gunpowder",frame);

        frame = new ImageIcon("./data/img/collectable/heart.png");
        frame = resizeImageIcon(frame,(int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
        collectableFrames.put("heart",frame);

        frame = new ImageIcon("./data/img/collectable/portal.png");
        frame = resizeImageIcon(frame,(int)(GameManager.DEFAULT_TILE_WIDTH * imageWidthRatio), (int)(GameManager.DEFAULT_TILE_WIDTH * imageHeightRatio));
        collectableFrames.put("portal",frame);
    }

    /**
     * Wywołuje metody wczytujące właściwości (pliki .property) dla klas
     */
    static void loadAppProperties()
    {
        GameManager.loadProperties(null);
        Tile.loadProperties(null);
        Bomb.loadProperties(null);
        Explosion.loadProperties(null);
        Player.loadProperties(null);
        Enemy.loadProperties(null);
        Collectable.loadProperties(null);
    }

    static void getAppProperties(ClientConnection clientConnection)
    {
        clientConnection.writeAndRemember("GET Properties gameManager", null);
        clientConnection.writeAndRemember("GET Properties tile", null);
        clientConnection.writeAndRemember("GET Properties bomb", null);
        clientConnection.writeAndRemember("GET Properties explosion", null);
        clientConnection.writeAndRemember("GET Properties player", null);
        clientConnection.writeAndRemember("GET Properties enemy", null);
        clientConnection.writeAndRemember("GET Properties collectable", null);
        clientConnection.flushBuffer();
    }

    /**
     * Zwraca koordynaty najbliższego od podanych koordynatów punktu siatki
     * @param x Składowa x oryginalnych koordynatów
     * @param y Składowa y oryginalnych koordynatów
     * @return Koordynaty najbliższego punktu siatki
     */
    public static Vector coordsToPosition(int x, int y)
    {
        return new Vector((x + 0.5) * GameManager.DEFAULT_TILE_WIDTH, (y + 0.5) * GameManager.DEFAULT_TILE_WIDTH);
    }

    /**
     * Zwraca odległość pomiędzy dwoma punktami
     * @param a Składowa x pierwszego punktu
     * @param b Składowa y pierwszego punktu
     * @param c Składowa x drugiego punktu
     * @param d Składowa y drugiego punktu
     * @return Odległość między punktami
     */
    public static double dist(double a, double b, double c, double d)
    {
        return Math.sqrt((c - a) * (c - a) + (d - b) * (d - b));
    }

    /**
     * Przeskalowuje obraz
     * @param source Oryginalny obraz
     * @param desiredWidth Żądana szerokość
     * @param desiredHeight Żądana wysokość
     * @return Przeskalowany obraz
     */
    private static ImageIcon resizeImageIcon(ImageIcon source, int desiredWidth, int desiredHeight)
    {
        return new ImageIcon((source.getImage().getScaledInstance(desiredWidth, desiredHeight, Image.SCALE_SMOOTH)));
    }

    /**
     * Generuje tablicę liczb losowych bez powtórzeń
     * @param n Wielkość tablicy
     * @param lim Górne ograniczenie przedziału losowania (dolne to 0)
     * @return Tablica liczb losowych
     */
    public static int[] noRepGen(int n, int lim)
    {
        Random random = new Random();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++)
        {
            int rand;
            boolean ok;
            do
            {
                rand = (int)Math.floor(random.nextDouble() * lim);
                ok = true;
                for (int j = 0; j < i; j++)
                    if (rand == arr[j])
                    {
                        ok = false;
                        break;
                    }
            }while (!ok);

            arr[i] = rand;
        }
        return arr;
    }

    /**
     * Metoda rysująca napis na środku
     * @param g Obiekt klasy {@code Graphics}
     * @param text Tekst do wyświetlenia
     * @param v Wektor reprezentujący pozycję środka tekstu
     * @param font Obiekt reprezentujący czcionkę tekstu
     */
    public static void drawCenteredString(Graphics g, String text, Vector v, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        g.setFont(font);
        g.drawString(text, (int)(v.x - metrics.stringWidth(text) / 2), (int)(v.y + metrics.getHeight() / 4));
    }

    /**
     * Getter pola {@link #onlineGaming} zwracający informację czy gra jest w trybie online
     * @return {@code boolean}
     */
    public static boolean getOnlineGaming() {
        return onlineGaming.get();
    }

    /**
     * Setter pola {@link #onlineGaming}
     * @param flag {@code boolean} wartosc trybu gry jaka ma byc ustawiona
     */
    public static void setOnlineGaming(boolean flag) {
        onlineGaming.set(flag);
    }
}
