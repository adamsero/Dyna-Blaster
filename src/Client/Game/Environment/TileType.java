package Client.Game.Environment;

import javax.swing.*;

/**
 * Klasa reprezentuąca typ komórki planszy
 */
public class TileType
{
    /**
     * ID komórki
     */
    public int ID;

    /**
     * Mówi, czy zmienna jest przeszkodą terenową
     */
    public boolean obstacle;

    /**
     * Mówi, czy komórkę można zniszczyć bombą
     */
    public boolean destructible;

    /**
     * Obrazek wyświetlany na pozycji komórki
     */
    ImageIcon texture;

    /**
     * Konstruktor, przypisuje wartości zmiennych
     * @param ID ID komórki
     * @param texture Mówi, czy zmienna jest przeszkodą terenową
     * @param obstacle Mówi, czy komórkę można zniszczyć bombą
     * @param destructible Obrazek wyświetlany na pozyci komórki
     */
    public TileType(int ID, ImageIcon texture, boolean obstacle, boolean destructible)
    {
        this.ID = ID;
        this.texture = texture;
        this.obstacle = obstacle;
        this.destructible = destructible;
    }
}
