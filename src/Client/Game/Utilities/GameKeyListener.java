package Client.Game.Utilities;

import Client.Game.Managers.GamePanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Klasa służąca do rozpoznawania naciśnięcia klawiszy
 */
public class GameKeyListener implements KeyListener
{
    /**
     * Pole typu {@code GamePanel} przechowywujące referencję do panelu gry
     */
    private GamePanel gamePanel;

    /**
     * Konstruktor
     * @param gamePanel przekazuje referencje do panelu gry
     */
    public GameKeyListener(GamePanel gamePanel)
    {
        this.gamePanel = gamePanel;
    }

    /**
     * Metoda obsługująca wybranie klawisza
     * @param e obiekt typu {@code KeyEvent} reprezentujący zdarzenie wybranie klawisza
     */
    @Override
    public void keyTyped(KeyEvent e)
    { }


    /**
     * Metoda wykrywająca i obsługująca naciśnięcie klawisza
     * @param e obiekt typu {@code KeyEvent} reprezentujący zdarzenie nacisnięcia klawisza
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_P) {
            if(!(gamePanel.timeManager.paused)){
                gamePanel.timeManager.pause();
            } else {
                gamePanel.timeManager.resume();
                //System.out.println(gamePanel.timeManager.calculatePointsToSubtract());
            }
        }

        if(gamePanel.timeManager.paused)
            return;

        switch (key)
        {
            case KeyEvent.VK_LEFT:
                gamePanel.player.left = true;
                break;

            case KeyEvent.VK_RIGHT:
                gamePanel.player.right = true;
                break;

            case KeyEvent.VK_UP:
                gamePanel.player.up = true;
                break;

            case KeyEvent.VK_DOWN:
                gamePanel.player.down = true;
                break;

            case KeyEvent.VK_SPACE:
                gamePanel.player.currentTile().plantBomb();
                break;
        }
    }

    /**
     * Metoda wykrywająca i obsługująca puszczenie klawisza
     * @param e obiekt typu {@code KeyEvent} reprezentujący zdarzenie puszczenia klawisza klawisza
     */
    @Override
    public void keyReleased(KeyEvent e)
    {
        if(gamePanel.timeManager.paused)
            return;

        int key = e.getKeyCode();

        switch (key)
        {
            case KeyEvent.VK_LEFT:
                gamePanel.player.left = false;
                break;

            case KeyEvent.VK_RIGHT:
                gamePanel.player.right = false;
                break;

            case KeyEvent.VK_UP:
                gamePanel.player.up = false;
                break;

            case KeyEvent.VK_DOWN:
                gamePanel.player.down = false;
                break;
        }
    }
}
