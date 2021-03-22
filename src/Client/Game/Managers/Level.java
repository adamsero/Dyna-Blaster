package Client.Game.Managers;

import Client.ClientConnection;
import Client.Game.Entities.Player;
import Client.Game.Entities.SmartEnemy;
import Client.Game.Entities.StupidEnemy;
import Client.Game.Environment.Bomb;
import Client.Game.Environment.Collectable;
import Client.Game.Environment.Tile;
import Client.Main;
import Client.Sound.SoundManager;
import Client.Utils;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ClientInfoStatus;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Klasa przechowująca mapę poziomów oraz ich wczytywanie z plików.
 */
public class Level
{
    /**
     * Referencja do klasy {@link Client.Game.Managers.GamePanel}, nadrzędnej w hierarchii
     */
    public GamePanel gamePanel;

    /**
     * Dwuwymiarowa tablica komórek reprezentująca mapę poziomu
     */
    public Tile[][] map;

    /**
     * Szerokość mapy wyrażona w ilości komórek
     */
    public int mapWidth;

    /**
     * Wysokość mapy wyrażona w ilości komórek
     */
    public int mapHeight;

    /**
     * Lista przechowywująca otrzymane dane konfiguracyjne dotyczące levelu
     */
    public static ArrayList<String> receivedLevelLines = null;

    /**
     * Konstruktor, ustala referencję do klasy {@link Client.Game.Managers.GamePanel}
     * @param gamePanel Referencja do klasy {@link Client.Game.Managers.GamePanel}
     */
    public Level(GamePanel gamePanel)
    {
        this.gamePanel = gamePanel;
    }

    void loadLevelFromFile(int levelIndex) throws FileNotFoundException
    {
        boolean readFromLocal = !Utils.getOnlineGaming();
        GameManager.isRunning = false;

        if(Utils.getOnlineGaming())
        {
            ClientConnection clientConnection = Main.instance.clientConnection;
            clientConnection.levelReceived.set(false);
            clientConnection.writeAndFlush("GET Level " + levelIndex, null);

            synchronized (clientConnection.levelReceived)
            {
                while(!clientConnection.levelReceived.get())
                {
                    try {
                        clientConnection.levelReceived.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(receivedLevelLines == null)
                readFromLocal = true;
            else
                loadLevel(receivedLevelLines, levelIndex);
        }

        if(readFromLocal && GameManager.LEVEL_COUNT >= levelIndex)
        {
            ArrayList<String> lines = new ArrayList<>();
            Scanner mapScanner = new Scanner(new File("./data/levels/" + levelIndex + ".txt"));
            while (mapScanner.hasNextLine())
                lines.add(mapScanner.nextLine());

            loadLevel(lines, levelIndex);
        }

        if (!GameManager.isRunning){
            GameManager.isRunning = true;
        }
    }

    /**
     * Wczytuje poziom z plików
     * @param lines Lista linijek danych z pliku poziomu
     * @param levelIndex Indeks pozimu
     * @throws IllegalArgumentException Występuje przy błędzie parsowania danych z pliku
     */
    void loadLevel(ArrayList<String> lines, int levelIndex) throws IllegalArgumentException
    {
        //wczytywanie komorek mapy
        String[] mapDimensions = lines.get(0).split(" ");
        mapWidth = Integer.parseInt(mapDimensions[0]);
        mapHeight = Integer.parseInt(mapDimensions[1]);
        map = new Tile[mapWidth][mapHeight];

        int y = 0;
        for(int i = 1; i < mapHeight + 1; i++)
        {
            char[] rowTiles = lines.get(i).toCharArray();
            for(int x = 0; x < rowTiles.length; x++)
                map[x][y] = new Tile(x, y, Utils.tileTypes.get(Character.getNumericValue(rowTiles[x])), this);
            y++;
        }

        //wczytywanie gracza
        if(levelIndex == 1) //jeśli to pierwszy poziom, zainicjalizuj zmienną
            gamePanel.player = new Player(this);
        String[] playerCoordsString = lines.get(lines.size() - 1).split(" ");
        gamePanel.player.setCoords(Integer.parseInt(playerCoordsString[0]), Integer.parseInt(playerCoordsString[1]));
        gamePanel.player.setMortal();

        //wczytywanie przeciwnikow
        gamePanel.enemies.clear();
        int enemyCount = Integer.parseInt(lines.get(mapHeight + 1));
        for(int i = mapHeight + 2; i < mapHeight + 2 + enemyCount; i++)
        {
            String[] enemyInfo = lines.get(i).split(" ");
            int posX = Integer.parseInt(enemyInfo[0]);
            int posY = Integer.parseInt(enemyInfo[1]);
            String type = enemyInfo[2];
            switch(type)
            {
                case "stupid":
                    gamePanel.enemies.add(new StupidEnemy(posX, posY, this));
                    break;
                case "smart":
                    gamePanel.enemies.add(new SmartEnemy(posX, posY, this));
                    break;
            }
        }

        //wczytywanie znajdziek
        gamePanel.getCollectables().clear();
        int collectibleCount = Integer.parseInt(lines.get(mapHeight + enemyCount + 2));
        for(int i = mapHeight + enemyCount + 3; i < mapHeight + enemyCount + 3 + collectibleCount; i++)
        {
            String[] collectableInfo = lines.get(i).split(" ");
            int posX = Integer.parseInt(collectableInfo[0]);
            int posY = Integer.parseInt(collectableInfo[1]);
            String type = collectableInfo[2];

            gamePanel.getCollectables().add(new Collectable(posX, posY, this, map[posX][posY], Utils.collectableFrames.get(type)){
                @Override
                public void collect(){
                    if(!this.collected){

                        SoundManager soundManager = gamePanel.getGameManager().getMainActivity().getUiManager().getSoundManager();

                        switch(type){
                            case "boots":
                                Player.increaseMovementSpeed();
                                soundManager.playCollectSound();
                                break;
                            case "gunpowder":
                                Bomb.increaseRange();
                                soundManager.playCollectSound();
                                break;
                            case "heart":
                                gamePanel.player.addLife();
                                soundManager.playCollectSound();
                                break;
                            case "portal":
                                gamePanel.nextLevel();
                                soundManager.playGateSound();
                                break;
                        }
                        this.collected = true;

                    }
                }
            });
        }
        assignCollectablestoTiles();
    }

    /**
     * Rysuje mapę, wyświetlając każdą komórkę oddzielnie w pętli
     * @param g Referencja do instancji klasy {@code Graphics} w panelu gry
     */
    void displayLevel(Graphics g)
    {
        for(int x = 0; x < mapWidth; x++)
            for(int y = 0; y < mapHeight; y++)
                map[x][y].display(g);
    }

    /**
     * Metoda ustawia referencje do znajdziek w odpowiednich komorkach
     */
    private void assignCollectablestoTiles(){
        for (Collectable collectable: this.gamePanel.getCollectables()) {
            collectable.currentTile().setCollectable(collectable);
        }
    }
}
