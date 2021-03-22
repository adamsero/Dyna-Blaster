package Server.ServerUI;



import Server.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Klasa obsługująca GUI serwera
 */
public class ServerUI implements ActionListener {

    /**
     * Okno serwera
     */
    private CustomFrame mainFrame;

    /**
     * Przycisk rozpoczynający działanie serwera
     */
    private CustomButton startButton;

    /**
     * Przycisk kończący działanie serwera
     */
    private CustomButton stopButton;

    /**
     * Napis mówiący o tym, czy serwe aktualnie działa
     */
    private CustomLabel isRunningLabel;

    /**
     * Pole do wprowadzenia numeru portu, na którym będzie działał serwer
     */
    private CustomTextField portField;

    /**
     * Wywołuje {@link #createServerWindow()}
     */
    public ServerUI(){
        createServerWindow();
    }

    /**
     * Tworzy okno serwera i wszystkie jego komonenty
     */
    private void createServerWindow(){
        mainFrame = new CustomFrame("Server","#191919",400,300);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Main.closeConnections();
            }
        });

        CustomLabel serverLabel = new CustomLabel("Serwer", 200, 50, 200, 50);

        isRunningLabel = new CustomLabel("Serwer nie działa",200,100,300,50,30);

        startButton = new CustomButton("Start",100,250,100,50,0.75,"#191919",this);
        stopButton = new CustomButton("Stop",300,250,100,50,0.75,"#191919",this);

        portField = new CustomTextField(120, 150, 200, 40, 35);
        portField.setText("10000");
        mainFrame.add(new CustomLabel("Port: ", 60, 168, 100, 40));

        mainFrame.add(serverLabel);
        mainFrame.add(isRunningLabel);
        mainFrame.add(startButton);
        mainFrame.add(stopButton);
        mainFrame.add(portField);

        isRunningLabel.setForeground(Color.RED);
        stopButton.setVisible(false);
    }

    /**
     * Pojawia okno serwera
     */
    public void show(){
        mainFrame.setVisible(true);
    }

    /**
     * Ukrywa okno serwera
     */
    public void hide(){
        mainFrame.setVisible(false);
    }

    /**
     * Wywołuje odpowiednie działanie po kliknięciu przycisku start
     */
    private void startButtonClicked(){
        String userInput = portField.getText();
        String verdict = legalInput(userInput);
        if(verdict.length() > 0) {
            JOptionPane.showMessageDialog(null, verdict, "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
            return;
        }
        portField.setEditable(false);

        isRunningLabel.setText("Serwer działa");
        isRunningLabel.setForeground(Color.GREEN);
        stopButton.setVisible(true);
        startButton.setVisible(false);

        new Thread(() -> Main.runServer(userInput)).start();
    }

    /**
     * Wywołuje odpowiednie działanie po kliknięciu przycisku stop
     */
    private void stopButtonClicked(){
        portField.setEditable(true);

        isRunningLabel.setText("Serwer nie działa");
        isRunningLabel.setForeground(Color.RED);
        stopButton.setVisible(false);
        startButton.setVisible(true);

        Main.closeConnections();
        System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] Server closed connections");
    }

    /**
     * Sprawdza, czy dane portu wpisane do pola tekstowego są liczbą mniejszą niż 65636
     * @param input Dane w formie tekstu
     * @return Wartość logiczna testu
     */
    private String legalInput(String input) {
        try {
            int inputInt = Integer.parseInt(input);
            if(inputInt < 0 || inputInt > 65635)
                return "Port musi być w przedziale 0-65635!";
        } catch(NumberFormatException e) {
            return "Numer portu musi być liczbą całkowitą!";
        }
        return "";
    }

    /**
     * Nasłuchuje na kliknięcia przycisków
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startButton){
            startButtonClicked();
        }
        else if(e.getSource() == stopButton){
            stopButtonClicked();
        }
    }
}
