package Client.UI;

import Client.Game.Managers.FileManager;
import Client.Utils;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Klasa służąca do wyboru przez gracza trybu gry - online lub offline
 * Przechowuje okienko interfejsu wyboru trybu i wpisania danych serwera
 */
public class ConnectionQuery implements Runnable, ActionListener
{
    /**
     * Flaga do synchronizacji z wątkiem głównym
     */
    public final AtomicBoolean connectionChosen = new AtomicBoolean();

    /**
     * Zmienna przechowująca okno wyboru trybu gry (online czy offline) oraz wprowadzania adresu
     * IP i numeru portu serwera
     */
    private CustomFrame connectionWindow;

    /**
     * Zmienna przechowująca pole tekstowe z informacją o adresie IP serwera
     */
    public CustomTextField ipField;

    /**
     * Zmienna przechowująca pole tekstowe z informacją o numerze portu serwera
     */
    public CustomTextField portField;

    /**
     * Przycisk do wyboru trybu gry przez sieć
     */
    private CustomButton playOnline;

    /**
     * Przycisk do wyboru trybu gry poza siecią
     */
    private CustomButton playOffline;

    /**
     * Wywołuje metodę {@link #createConnectionWindow()}
     */
    @Override
    public void run() {
        createConnectionWindow();
    }

    /**
     * Tworzy okno connectionWindow oraz wszystkie elementy UI do niego należące
     */
    private void createConnectionWindow()
    {
        FileManager fileManager = new FileManager();
        Element rootElement = fileManager.parseXML("./data/info/UIelements/connectionElements/connectionElements.xml").getDocumentElement();
        rootElement.normalize();

        connectionWindow = new CustomFrame(rootElement.getAttribute("txt"), rootElement.getAttribute("background-color"),Integer.parseInt(rootElement.getAttribute("width")), Integer.parseInt(rootElement.getAttribute("height")));

        connectionWindow.setLocationRelativeTo(null);
        connectionWindow.setLayout(null);
        connectionWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Element currentElement = (Element) rootElement.getElementsByTagName("label").item(0);
        CustomLabel insertIP = new CustomLabel(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")));
        connectionWindow.add(insertIP);

        currentElement = (Element) rootElement.getElementsByTagName("label2").item(0);
        CustomLabel insertPort = new CustomLabel(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")));
        connectionWindow.add(insertPort);

        currentElement = (Element) rootElement.getElementsByTagName("text-field").item(0);
        ipField = new CustomTextField(Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Integer.parseInt(currentElement.getAttribute("font-size")));
        ipField.setText("localhost");
        connectionWindow.add(ipField);

        currentElement = (Element) rootElement.getElementsByTagName("text-field2").item(0);
        portField = new CustomTextField(Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Integer.parseInt(currentElement.getAttribute("font-size")));
        portField.setText("10000");
        connectionWindow.add(portField);

        currentElement = (Element) rootElement.getElementsByTagName("button").item(0);
        playOnline = new CustomButton(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Double.parseDouble(currentElement.getAttribute("font-size-to-height-ratio")), currentElement.getAttribute("text-color"), this);
        connectionWindow.add(playOnline);

        currentElement = (Element) rootElement.getElementsByTagName("button2").item(0);
        playOffline = new CustomButton(currentElement.getTextContent(), Integer.parseInt(currentElement.getAttribute("x")), Integer.parseInt(currentElement.getAttribute("y")), Integer.parseInt(currentElement.getAttribute("width")), Integer.parseInt(currentElement.getAttribute("height")),Double.parseDouble(currentElement.getAttribute("font-size-to-height-ratio")), currentElement.getAttribute("text-color"), this);
        connectionWindow.add(playOffline);

        connectionWindow.setVisible(true);
    }

    /**
     * Zarządza wciśnięciami przycisków, powiadamia wątek główny o wznowienu pracy
     * @param e Zdarzenie, które zaszło w programie
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource().equals(playOnline))
        {
            Utils.setOnlineGaming(true);
            connectionWindow.dispose();
            synchronized (connectionChosen)
            {
                connectionChosen.set(true);
                connectionChosen.notifyAll();
            }
        }
        else if(e.getSource().equals(playOffline))
        {
            Utils.setOnlineGaming(false);
            connectionWindow.dispose();
            synchronized (connectionChosen)
            {
                connectionChosen.set(true);
                connectionChosen.notifyAll();
            }
        }
    }
}
