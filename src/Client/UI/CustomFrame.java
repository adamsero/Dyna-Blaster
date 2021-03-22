package Client.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;

/**
 * Klasa dziedziczy po {@code JFrame} ze zdefiniowanymi
 * własnymi parametrami stylizującymi oraz obsługą skalowania
 * */

public class CustomFrame extends JFrame  {

    /**
     * Zmienna finalna określająca startową wysokość okna
     * */
    private final int startHeight;
    /**
     * Zmienna finalna określająca startową szerokość okna
     * */
    private final int startWidth;

    /**
     * Tworzy oraz stylizuje okno.
     * Ustala początkową wysokość, szerokość oraz rozmiar czcionki okna
     * @param txt Tekst nagłówka okna
     * @param backgroundColor Kolor tła okna
     * @param w Szerokość okna
     * @param h Wysokość okna
     */
    public CustomFrame(String txt,String backgroundColor, int w, int h){
        super(txt);

        this.startWidth = w;
        this.startHeight = h;

        this.setSize(w,h);
        this.setLocationRelativeTo(null);
        this.setLayout(null);
        this.getContentPane().setBackground(Color.decode(backgroundColor));

        getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeFrame();
            }
        });

    }

    /**
     * Metoda obsługująca skalowanie elementów okna: {@link CustomButton}, {@link CustomTextField},
     * {@link CustomTextArea}, {@link CustomLabel} które zawierają się w instancji {@link CustomFrame}
     */
    private void resizeFrame(){
        Component[] components = this.getContentPane().getComponents();
        if (components.length > 0){

            double xScaleRatio  =  (double)getContentPane().getWidth() / startWidth;
            double yScaleRatio  = (double)getContentPane().getHeight() / startHeight;

            for (Component currentComponent: components)
            {
                if(!(currentComponent instanceof Scalable))
                    continue;
                Scalable scalable = (Scalable)currentComponent;
                HashMap<String, Integer> dimensions = scalable.getInitialDimensions();
                scalable.setBoundsCenter((int)(dimensions.get("posX") * xScaleRatio), (int)(dimensions.get("posY") * yScaleRatio), (int)(dimensions.get("width") * xScaleRatio), (int)(dimensions.get("height") * yScaleRatio));
                currentComponent.setFont(new Font(currentComponent.getFont().getName(), currentComponent.getFont().getStyle(), calculateFontSize(xScaleRatio, yScaleRatio, dimensions.get("fontSize"))));
            }
        }
    }

    /**
     * Metoda przeskalowuje rozmiar czcionki, wybierając mniejszy współczynnik skalowania
     * @param xRatio Współczynnik skali względem osi x zależny od rozmiaru okna
     * @param yRatio Współczynnik skali względem osi y zależny od rozmiaru okna
     * @param startSize Początkowa wartość rozmiaru czcionki
     * @return Zwraca przeskalowany rozmiar czcionki
     */
    private int calculateFontSize(double xRatio, double yRatio, int startSize){
        return xRatio >= yRatio ? (int)(yRatio * startSize) : (int)(xRatio * startSize);
    }

}
