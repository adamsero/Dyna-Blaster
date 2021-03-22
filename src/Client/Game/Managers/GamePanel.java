package Client.Game.Managers;

import Client.ClientConnection;
import Client.Game.Environment.Collectable;
import Client.Game.Utilities.FadingLabel;
import Client.Game.Utilities.Vector;
import Client.Main;
import Client.UI.CustomLabel;
import Client.Game.Entities.Enemy;
import Client.Game.Utilities.GameKeyListener;
import Client.Game.Entities.Player;
import Client.Utils;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Klasa obsługująca aktualizaje oraz wyświetlanie elementów gry
 */
public class GamePanel extends JPanel
{
    /**
     * Referncja do aktualnego poziomu gry
     */
    private Level currentLevel;

    /**
     * Numer obecnego poziomu
     */
    private int levelNumber;

    /**
     * Referencja do klasy {@link Client.Game.Managers.GameManager}, nadrzędnej w hierarchii
     */
    private GameManager gameManager;

    /**
     * Instancja menedżera czasu
     */
    public TimeManager timeManager;

    /**
     * Etykieta wyświetlająca informacje o czasie, który upłynął od początku gry
     */
    private String timeLabel;

    /**
     * Zmienna typu {@code String} przechowująca informację o aktualnym wyniku gry
     */
    private String scoreLabel;

    /**
     * Lista typu {@code ArrayList} przechowywująca animacje rozmywających się napisów
     */
    private final ArrayList<FadingLabel> killScores = new ArrayList<>();

    /**
     * Zmienna odpowiadająca za przechwywanie wyniku gry
     */
    private double gameScore;

    /**
     * Znacznik czasowy momentu, do którego trwa combo
     */
    private int comboLifeTS;

    /**
     * Mówi, który poziom comba osiągnął gracz
     */
    private int comboIndex;

    /**
     * Instancja klasy gracza
     */
    public Player player;

    /**
     * Lista przeciwników na poziomie
     */
    ArrayList<Enemy> enemies = new ArrayList<>();

    /**
     * Lista znajdziek na poziomie
     */
    private ArrayList<Collectable> collectables = new ArrayList<>();

    /**
     * Zawiera informacje o koncu gry
     */
    private boolean gameover = false;

    /**
     * Konstruktor tworzący panel gry i inicjalizujący pola klasy
     * @param w Szerokość panelu
     * @param h Wysokość panelu
     * @param gameManager Referencja menedżera gry
     */
    public GamePanel(int w, int h, GameManager gameManager)
    {
        setPreferredSize(new Dimension(w, h));
        setLayout(null);
        this.gameManager = gameManager;

        currentLevel = new Level(this);

        timeLabel = "CZAS: 00:00:00";
        scoreLabel = "WYNIK: " + (int)gameScore;

        GameKeyListener keyListener = new GameKeyListener(this);
        gameManager.gameWindow.addKeyListener(keyListener);

        resetGame(false);
    }

