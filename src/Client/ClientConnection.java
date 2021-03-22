package Client;

import Client.Game.Entities.Enemy;
import Client.Game.Entities.Player;
import Client.Game.Environment.Bomb;
import Client.Game.Environment.Collectable;
import Client.Game.Environment.Explosion;
import Client.Game.Environment.Tile;
import Client.Game.Managers.GameManager;
import Client.Game.Managers.Level;
import Client.UI.CustomTextArea;
import javafx.util.Pair;

import java.io.*;
import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Klasa reprezentująca klienta do łączenia się z serwerem
 */
public class ClientConnection implements Runnable
{
    /**
     * Zmienna reprezentująca gniazdko TCP
     */
    private final Socket socket;

    /**
     * Zmienna reprezentująca obiektowy strumień wyjściowy
     */
    private ObjectOutputStream objectOutputStream;

    /**
     * Zmienna reprezentująca obiektowy strumień wejściowy
     */
    private ObjectInputStream objectInputStream;

    /**
     * Pole zawierające informację o ilości wysłanych żądań
     */
    final AtomicInteger requestsSent = new AtomicInteger();

    /**
     * Pole informujące czy otrzymano poziom od serwera
     */
    public final AtomicBoolean levelReceived = new AtomicBoolean();

    /**
     * Pole informujące czy otrzymano listę wynikiów od serwera
     */
    public final AtomicBoolean highScoresRecived = new AtomicBoolean();

    /**
     * Konstruktor
     * @param ipAddress adres ip na którym nasłuchuje serwer
     * @param port port na którym nasłuchuje serwer
     * @throws IOException w przypadku wyjątku strumieniowego
     */
    ClientConnection(String ipAddress, int port) throws IOException
    {
        socket = connectToServer(ipAddress, port);
        try
        {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda rozpoczynajaca faktyczną pracę wątku obsługi połączenia z serwerem
     */
    @Override
    public void run()
    {
        while(true)
        {
            try {
                receiveData();
                Thread.sleep(25);
            } catch(Exception e) {
                try {
                    socket.close();
                    Utils.setOnlineGaming(false);
                    JOptionPane.showMessageDialog(null,"Połączenie z serwerem zostało utracone!\nNastąpi przejście w tryb offline.", "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                //e.printStackTrace();
            }
        }
    }

    /**
     * Metoda odbierająca dane ze strumienia wejściowego
     * @throws IOException wyjątek związany z błędem strumienia wejścia wyjścia
     * @throws ClassNotFoundException wyjątek związany z błędem rzutowania
     */
    private void receiveData() throws IOException, ClassNotFoundException
    {
        Pair<String, Object> pair = (Pair<String, Object>)objectInputStream.readObject();
        manageReceivedData(pair);
    }

    /**
     * Metoda zarządzająca odebranymi danymi
     * @param pair {@code Pair<String,Object>} argument będący odebraną od serwera odpowiedzą
     */
    private void manageReceivedData(Pair<String, Object> pair)
    {
        String key = pair.getKey();
        String[] keyWords = key.split(" ");

        if(keyWords[0].equals("SEND") && keyWords[1].equals("Properties"))
        {
            Properties properties = (Properties)pair.getValue();

            switch(keyWords[2])
            {
                case "gameManager":
                    GameManager.loadProperties(properties);
                    break;
                case "tile":
                    Tile.loadProperties(properties);
                    break;
                case "bomb":
                    Bomb.loadProperties(properties);
                    break;
                case "explosion":
                    Explosion.loadProperties(properties);
                    break;
                case "player":
                    Player.loadProperties(properties);
                    break;
                case "enemy":
                    Enemy.loadProperties(properties);
                    break;
                case "collectable":
                    Collectable.loadProperties(properties);
                    break;
            }

            requestsSent.decrementAndGet();
            if(requestsSent.get() == 0)
            {
                synchronized (requestsSent) {
                    requestsSent.notifyAll();
                }
            }
        }

        else if(keyWords[0].equals("SEND") && keyWords[1].equals("Level"))
        {
            ArrayList<String> lines = (ArrayList<String>)pair.getValue();
            Level.receivedLevelLines = lines;
            synchronized (levelReceived) {
                levelReceived.set(true);
                levelReceived.notifyAll();
            }
        }

        else if(keyWords[0].equals("SEND") && keyWords[1].equals("Leaderboard")){

            ArrayList<String> highScores = (ArrayList<String>)pair.getValue();

            if (highScores != null){

                    CustomTextArea scores = Main.instance.getUiManager().highScores;
                    scores.setText("");
                    int index = 1;
                    for (String s:highScores) {
                        scores.append(index++ + ". " + s + "\n");
                    }
                    highScoresRecived.set(true);
                    System.out.println("High Scores received");

                    synchronized (highScoresRecived) {
                        highScoresRecived.notifyAll();
                    }
            }
        }
    }

    /** Metoda wpisująca dane do buffora wysyłkowego strumienia wyjściowego
     * @param key nagłówek żądania typu {@code String}
     * @param value obiekt który ma być wysłany {@code Object}
     */
    public void writeToBuffer(String key, Object value)
    {
        try {
            //objectOutputStream.reset();
            objectOutputStream.writeObject(new Pair<>(key, value));
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Metoda wpisująca dane do buffora wysyłkowego strumienia wyjściowego i zapisująca informację że żądanie ma zostać obsłużone
     * @param key nagłówek żądania typu {@code String}
     * @param value obiekt który ma być wysłany {@code Object}
     */
    public void writeAndRemember(String key, Object value)
    {
        writeToBuffer(key, value);
        requestsSent.incrementAndGet();
    }

    /**
     * Opróżnienie buffora wysyłkowego strumienia wyjściowego
     */
    public void flushBuffer()
    {
        try {
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Metoda wpisująca dane do buffora wysyłkowego strumienia wyjściowego i opróżniająca buffor wysyłkowy strumienia wyjściowego
     * @param key nagłówek żądania typu {@code String}
     * @param value obiekt który ma być wysłany {@code Object}
     */
    public void writeAndFlush(String key, Object value)
    {
        writeToBuffer(key, value);
        flushBuffer();
    }

    /**
     * Metoda łącząca się z serwerem za pomocą gniazdka
     * @param ipAddress adres IP serwera {@code String}
     * @param port port na którym serwer nasłuchuje {@code int}
     * @return {@code Socket} Zwracana jest referencja na gniazdko poprzez które zostało ustanowione połączenia
     * @throws IOException w przypadku błędu strumienia wejścia/wyjścia
     */
    private Socket connectToServer(String ipAddress, int port) throws IOException
    {
        Socket socket = new Socket();
        boolean canExit;
        int maxNumberOfAttempts = 3;
        int i = 0;
        do
        {
            canExit = true;
            try {
                System.out.println("Connection attempt #" + ++i);
                socket.connect(new InetSocketAddress(ipAddress, port), 5000);
            }catch (Exception e) {
                canExit = false;
            }
        } while(!canExit && i < maxNumberOfAttempts);
        if(i == maxNumberOfAttempts) {
            throw new IOException("Maximum number of connection attempts has been reached");
        }

        System.out.println("Connected to the server successfully");

        return socket;
    }
}
