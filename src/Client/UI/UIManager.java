package Client.UI;

import Client.ClientConnection;
import Client.Game.Managers.FileManager;
import Client.Game.Managers.GameManager;

import Client.Main;
import Client.Sound.SoundManager;
import Client.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Klasa odpowiadająca za zarządzanie wszystkimi elementami interfejsu użytkownika.
 * Dziedziczy po {@code Runnable}, żeby mogła pracować na innym wątku
 * oraz po {@code ActionListener}, żeby można było w niej zaprojektować
 * interakcje pomiędzy elementami UI
 */
public class UIManager implements Runnable, ActionListener
{
    /**
     * Referencja do instancji klasy głównej.
     * Używana do komunikacji z klasą {@link GameManager}
     */
    private Main mainActivity;

    /**
     * Zmienna przechowująca okno główne menu
     */
    private CustomFrame menuWindow;

    /**
     * Zmienna przechowująca okno pomocy
     */
    private CustomFrame helpWindow;

    /**
     * Zmienna przechowująca okno najlepszych wyników
     */
    private CustomFrame highScoreWindow;

    /**
     * Zmienna przechowująca okno wprowadzania nazwy gracza
     */
    private CustomFrame insertNameWindow;

    /**
     * Zmienna przechowująca okno końca gry
     */
    private CustomFrame endGameInfo;

    /**
     * Zmienna przechowująca pole tekstowe z nazwą gracza - używane
     * do ewentualnego wpisania gracza do listy najlepszych wyników
     */
    private CustomTextField nameField;

    /**
     * Zmienna określająca napis, któy wyświtli się po końcu gry
     * (Wygrana / Porażka) i liczba punktów
     */
    private CustomLabel gameResult;

    /**
     * Kontener przechowujący wszystkie przyciski we wszystkich oknach.
     * Użyto {@code HashMap}, ponieważ każdy przycisk potrzebuje swojego
     * identyfikatora - tutaj w postaci klucza typu {@code String}, żeby później
     * w {@link UIManager#actionPerformed(ActionEvent)} wiadomo było, który
     * przycisk odpowiada za jaką akcję
     */
    private HashMap<String, CustomButton> buttons = new HashMap<>();

    /**
     * Zmienna określająca kolor tła wszystkich okien UI
     */
    private Color backgroundColor = new Color(25, 25, 25);

    /**
     * Pole typu {@code SoundManager} przechowywujące instancję menadżera dźwięku, który obsługuje odtwarzanie muzyki
     * w tle oraz efektów dźwiękowych
     */
    private SoundManager soundManager = new SoundManager();

    /**
     * Pole typu {@code FileManager} przechowywujące instancję menadżera plików, który obsługuje wczytywanie plików XML
     */
    private FileManager fileManager = new FileManager();

    /**
     * Obszar tekstowy do wyświetlania informacji o najlepszych wynikach
     */
    public CustomTextArea highScores;

    /**
     * Pole typu{@code String} z nazwą gracza
     */
    private String nick;

    /**
     * Przypisuje referencję do instancji klasy głównej
     * @param mainActivity Referencja do instancji klasy głównej
     */
    public UIManager(Main mainActivity)
    {
        this.mainActivity = mainActivity;
        soundManager.playBackgroundTheme();
    }

