package Server;

import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

/**
 * Klasa obsługująca serwerową część połączenia między serwerem a klientem. Działa na osobnym wątku.
 */
public class ServerConnection implements Runnable
{
    /**
     * Gniazdko obsługujące to połączenie
     */
    private final Socket socket;

    /**
     * Strumień obiektowy wyjścia
     */
    private ObjectOutputStream objectOutputStream;

    /**
     * Strumień obiektowy wejścia
     */
    private ObjectInputStream objectInputStream;

    /**
     * Getter gnaizdka
     * @return Gniazdko
     */
    Socket getSocket() {
        return socket;
    }

    /**
     * Przypisuje zmienną {@link #socket} oraz na jej podstawie inicjalizuje {@link #objectInputStream} oraz {@link #objectOutputStream}
     * @param socket Gniazdko generowane w metodzie akceptującej klientów w klasie głównej
     */
    ServerConnection(Socket socket)
    {
        this.socket = socket;
        try
        {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Czeka w pętli na odebranie danych od klienta
     */
    @Override
    public void run()
    {
        while(true)
        {
            try {
                receiveData();
            } catch(IOException | ClassNotFoundException e) {
                //tutaj jest SocketException jeśli objectOutputStream jest pusty
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Zapisuje dane do strumienia obiektowego
     * @param key Nagłówek wiadomości
     * @param value Ciało wiadomości
     */
    private void writeToBuffer(String key, Object value)
    {
        try {
            //objectOutputStream.reset();
            objectOutputStream.writeObject(new Pair<>(key, value));
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Przesyła dane przez gniazdko do klienta
     */
    private void flushBuffer()
    {
        try {
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zapisuje dane do strumienia i przesyła je przez gniazdko do klienta
     * @param key Nagłówek wiadomości
     * @param value Ciało wiadomości
     */
    private void writeAndFlush(String key, Object value)
    {
        writeToBuffer(key, value);
        flushBuffer();
    }

    /**
     * Odbiera dane i wywołuje {@link #manageReceivedData(Pair)}
     * @throws IOException Wyrzucany przy błędzie odczytywania danych ze strumienia wejściowego
     * @throws ClassNotFoundException Wyrzucany przy błędzie rzutowania klas
     */
    private void receiveData() throws IOException, ClassNotFoundException
    {
        Pair<String, Object> pair = (Pair<String, Object>)objectInputStream.readObject();
        manageReceivedData(pair);
    }

    /**
     * Zarząda danymi w parze nagłowek-wartość na podstawie informacji zawartych w nagłówku
     * @param pair Para nagłowek-wartość
     */
    private void manageReceivedData(Pair<String, Object> pair)
    {
        String key = pair.getKey();
        String[] keyWords = key.split(" ");

        if(keyWords[0].equals("GET") && keyWords[1].equals("Properties"))
        {
            Properties properties = new Properties();
            try{
                FileInputStream fileInputStream = new FileInputStream("./data/server/" + keyWords[2] + ".properties");
                properties.load(fileInputStream);
            } catch (IOException e) {
                System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] Nie udalo sie odnalezc pliku .properties");
            }
            writeAndFlush("SEND Properties " + keyWords[2], properties);
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] " + "SEND Properties " + keyWords[2]);
        }

        else if(keyWords[0].equals("GET") && keyWords[1].equals("Level"))
        {
            int levelIndex = Integer.parseInt(keyWords[2]);
            ArrayList<String> lines = new ArrayList<>();
            Scanner mapScanner;
            try {
                mapScanner = new Scanner(new File("./data/server/levels/" +levelIndex + ".txt"));
                while(mapScanner.hasNextLine())
                    lines.add(mapScanner.nextLine());

                writeAndFlush("SEND Level " + keyWords[2], lines);
                System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] " + "SEND Level " + keyWords[2]);
            } catch (FileNotFoundException e) {
                System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] " + "Desired level not found!");
                e.printStackTrace();
            }
        }

        else if(keyWords[0].equals("GET") && keyWords[1].equals("Leaderboard"))
        {
            ArrayList<String> highScores = new ArrayList<>();
            Scanner mapScanner = null;
            try {
                synchronized (Main.getScoreQueries()){
                    mapScanner = new Scanner(new File("./data/server/highScores/highScores.txt"));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            assert mapScanner != null;
            while(mapScanner.hasNextLine())
                highScores.add(mapScanner.nextLine());

            writeAndFlush("SEND Leaderboard", highScores);
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] "+ "SEND Leaderboard");
        }

        else if (keyWords[0].equals("UPDATE") && keyWords[1].equals("Highscore")){
            synchronized (Main.getScoreQueries()){
                Main.getScoreQueries().add((Pair<String, String>) pair.getValue());
            }
        }

    }
}
