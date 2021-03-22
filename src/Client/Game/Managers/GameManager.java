package Client.Game.Managers;

import Client.Main;
import Client.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Klasa odpowiadająca za zarządzanie panelem gry.
 * Obsługuje wewnętrzny zegar gry, odpowiadający za pętlę gry
 */
public class GameManager implements Runnable
{
    /**
     * Domyślna (bez skalowania) szerokość komórki planszy
     */
    public static int DEFAULT_TILE_WIDTH;

    /**
     * Domyślna (bez skalowania) szerokość okna gry
     */
    static int INITIAL_WINDOW_WIDTH;

    /**
     * Domyślna (bez skalowania) wysokość okna gry
     */
    static int INITIAL_WINDOW_HEIGHT;

    /**
     * Zmienna mówiąca ile poziomów jest w grze
     */
    static int LEVEL_COUNT;

    /**
     * Zmienna mówiąca o mnożniku przy liczeniu punktów w systemie combo
     */
    static double COMBO_MULTIPLIER;

    /**
     * Zmienna mówiąca o mnożniku przy liczeniu punktów w systemie combo
     */
    static int COMBO_DURATION;

    /**
     * Zmienna mówiąca o początkowej ilośći punktów w każdej grze
     */
    static int INITIAL_POINTS;

    /**
     * Zmienna zliczająca sumę klatek gry od jej początku
     */
    public static int frameCount = 0;

    /**
     * Pożądana ilość kaltek na sekundę
     */
    private static int FRAME_RATE;

    /**
     * Średnia ilość kaltek na sekundę
     */
    private static double averageFrameRate = 0;

    /**
     * Określa, czy okno gry powinno się odświeżać. Sterowana przez widoczność okna gry;
     */
    static boolean isRunning = false;

    /**
     * Referencja do instancji klasy głównej
     */
    private Main mainActivity;

    /**
     * Instancja okna gry
     */
    JFrame gameWindow;

    /**
     * Instancja panelu gry
     */
    private GamePanel gamePanel;

    /**
     * Konstruktor przypisujący wartość zmiennej {@link #mainActivity}
     * @param mainActivity Referencja do instancji klasy głównej
     */
    public GameManager(Main mainActivity)
    {
        this.mainActivity = mainActivity;
    }

    /**
     * Wczytuje właściwości tej klasy z pliku
     * @param properties Właściowści pobrane z serwera, jeśli gra jest w trybie online - w
     *                          przeciwnym wypadku ma wartość null
     */
    public static void loadProperties(Properties properties)
    {
        if (properties == null){
            properties = new Properties();
            try
            {
                FileInputStream fileInputStream = new FileInputStream("./data/info/general/gameManager.properties");
                properties.load(fileInputStream);
            } catch (IOException e) {
                System.out.println("Nie udalo sie odnalezc pliku .properties klasy GameManager");
            }
        }

        try
        {
            DEFAULT_TILE_WIDTH = Integer.parseInt(properties.getProperty("DEFAULT_TILE_WIDTH"));
            INITIAL_WINDOW_WIDTH = Integer.parseInt(properties.getProperty("INITIAL_WINDOW_WIDTH"));
            INITIAL_WINDOW_HEIGHT = Integer.parseInt(properties.getProperty("INITIAL_WINDOW_HEIGHT"));
            FRAME_RATE = Integer.parseInt(properties.getProperty("FRAME_RATE"));
            LEVEL_COUNT = Integer.parseInt(properties.getProperty("LEVEL_COUNT"));
            COMBO_MULTIPLIER = Double.parseDouble(properties.getProperty("COMBO_MULTIPLIER"));
            COMBO_DURATION = Integer.parseInt(properties.getProperty("COMBO_DURATION"));
            INITIAL_POINTS = Integer.parseInt(properties.getProperty("INITIAL_POINTS"));
        } catch(IllegalArgumentException e) {
            System.out.println("Wystapil problem przy parsowaniu pliku .properties klasy GameManager");
        }
    }

    /**
     * Inicjalizuje, wywołując {@link #initialize()} oraz wywołuje {@link #doGameLoopIteration()} w pętli
     */
    @Override
    public void run()
    {
        initialize();

        long totalTimeElapsed = 0;
        double desiredFrameDuration = 1e3 / FRAME_RATE;
        while(true)
        {
            long startTime = System.nanoTime();

            if(isRunning)
                doGameLoopIteration();

            long finishTime = System.nanoTime();
            double frameDuration = (finishTime - startTime) / 1e6;
            long waitTime = (long)(desiredFrameDuration - frameDuration);

            if(waitTime > 0)
            {
                try {
                    Thread.sleep(waitTime);
                }catch (InterruptedException e){ e.printStackTrace(); }
            }

            totalTimeElapsed += System.nanoTime() - startTime;

            if(!gamePanel.timeManager.paused)
                frameCount++;

            if(frameCount % FRAME_RATE == 0)
            {
                averageFrameRate = 1e9 * FRAME_RATE / totalTimeElapsed;
                totalTimeElapsed = 0;
            }
        }
    }

    /**
     * Wywołuje {@link #loadAssets()} oraz {@link #createGameWindow()}
     */
    private void initialize()
    {
        isRunning = false;
        try {
            loadAssets();
        } catch (IOException e) { e.printStackTrace(); }
        createGameWindow();
    }

    /**
     * Wywołuje metody w klasie {@link Utils} ładujące pliki .property
     * @throws IOException Występuje przy błędach we wczytywaniu
     */
    private void loadAssets() throws IOException
    {
        Utils.loadTileTypes();
        Utils.loadPlayerFrames();
        Utils.loadEnemyFrames();
        Utils.loadBombFrames();
        Utils.loadExplosionFrames();
        Utils.loadCollectableFrames();
    }

    /**
     * Tworzy oraz ustala parametry okna gry
     */
    private void createGameWindow()
    {
        gameWindow = new JFrame("DynaBlaster");
        gameWindow.setSize(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
        gameWindow.setLocationRelativeTo(null);
        gameWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        gameWindow.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                gamePanel.timeManager = new TimeManager(1.0);
                mainActivity.openMenuWindow();
            }
        });

        gamePanel = new GamePanel(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT, this);
        gameWindow.add(gamePanel);
        gameWindow.pack();

        gameWindow.setVisible(false);
    }

    /**
     * Wywołuje metody odpowiedzialne za aktualizacje oraz wyświetlanie w panelu gry
     */
    private void doGameLoopIteration()
    {
        gamePanel.update();
        gamePanel.repaint();
    }

    /**
     * Zmienia widoczność okna oraz wartość zmeinnej {@link #isRunning} w zależności od stanu
     * @param state Mówi, czy okno ma być chowane czy odkrywane
     */
    public void setGameWindowVisibility(boolean state)
    {
        gameWindow.setVisible(state);
        if(state && !isRunning) {
            gamePanel.resetGame(true);
            System.out.println("Game reset");
            isRunning = true;
            gamePanel.timeManager.start();
        }
        else if(!state && isRunning) {
            isRunning = false;
        }
    }

    /**
     * Getter
     * @return zwraca referencję do obiektu {@code Main}
     */
    public Main getMainActivity(){
        return mainActivity;
    }

}
