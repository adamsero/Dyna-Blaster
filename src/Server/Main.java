package Server;


import Server.ServerUI.ServerUI;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Klasa odpowiadająca za działanie serwera i zarządanie połączeniami po stronie serwera
 */
public class Main
{
    /**
     * Instancja tej klasy
     */
    public static Main instance;

    /**
     * Instancja okienka interfejsu użytkownika
     */
    private static ServerUI serverUI = new ServerUI();

    /**
     * Gniazdo serwera odpowiedzialne za akceptowanie połączeń klientów
     */
    private static ServerSocket serverSocket;

    /**
     * Mówi, czy serwer obecnie działa
     */
    private static boolean running;

    /**
     * Lista połączeń serwerowych
     */
    private static final ArrayList<ServerConnection> connections = new ArrayList<>();

    /**
     * Lista zapytań o dodanie wyniku do tabeli
     */
    private static final LinkedList<Pair<String,String>> scoreQueries = new LinkedList<>();

    /**
     * Getter zmiennej {@link #scoreQueries}
     * @return Zmienna {@link #scoreQueries}
     */
    static LinkedList<Pair<String, String>> getScoreQueries() {
        return scoreQueries;
    }

    /**
     * Konstruktor pojawiający okno interfejsu
     */
    private Main(){
        serverUI.show();
    }

    /**
     * Akceptuje połączenia użytkowników w pętli do czasu zamknięcia serwera
     * @throws IOException Wyrzucany przy zamknięciu {@link #serverSocket}
     */
    private static void acceptClients() throws IOException
    {
        int index = 1;
        while(true) {
            Socket clientSocket = serverSocket.accept();
            ServerConnection serverConnection = new ServerConnection(clientSocket);
            connections.add(serverConnection);
            new Thread(serverConnection).start();
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] Client #" + index++ + " has connected");
        }
    }

    /**
     * Tworzy instancję tej klasy oraz tworzy nowy wątek obsługujący kolejkę oczekujących zapytań - {@link #scoreQueries}
     * @param args Parametry wejściowe programu
     */
    public static void main(String[] args) {
        instance = new Main();
        new Thread(Main::handleScoreQueries).start();
    }

    /**
     * Uruchamia gniazdko serwerowe oraz wywołuje {@link #acceptClients()}
     * @param portNumber Numer portu serwera
     */
    public static void runServer(String portNumber){
        try
        {
            running = true;
            serverSocket = new ServerSocket(Integer.parseInt(portNumber));
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] Server running...");
            acceptClients();
        } catch (IOException e) {
            e.printStackTrace();
            running = false;
        }
    }

    /**
     * Zamyka wszystkie połączenia serwerowe a potem zamyka gniazdko serwerowe
     */
    public static void closeConnections(){
        for(int i = connections.size()-1; i > -1 ;i--){
            try {
                connections.get(i).getSocket().close();
                connections.remove(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if(running) {
                serverSocket.close();
                running = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zarządza listą zapytań o dodanie wyniku do tabeli. Sprawdza, czy dany wynik można wpisać do tabeli,
     * po czym usuwa go z listy. Działą w pętli, do póki lista nie będzie pusta
     */
    private static void handleScoreQueries(){
        while(true){
            synchronized(scoreQueries){
                if (scoreQueries.size() > 0){
                    Pair<String,String> currentPair = scoreQueries.pollLast();
                    ArrayList<Pair<String,String>> serverScores = loadScoresFromServerFile();

                    assert serverScores != null;
                    serverScores = sortScores(serverScores, currentPair);
                    saveScoresOnServer(serverScores);
                    System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] " + "Highscore recived");
                }
                else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(25);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }

    /**
     * Ładuje tabelę najlepszych wyników z pilku
     * @return Tabela najlepszych wyników
     */
    private static ArrayList<Pair<String,String>> loadScoresFromServerFile(){

        try {
            ArrayList<Pair<String,String>> scores = new ArrayList<>();
            Scanner scanner = new Scanner(new File("./data/server/highScores/highScores.txt"));

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
     * Wstawia wynik w odpowiednie miejsce do tabeli tak, żeby zachować kolejność malejącą
     * @param scoreList Tablica wyników
     * @param pair Para - imię, wynik - do wstawienia do tabeli
     * @return Zmodyfikowana tabela
     */
    private static ArrayList<Pair<String,String>> sortScores(ArrayList<Pair<String,String>> scoreList,Pair<String,String> pair){
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
            else {
                sorted.add(currentPair);
            }
        }

        //jako ostatni
        if(!added){
            sorted.add(pair);
        }
        return sorted;
    }

    /**
     * Zapisuje tablicę wyników do pliku
     * @param scores Tablica wyników
     */
    private static void saveScoresOnServer(ArrayList<Pair<String,String>> scores){

        if (scores != null){
            try{
                File scoresFile = new File("./data/server/highScores/highScores.txt");
                scoresFile.delete();

                try {
                    PrintStream out = new PrintStream("./data/server/highScores/highScores.txt");

                    for (Pair<String,String> pair : scores){
                        out.println(pair.getKey() + " " + pair.getValue());
                    }
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
