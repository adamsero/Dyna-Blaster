package Client.UI;

import Client.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Klasa dziedzicząca po {@code JLabel} ze zdefiniowanymi
 * własnymi parametrami stylizującymi
 */
public class CustomLabel extends JLabel implements Scalable
{
    /**
     * Kontener przechowujący początkowe parametry komponentu.
     * Na początkowe parametry składa się:
     * pozycja X, pozycja Y, wysokość, szerokość, wielkość czcionki
     * */
    private HashMap<String, Integer> initialDimensions = new HashMap<>();

    /**
     * Tworzy oraz stylizuje etykietę.
     * Ustala startowe współrzędne etykiety, jej początkową wysokość, szerokość oraz rozmiar czcionki
     * @param txt Tekst
     * @param x Składowa X środka etykiety
     * @param y Składowa Y środka etykiety
     * @param w Szerokość etykiety
     * @param h Wysokość etykiety
     */

    public CustomLabel(String txt, int x, int y, int w, int h)
    {
        super(txt);
        setBoundsCenter(x, y, w, h);

        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setForeground(Color.WHITE);
        setFont(Utils.createCustomFont("RL.ttf", (int)(getHeight() * 0.9), Font.PLAIN));

        initialDimensions.put("posX", x);
        initialDimensions.put("posY", y);
        initialDimensions.put("width", w);
        initialDimensions.put("height", h);
        initialDimensions.put("fontSize", getFont().getSize());
    }

    public CustomLabel(String txt, int x, int y, int w, int h, int fontSize)
    {
        super(txt);
        setBoundsCenter(x, y, w, h);

        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setForeground(Color.WHITE);
        setFont(Utils.createCustomFont("RL.ttf", fontSize, Font.PLAIN));

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
     * Ustala koordynaty lewego górnego rogu etykiety korzystając z koordynatów jej środka
     * @param x Składowa X środka etykiety
     * @param y Składowa Y środka etykiety
     * @param w Szerokość etykiety
     * @param h Wysokość etykiety
     */
    @Override
    public void setBoundsCenter(int x, int y, int w, int h)
    {
        setBounds(x - w / 2, y - h / 2, w, h);
    }
}
