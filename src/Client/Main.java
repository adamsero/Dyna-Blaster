package Client;

import Client.Game.Managers.GameManager;
import Client.UI.ConnectionQuery;
import Client.UI.UIManager;

import javax.swing.*;
import java.io.IOException;

/**
 * Klasa główna, odpowiedzialna za komunikację między menedżerem UI a menedżerem gry
 */

public class Main
{
    /**
     * Typ wyliczeniowy odpowiedzialny za przechowywanie obecnego stanu aplikacji
     * np. czy użytkownik jest w menu, w grze.
     */
    private enum GameState {MENU, GAME}

    /**
     * Zmienna mówiąca o obecnym stanie aplikacji
     */
    private static GameState gameState;

    /**
     * Instancja menedżera UI
     */
    private UIManager uiManager;

    /**
     * Instancja menedżera gry
     */
    private GameManager gameManager;

    /**
     * Instancja połączenia po stronie klienta
     */
    public ClientConnection clientConnection;

    /**
     * Instancja klasy głównej
     */
    public static Main instance;

    /**
     * Wczytuje właściwości klas za pośrednictwem {@link Client.Utils#loadAppProperties()}.
     * Wywołuje konstruktor tej klasy
     * @param args Argumenty początkowe
     */
    public static void main(String[] args)
    {
        ConnectionQuery connectionQuery = new ConnectionQuery();
        new Thread(connectionQuery).start();
        synchronized (connectionQuery.connectionChosen)
        {
            while(!connectionQuery.connectionChosen.get())
            {
                try {
                    connectionQuery.connectionChosen.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        instance = new Main();
        if(Utils.getOnlineGaming())
        {
            try {
                instance.createClient(connectionQuery);
            } catch (IOException e) {
                int input = JOptionPane.showOptionDialog(null, "Nie można połączyć się z serwerem", "Błąd", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if(input == JOptionPane.OK_OPTION)
                    System.exit(-1);
            }

            Utils.getAppProperties(instance.clientConnection);

            synchronized (instance.clientConnection.requestsSent)
            {
                while(instance.clientConnection.requestsSent.get() != 0)
                {
                    try {
                        instance.clientConnection.requestsSent.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("All properties ready");
        }
        else
            Utils.loadAppProperties();
        instance.initializeGame();

    }

    /**
     * Metoda tworząca klienta do kontaktu z serwerem
     * @param connectionQuery argument reprezentujący okienko z wyborem trybu gry
     * @throws IOException w przypadku niepowodzenia połączenia
     */
    private void createClient(ConnectionQuery connectionQuery) throws IOException
    {
        String ip = connectionQuery.ipField.getText();
        int port = Integer.parseInt(connectionQuery.portField.getText());
        clientConnection = new ClientConnection(ip, port);
        new Thread(clientConnection).start();
    }

    /**
     * Inicjalizuje zmienne {@link Client.Main#uiManager} i {@link Client.Main#gameManager}
     * oraz tworzy nowe wątki dla tych obiektów, ustawia początkowy stan aplikacji na MENU
     */
    private void initializeGame()
    {
        uiManager = new UIManager(this);
        gameManager = new GameManager(this);

        new Thread(uiManager, "UIThread").start();
        new Thread(gameManager, "GameThread").start();

        gameState = GameState.MENU;
    }

    /**
     * Zmienia stan aplikacji na GAME oraz wywołuje {@link Client.Main#onStateChanged()}
     */
    public void openGameWindow()
    {
        gameState = GameState.GAME;
        onStateChanged();
    }

    /**
     * Zmienia stan aplikacji na MENU oraz wywołuje {@link Client.Main#onStateChanged()}
     */
    public void openMenuWindow()
    {
        gameState = GameState.MENU;
        onStateChanged();
    }

    /**
     * W zależności od stanu aplikacji, otwiera lub zamyka okno (lub okna) menu oraz okno gry.
     * Wywoływana przy każdej zmianie stanu aplikacji
     */
    private void onStateChanged()
    {
        uiManager.setMenuWindowVisibility(gameState.equals(GameState.MENU));
        gameManager.setGameWindowVisibility(gameState.equals(GameState.GAME));
    }

    /**
     * Getter zmiennej {@link #uiManager}
     * @return Zmienna {@link #uiManager}
     */
    public UIManager getUiManager() {
        return uiManager;
    }
}