    /**
     * Metoda resetująca grę
     * @param loadLevel Mówi, czy wczytać poziom przy okazji resetu (prawda przy wywoływaniu dla pierwszego poziomu)
     */
    void resetGame(boolean loadLevel)
    {
        levelNumber = 1;
        gameScore = GameManager.INITIAL_POINTS;
        comboLifeTS = -1;
        if(loadLevel)
        {
            try {
                currentLevel.loadLevelFromFile(levelNumber);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        timeLabel = "CZAS: 00:00:00";
        scoreLabel = "WYNIK: " + (int)gameScore;
        timeManager = new TimeManager(1.0);
        gameover = false;
    }

    /**
     * Metoda wywołująca funkcje wyświetlające wszystkie elementy gry
     * @param g Zmienna, która umożliwia rysowanie po panelu
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Dimension gameWindowDimension = gameManager.gameWindow.getContentPane().getSize();
        double scaleX = gameWindowDimension.getWidth() / (GameManager.DEFAULT_TILE_WIDTH * currentLevel.mapWidth);
        double scaleY = gameWindowDimension.getHeight() / (GameManager.DEFAULT_TILE_WIDTH * currentLevel.mapHeight);
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.scale(scaleX, scaleY);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        currentLevel.displayLevel(g2d);
        player.display(g2d);
        for(Enemy enemy : enemies)
            enemy.display(g2d);

        for (Collectable collectable : collectables)
            collectable.display(g2d);

        displayLabels(g2d);

        if (this.isVisible() && !(this.timeManager.paused)){
            updateGameTime();
        }

        if(gameover){
            showGif(g2d);
        }
    }

    /**
     * Metoda aktualizująca elementy gry
     */
    public void update() {
        //System.out.println(gameScore + "\tCombo " + (comboIndex == 0 ? "OFF" : "ON"));
        if (!timeManager.paused) {
            player.move();

            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy enemy = enemies.get(i);
                if(player.collideEnemies(enemy)){
                    if (!player.isImmortal()) {
                        this.getGameManager().getMainActivity().getUiManager().getSoundManager().playHitSound();
                    }
                    player.loseLife(GameManager.frameCount);
                }
                if (enemy.collideExplosion(enemy.currentTile()))
                {
                    enemy.setDead();
                    this.getGameManager().getMainActivity().getUiManager().getSoundManager().playHitSound();
                }

                enemy.move(player);
                if (enemy.isDead()) {
                    enemies.remove(i);

                    double deltaScore = enemy.getPointValue() * (1d + comboIndex * GameManager.COMBO_MULTIPLIER);
                    killScores.add(new FadingLabel("+" + (int)deltaScore, (int)enemy.position.x, (int)enemy.position.y));

                    comboLifeTS = GameManager.frameCount + GameManager.COMBO_DURATION;
                    gameScore += deltaScore;
                    comboIndex++;
                }
            }

            if(GameManager.frameCount == comboLifeTS)
                comboIndex = 0;

            if(player.collideCollectable(player.currentTile().getCollectable())){
                player.currentTile().getCollectable().collect();
            }

            if(player.collideExplosion(player.currentTile())){
                if (!player.isImmortal()) {
                    this.getGameManager().getMainActivity().getUiManager().getSoundManager().playHitSound();
                }
                player.loseLife(GameManager.frameCount);
            }

            if (GameManager.frameCount >= player.getEndFrameCount()){
                player.setImmortal(false,0);
            }

            if (player.getLives() == 0){
                if (!player.isDead()){
                    player.setDead(true);
                    //gameManager.getMainActivity().getUiManager().soundManager.playGameOverTheme();
                    gameover = true;
                    endGame(false);
                    timeManager.pause();
                }
            }
        }
    }

    /**
     * Metoda wyświetlająca napisy informacyjne na planszy gry
     * @param g2d Obiekt klasy {@code Graphics2D}
     */
    private void displayLabels(Graphics2D g2d) {
        Font pixelFont = Utils.createCustomFont("Typecast.ttf", 35, Font.PLAIN);
        g2d.setFont(pixelFont);
        g2d.setColor(Color.WHITE);
        Vector textPos = new Vector(10, 25);
        g2d.drawString(timeLabel, (int)textPos.x, (int)textPos.y);

        textPos = new Vector(210, 25);
        g2d.drawString(scoreLabel, (int)textPos.x, (int)textPos.y);

        for(int i = killScores.size() - 1; i >= 0; i--) {
            killScores.get(i).displayAndUpdate(g2d, pixelFont);
            if(killScores.get(i).isDead())
                killScores.remove(i);
        }

        if(timeManager.paused) {
            g2d.setColor(Color.RED);
            pixelFont = Utils.createCustomFont("Typecast.ttf", 70, Font.PLAIN);
            textPos = new Vector(GameManager.INITIAL_WINDOW_WIDTH / 2d, GameManager.INITIAL_WINDOW_HEIGHT / 2d);
            Utils.drawCenteredString(g2d, "PAUZA", textPos, pixelFont);
        }
    }

    /**
     * Metoda aktualizująca licznik czasu
     */
    private void updateGameTime(){
        if(timeManager.getStartTime() != null){
            String currentGameTime = LocalTime.MIDNIGHT.plus(timeManager.calculateCurrentGameTime()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            double gameTime = timeManager.calculatePointsToSubtractDuringGame();
            timeLabel = "CZAS: " + currentGameTime;
            scoreLabel = "WYNIK: " + ((int)gameScore - (int)gameTime);
        }
    }

    /**
     * Metoda obsługująca przechodzenie na następny poziom
     */
    void nextLevel()
    {
        levelNumber++;
        if(levelNumber > GameManager.LEVEL_COUNT) {
            endGame(true);
            timeManager.pause();
            return;
        }

        try {
            currentLevel.loadLevelFromFile(levelNumber);
        } catch (FileNotFoundException e) { e.printStackTrace(); }
    }

    /**
     * Metoda obslugująca koniec gry
     * @param victory przyjmuje informację o zwycięstwie
     */
    private void endGame(boolean victory)
    {
        timeManager.stop();
        double totalPoints = gameScore - timeManager.calculatePointsToSubtract();
        String result = victory ? "Wygrana! " : "Porażka. ";
        String text = result + "Twój wynik: " + (int)totalPoints;
        gameManager.getMainActivity().getUiManager().setGameResult(text);
        if(victory){

            if (Utils.getOnlineGaming()){
                //wysylka wyniku na serwer
                ClientConnection clientConnection = Main.instance.clientConnection;
                clientConnection.writeAndFlush("UPDATE Highscore",new Pair(Main.instance.getUiManager().getNick(),String.valueOf((int)totalPoints)));
            }
            else{
                //zapis lokalnie
                saveScoreLocal(Main.instance.getUiManager().getNick(),String.valueOf((int)totalPoints));
            }
        }
    }

    /**
     * Metoda obsługująca wyswietlanie informacji o końcu gry - porażce
     */
    public void showEndGameInfo(){
        //timeManager.pause();
        this.gameover = true;
    }

    /**
     * Getter do listy znajdziek
     * @return lista znajdziek
     */
    ArrayList<Collectable> getCollectables() {
        return collectables;
    }

    /**
     * Metoda pokazująca gif w przypadku porażki
     * @param g2d obiekt typu Graphics2D używany do rysowania
     */
    private void showGif(Graphics2D g2d){
        ImageIcon gameover = new ImageIcon("./data/img/gameover/gameover.gif");
        gameover.paintIcon(this,g2d,GameManager.INITIAL_WINDOW_HEIGHT/2 - gameover.getIconWidth()/2,GameManager.INITIAL_WINDOW_WIDTH/2 - gameover.getIconHeight()/2);
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    /**
     * Metoda zapisująca loklnie wynik gry do pliku
     * @param name {@code String} imię gracza
     * @param result {@code String} wynik gracza bedący liczbą zapisaną w postaci tekstowej
     */
    private void saveScoreLocal(String name, String result){
        ArrayList<Pair<String,String>> scores = loadScoresFromFile();

        if (scores != null){
            scores = sortScores(scores, new Pair<String, String>(name,result));

            try{
                File scoresFile = new File("./data/info/general/highScores.txt");
                scoresFile.delete();

                try {
                    PrintStream out = new PrintStream("./data/info/general/highScores.txt");

                    for (Pair<String,String> pair : scores){
                        out.println(pair.getKey() + " " + pair.getValue());
                    }
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    /**
     * Metoda ładująca z pliku listę najlepszych wyników
     * @return Zwraca {@code ArrayList<Pair>} zawierającą wyniki
     */
    private ArrayList<Pair<String,String>> loadScoresFromFile(){

        try {
            ArrayList<Pair<String,String>> scores = new ArrayList<>();
            Scanner scanner = new Scanner(new File("./data/info/general/highScores.txt"));

            String[] results;
            while(scanner.hasNextLine()){
                results = scanner.nextLine().split(" ");
                scores.add(new Pair<>(results[0],results[1]));
            }
            return scores;
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Metoda sortujaca - insertion sort - dodaje wynik na odpowiednim miejscu
     * @param scoreList {@code ArrayList<Pair<String,String>>} lista do której ma być dodany nowy wynik
     * @param pair {@code Pair<String,String>} parametr reprezentujacy wynik który ma być dodany do listy
     * @return {@code ArrayList<Pair<String,String>>} zwraca posortowaną listę wyników
     */
    private ArrayList<Pair<String,String>> sortScores(ArrayList<Pair<String,String>> scoreList,Pair<String,String> pair){
        int index = 0;
        ArrayList<Pair<String,String>> sorted = new ArrayList<>();
        boolean added = false;

        for (Pair<String,String> currentPair: scoreList){
            if (Integer.parseInt(currentPair.getValue()) >= Integer.parseInt(pair.getValue())){
                sorted.add(currentPair);
            }
            else if(!added){
                sorted.add(pair);
                sorted.add(currentPair);
                added = true;
            }
            else if(added){
                sorted.add(currentPair);
            }
        }

        //jako ostatni
        if(!added && sorted.size() < 10){
            sorted.add(pair);
            added = true;
        }
        return sorted;
    }
}
