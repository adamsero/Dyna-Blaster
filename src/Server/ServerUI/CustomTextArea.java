package Server.ServerUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Klasa dziedzicząca po {@code JTextArea} ze zdefiniowanymi
 * własnymi parametrami stylizującymi
 */
public class CustomTextArea extends JTextArea implements Scalable
{
    /**
     * Kontener przechowujący początkowe parametry komponentu.
     * Na początkowe parametry składa się:
     * pozycja X, pozycja Y, wysokość, szerokość, wielkość czcionki
     * */
    private HashMap<String, Integer> initialDimensions = new HashMap<>();

    /**
     * Tworzy oraz stylizuje pole tekstowe.
     * Ustala startowe współrzędne pola tekstowego, jego początkową wysokość, szerokość oraz rozmiar czcionki
     * @param x Składowa X środka pola tekstowego
     * @param y Składowa Y środka pola tekstowego
     * @param w Szerokość pola tekstowego
     * @param h Wysokość pola tekstowego
     * @param fontSize Rozmiar czcionki pola tekstowego
     */
    public CustomTextArea(int x,int y,int w, int h, int fontSize){
        super();
        this.setBounds(x, y, w, h);

        this.setForeground(Color.WHITE);
        this.setEditable(false);
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
        this.setFont(Utils.createCustomFont("RL.ttf", fontSize, Font.PLAIN));

        initialDimensions.put("posX", x);
        initialDimensions.put("posY", y);
        initialDimensions.put("width", w);
        initialDimensions.put("height", h);
        initialDimensions.put("fontSize", getFont().getSize());
    }

    /**
     * Getter dla początkowych wymiarów komponentu.
     * @return Kontener przechowujący początkowe parametry komponentu
     */
    @Override
    public HashMap<String, Integer> getInitialDimensions()
    {
        return initialDimensions;
    }

    /**
     * Ustala koordynaty środka okna tekstowego
     * @param x Składowa X środka pola tekstowego
     * @param y Składowa Y środka pola tekstowego
     * @param w Szerokość pola tekstowego
     * @param h Wysokość pola tekstowego
     */
    @Override
    public void setBoundsCenter(int x, int y, int w, int h)
    {
        setBounds(x, y, w, h);
    }

    /**
     * Metida ładująca wyniki z lokalnego pliku
     */
    public void loadFromLocalFile(){

        try {
            Scanner scanner = new Scanner(new File("./data/info/general/highScores.txt"));
            int index = 1;

            this.setText("");

            while(scanner.hasNextLine())
                this.append(index++ + ". " + scanner.nextLine() + "\n");
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}
