package Server.ServerUI;

import java.util.HashMap;

/**
 * Interfejs dla komponentów UI podlegających skalowaniu.
 * Umożliwia użycie polimorfizmu dla klas {@link CustomButton},
 * {@link CustomLabel}, {@link CustomTextArea}, {@link CustomTextField}.
 */
public interface Scalable
{
    /**
     * Zwraca kontener przechowujący początkowe parametry komponentu.
     * Na początkowe parametry składa się:
     * pozycja X, pozycja Y, wysokość, szerokość, wielkość czcionki
     * @return Kontener przechowujący początkowe parametry komponentu
     */
    HashMap<String, Integer> getInitialDimensions();

    /**
     * Metoda ustala koordynaty środka komponentu
     * @param x składowa X środka przycisku
     * @param y składowa Y środka przycisku
     * @param w szerokość przycisku
     * @param h wysokość przycisku
     */
    void setBoundsCenter(int x, int y, int w, int h);
}
