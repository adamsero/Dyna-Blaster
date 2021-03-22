package Server.ServerUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * Klasa dziedzicząca po {@code JButton} ze zdefiniowanymi
 * własnymi parametrami stylizującymi
 */
public class CustomButton extends JButton implements Scalable
{
    /**
     * Kontener przechowujący początkowe parametry komponentu.
     * Na początkowe parametry składa się:
     * pozycja X, pozycja Y, wysokość, szerokość, wielkość czcionki
     * */
    private HashMap<String, Integer> initialDimensions = new HashMap<>();

    /**
     * Tworzy oraz stylizje przycisk z tekstem.
     * Ustala startowe współrzędne przycisku, jego początkową wysokość, szerokość oraz rozmiar czcionki
     * @param txt tekst na przycisku
     * @param x składowa X środka przycisku
     * @param y składowa Y środka przycisku
     * @param w szerokość przycisku
     * @param h wysokość przycisku
     * @param fontSizeToHeightRatio współczynnik rozmiaru czcionki do wysokości - rozmiar obliczany
     *                              jest w następujący sposób:
     *                              {@code size = h * fontSizeToHeightRatio}
     * @param textColor kolor tekstu
     * @param actionListener "słuchacz" zdarzeń dla tego przycisku
     */
    public CustomButton(String txt, int x, int y, int w, int h, double fontSizeToHeightRatio, String textColor, ActionListener actionListener)
    {
        super(txt);
        setBackground(Color.WHITE);
        setForeground(Color.decode(textColor));
        setBorder(BorderFactory.createEmptyBorder());
        setFocusPainted(false);
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setBoundsCenter(x, y, w, h);

        setFont(Utils.createCustomFont("RL.ttf", (int)(getHeight() * fontSizeToHeightRatio), Font.PLAIN));

        if(actionListener != null)
            addActionListener(actionListener);

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
     * Metoda ustala koordynaty lewego górnego rogu przycisku korzystając z koordynatów jego środka
     * @param x składowa X środka przycisku
     * @param y składowa Y środka przycisku
     * @param w szerokość przycisku
     * @param h wysokość przycisku
     */
    @Override
    public void setBoundsCenter(int x, int y, int w, int h)
    {
        setBounds(x - w / 2, y - h / 2, w, h);
    }
}