    /**
     * Tworzy wszystkie okna, ukrywa wszystkie oprócz {@link UIManager#menuWindow}
     */
    @Override
    public void run()
    {
        createMenuWindow();
        createInsertNameWindow();
        createEndGameInfoWindow();
        try
        {
            createHelpWindow();
            createHighScoreWindow();
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
    }

    /**
     * Steruje interakcją między elementami interfejsu
     * @param e Zdarzenie, które zaszło w programie
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(buttons.get("Graj"))){
            soundManager.playClickPositiveSound();
            insertNameWindow.setVisible(true);
        }
        if(e.getSource().equals(buttons.get("Pomoc"))) {
            soundManager.playClickPositiveSound();
            helpWindow.setVisible(true);
        }
        if(e.getSource().equals(buttons.get("Wyniki"))) {
            soundManager.playClickPositiveSound();
            getHighScores();
            highScoreWindow.setVisible(true);
        }
        if(e.getSource().equals(buttons.get("Wyjscie"))) {
            soundManager.playClickNegativeSound();
            System.exit(0);
        }
        if(e.getSource().equals(buttons.get("ZamknijPomoc"))) {
            soundManager.playClickNegativeSound();
            helpWindow.setVisible(false);
        }
        if(e.getSource().equals(buttons.get("ZamknijNazweGracza"))) {
            soundManager.playClickNegativeSound();
            insertNameWindow.setVisible(false);
        }
        if(e.getSource().equals(buttons.get("ZamknijNajlepszeWyniki"))) {
            soundManager.playClickNegativeSound();
            highScoreWindow.setVisible(false);
        }
        if(e.getSource().equals(buttons.get("KoniecGry"))) {
            endGameInfo.setVisible(false);
            mainActivity.openMenuWindow();
        }
        if(e.getSource().equals(buttons.get("DoGry")))
        {
            nick = null;
            soundManager.playClickPositiveSound();
            this.nick = nameField.getText();
            if(nick.length() > 0)
            {
                insertNameWindow.setVisible(false);
                menuWindow.setVisible(false);
                mainActivity.openGameWindow();
            }
        }
    }

    /**
     * Tworzy okno główne menu oraz wszystkie elementy UI do niego należące
     */
    private void createMenuWindow()
    {
        Element rootElement = fileManager.parseXML("./data/info/UIelements/menuElements/menuElements.xml").getDocumentElement();
        rootElement.normalize();

        menuWindow = new CustomFrame(rootElement.getAttribute("txt"), rootElement.getAttribute("background-color"),Integer.parseInt(rootElement.getAttribute("width")), Integer.parseInt(rootElement.getAttribute("height")));

        Element currentElement = (Element) rootElement.getElementsByTagName("label").item(0);
        CustomLabel title = new CustomLabel(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")));

        NodeList nodes = rootElement.getElementsByTagName("button");

        for(int i = 0;i < nodes.getLength();i++){
            currentElement = (Element) nodes.item(i);
            buttons.put(currentElement.getAttribute("name"), new CustomButton(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Double.parseDouble(currentElement.getAttribute("font-size-to-height-ratio")), currentElement.getAttribute("text-color"), this));
        }

        for(CustomButton button : buttons.values()) {
            menuWindow.add(button);
        }
        menuWindow.add(Box.createRigidArea(new Dimension(60,60)));
        menuWindow.add(title);

        menuWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        menuWindow.setVisible(true);
    }

    /**
     * Tworzy okno pomocy oraz wszystkie elementy UI do niego należące
     * @exception FileNotFoundException Powstaje kiedy wczytywanie tekstu pomocy
     * z pliku tekstowego się nie powiedzie
     */
    private void createHelpWindow() throws FileNotFoundException
    {
        Element rootElement = fileManager.parseXML("./data/info/UIelements/helpElements/helpElements.xml").getDocumentElement();
        rootElement.normalize();

        helpWindow = new CustomFrame(rootElement.getAttribute("txt"), rootElement.getAttribute("background-color"),Integer.parseInt(rootElement.getAttribute("width")), Integer.parseInt(rootElement.getAttribute("height")));
        helpWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        Element currentElement = (Element) rootElement.getElementsByTagName("label").item(0);

        CustomLabel helpTitle = new CustomLabel(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")));
        helpWindow.add(helpTitle);

        currentElement = (Element) rootElement.getElementsByTagName("text-area").item(0);

        //String message = new Scanner(new File("./data/info/helpMessage.txt")).nextLine();
        CustomTextArea helpMessage = new CustomTextArea(Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Integer.parseInt(currentElement.getAttribute("font-size")));
        helpMessage.setBackground(backgroundColor);

        Scanner scanner = new Scanner(new File("./data/info/general/helpMessage.txt"));
        while(scanner.hasNextLine())
            helpMessage.append(scanner.nextLine() + "\n");
        helpWindow.add(helpMessage);

        currentElement = (Element) rootElement.getElementsByTagName("button").item(0);

        buttons.put(currentElement.getAttribute("name"), new CustomButton(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Double.parseDouble(currentElement.getAttribute("font-size-to-height-ratio")), currentElement.getAttribute("text-color"), this));
        helpWindow.add(buttons.get("ZamknijPomoc"));

        helpWindow.setVisible(false);
    }

    /**
     * Tworzy okno pomocy oraz wszystkie elementy UI do niego należące
     * @exception FileNotFoundException Powstaje kiedy wczytywanie najlepszych wyników
     * z pliku tekstowego się nie powiedzie
     */
    private void createHighScoreWindow() throws FileNotFoundException
    {
        Element rootElement = fileManager.parseXML("./data/info/UIelements/highScoreElements/highScoreElements.xml").getDocumentElement();
        rootElement.normalize();

        highScoreWindow = new CustomFrame(rootElement.getAttribute("txt"), rootElement.getAttribute("background-color"),Integer.parseInt(rootElement.getAttribute("width")), Integer.parseInt(rootElement.getAttribute("height")));

        highScoreWindow.setLocationRelativeTo(null);
        highScoreWindow.setLayout(null);
       // highScoreWindow.getContentPane().setBackground(backgroundColor);
        highScoreWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        Element currentElement = (Element) rootElement.getElementsByTagName("label").item(0);

        CustomLabel highScoreTitle = new CustomLabel(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")));
        highScoreWindow.add(highScoreTitle);

        currentElement = (Element) rootElement.getElementsByTagName("text-area").item(0);

        CustomTextArea highScores = new CustomTextArea(Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Integer.parseInt(currentElement.getAttribute("font-size")));
        this.highScores = highScores;
        highScores.setBackground(backgroundColor);

        JScrollPane scrollPane = new JScrollPane(highScores);
        scrollPane.setBounds(Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")));
        highScoreWindow.add(scrollPane);

        currentElement = (Element) rootElement.getElementsByTagName("button").item(0);

        buttons.put(currentElement.getAttribute("name"), new CustomButton(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Double.parseDouble(currentElement.getAttribute("font-size-to-height-ratio")), currentElement.getAttribute("text-color"), this));
        highScoreWindow.add(buttons.get("ZamknijNajlepszeWyniki"));

        highScoreWindow.setVisible(false);
    }

    /**
     * Tworzy okno wprowadzania nazwy gracza oraz wszystkie elementy UI do niego należące
     */
    private void createInsertNameWindow()
    {
        Element rootElement = fileManager.parseXML("./data/info/UIelements/insertNameElements/insertNameElements.xml").getDocumentElement();
        rootElement.normalize();

        insertNameWindow = new CustomFrame(rootElement.getAttribute("txt"), rootElement.getAttribute("background-color"),Integer.parseInt(rootElement.getAttribute("width")), Integer.parseInt(rootElement.getAttribute("height")));

        insertNameWindow.setLocationRelativeTo(null);
        insertNameWindow.setLayout(null);
       // insertNameWindow.getContentPane().setBackground(backgroundColor);
        insertNameWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        Element currentElement = (Element) rootElement.getElementsByTagName("label").item(0);

        CustomLabel insertName = new CustomLabel(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")));
        insertNameWindow.add(insertName);

        currentElement = (Element) rootElement.getElementsByTagName("text-field").item(0);

        nameField = new CustomTextField(Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Integer.parseInt(currentElement.getAttribute("font-size")));

        insertNameWindow.add(nameField);

        NodeList nodes = rootElement.getElementsByTagName("button");

        for(int i = 0;i < nodes.getLength();i++){
            currentElement = (Element) nodes.item(i);
            buttons.put(currentElement.getAttribute("name"), new CustomButton(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Double.parseDouble(currentElement.getAttribute("font-size-to-height-ratio")), currentElement.getAttribute("text-color"), this));
        }

        //buttons.put("DoGry", new CustomButton("OK", insertNameWindow.getWidth() / 2 - 70, insertNameWindow.getHeight() - 75, 100, 40, 0.75, backgroundColor, this));
        insertNameWindow.add(buttons.get("DoGry"));
        //buttons.put("ZamknijNazweGracza", new CustomButton("Anuluj", insertNameWindow.getWidth() / 2 + 70, insertNameWindow.getHeight() - 75, 100, 40, 0.75, backgroundColor, this));
        insertNameWindow.add(buttons.get("ZamknijNazweGracza"));

        insertNameWindow.setVisible(false);
    }

    /**
     * Tworzy okno końca gry oraz wszystkie elementy UI do niego należące
     */
    private void createEndGameInfoWindow()
    {
        Element rootElement = fileManager.parseXML("./data/info/UIelements/endGameInfoElements/endGameInfoElements.xml").getDocumentElement();
        rootElement.normalize();

        endGameInfo = new CustomFrame(rootElement.getAttribute("txt"), rootElement.getAttribute("background-color"),Integer.parseInt(rootElement.getAttribute("width")), Integer.parseInt(rootElement.getAttribute("height")));
        endGameInfo.setLocationRelativeTo(null);
        endGameInfo.setLayout(null);
        endGameInfo.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        Element currentElement = (Element) rootElement.getElementsByTagName("label").item(0);

        gameResult = new CustomLabel(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")));
        endGameInfo.add(gameResult);

        currentElement = (Element) rootElement.getElementsByTagName("button").item(0);
        buttons.put(currentElement.getAttribute("name"), new CustomButton(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Double.parseDouble(currentElement.getAttribute("font-size-to-height-ratio")), currentElement.getAttribute("text-color"), this));

        endGameInfo.add(buttons.get("KoniecGry"));
    }

    /**
     * Ustawia tekst zmiennej {@link #gameResult} i pojawia okno {@link #endGameInfo}
     * @param text tekst
     */
    public void setGameResult(String text)
    {
        gameResult.setText(text);
        endGameInfo.setVisible(true);
    }

    /**
     * Steruje widocznością okien menu.
     * Jeśli wartość {@code state} wynosi {@code false}, to chowane są wszystkie okna.
     * W przeciwnym wypadku, pojawia jest tylko okno główne
     * @param state Mówi, czy okna mają się chować, czy pojawiać
     */
    public void setMenuWindowVisibility(boolean state)
    {
        menuWindow.setVisible(state);
        if(state)
            return;
        helpWindow.setVisible(false);
        highScoreWindow.setVisible(false);
        insertNameWindow.setVisible(false);
        endGameInfo.setVisible(false);
    }

    /**
     * Getter dla pola {@link #soundManager}
     * @return {@code SoundManager}
     */
    public SoundManager getSoundManager() {
        return soundManager;
    }

    /**
     * Metoda wysyłająca żądanie pobrania wyników i czekająca na ich odebranie
     */
    public void getHighScores(){
        if(Utils.getOnlineGaming()) {
            ClientConnection clientConnection = Main.instance.clientConnection;
            clientConnection.writeAndFlush("GET Leaderboard", null);
            clientConnection.highScoresRecived.set(false);

            synchronized (clientConnection.highScoresRecived) {
                while (!clientConnection.highScoresRecived.get()) {
                    try {
                        clientConnection.highScoresRecived.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else{
            highScores.loadFromLocalFile();
        }
    }

    /**
     * Getter dla pola {@link #nick}
     * @return Zmienna {@link #nick}
     */
    public String getNick() {
        return nick;
    }
}
