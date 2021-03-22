package Client.Game.Entities;

import Client.Game.Managers.AnimationManager;
import Client.Game.Managers.GameManager;
import Client.Game.Managers.Level;
import Client.Game.Utilities.Vector;
import Client.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca "mądrego" przeciwnika - znajdującego optymalną ścieżkę do gracza
 */
public class SmartEnemy extends Enemy
{
    /**
     * Klasa pomocnicza,reprezentująca punkt na siatce, wykorzystywana w algorytmie A*
     */
    private static class Spot
    {
        /**
         * Składowa x pozycji punktu na siatce
         */
        int i;

        /**
         * Skłądowa y pozycji punktu na siatce
         */
        int j;

        /**
         * Mówi, ile kosztowało dotarcie do tego punktu
         */
        double g = 0;

        /**
         * Przewiduje, ile będzie kosztowało dojście do celu z tego punktu
         */
        double heuristic = 0;

        /**
         * Summa zmiennych {@link #g} i {@link #heuristic}
         */
        double f = 0;

        /**
         * Lista sąsiiadujących punktów
         */
        List<Spot> neighbors = new ArrayList<>();

        /**
         * Poprzednik tego punktu w algorytmie A*
         */
        Spot previous = null;

        /**
         * Mówi, czy punkt jest przeszkodą
         */
        boolean wall;

        /**
         * Konstruktor, przypisuje wartości zmiennym
         * @param i Składowa x pozycji punktu na siatce
         * @param j Składowa y pozycji punktu na siatce
         * @param wall Mówi, czy punkt jest przeszkodą
         */
        Spot(int i, int j, boolean wall)
        {
            this.i = i;
            this.j = j;
            this.wall = wall;
        }

        /**
         * Dodaje sąsiadów do listy. Zwraca uwgę na edge case'y - czy punkt znajduje się na którejś krawędzi
         * @param grid Tablica przechowująca wszystkie punkty
         */
        void addNeighbors(Spot[][] grid)
        {
            int cols = grid.length, rows = grid[0].length;
            if (i < cols - 1)
                neighbors.add(grid[i + 1][j]);
            if (i > 0)
                neighbors.add(grid[i - 1][j]);
            if (j < rows - 1)
                neighbors.add(grid[i][j + 1]);
            if (j > 0)
                neighbors.add(grid[i][j - 1]);
        }
    }

    /**
     * Wywołuje konstruktor klasy rodzica
     * @param x Składowa x pozycji komórki, na której przseciwnik się pojawił
     * @param y Składowa y pozycji komórki, na której przseciwnik się pojawił
     * @param level Referencja do obecnego poziomu gry
     */
    public SmartEnemy(int x, int y, Level level) {
        super(x, y, level);
        pointValue = Double.parseDouble(properties.getProperty("pointValueSmart"));
    }

    /**
     * Obiera optymalną ścieżkę do gracza. Jeśli taka ścieżka nie istnieje, obiera
     * inną, do punktu położonego najbliżej gracza w sensie odległości.
     * @param player Referencja do postaci gracza
     */
    void setObjective(Player player)
    {
        int TILE_WIDTH = GameManager.DEFAULT_TILE_WIDTH;
        int cols = currentLevel.mapWidth;
        int rows = currentLevel.mapHeight;

        //tworzymy mapę punktów
        Spot[][] grid = new Spot[cols][rows];

        //zbiór otwarty - punkty, któych algorytm jeszcze nie sprawdził
        List<Spot> openSet = new ArrayList<>();

        //zbiór zamknięty - punkty, które zostały już sprawdzone
        List<Spot> closedSet = new ArrayList<>();

        //start - punkt początkowy
        //end - punkt końcowy
        //current - pnkt obecny
        //bestOfWorst - punkt najbliżej celu jeśli ścieżka do celu nie istnieje
        Spot start, end, current = null, bestOfWorst;

        //inicjalizujemy tablicę punktów
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++)
                grid[i][j] = new Spot(i, j, currentLevel.map[i][j].getTileType().obstacle);

        //dodajemy sąsiadów dla każdego punktu
        for (int i = 0; i < cols; i++)
            for (int j = 0; j < rows; j++)
                grid[i][j].addNeighbors(grid);

        //punkt startowy to pozycja przeciwnika na mapie w pikselach, przekonwertowana na miarę w punktach
        start = grid[(int)Math.floor(position.x / TILE_WIDTH)][(int)Math.floor(position.y / TILE_WIDTH)];

        //na początku nie istnieje ścieżka, więc ten punkt będzie się znajdował na starcie
        bestOfWorst = start;

        //punkt końcowy to pozycja gracza na mapie w pikselach, przekonwertowana na miarę w punktach
        end = grid[(int)Math.floor(player.position.x / TILE_WIDTH)][(int)Math.floor(player.position.y / TILE_WIDTH)];

        //dodajemy start do zbioru otwartego - jest on jedynym znanym punktem ścieżki na początku
        openSet.add(start);

        //Wykonujemy iteracje dopóki będą istaniały jakieś punkty do sprawdzenia
        while (openSet.size() > 0)
        {
            //winner to indeks punktu o najlepszej wartości f - czyli prawdopopdobnie punku przyszłej ścieżki
            int winner = 0;
            for (int i = 0; i < openSet.size(); i++)
                if (openSet.get(i).f < openSet.get(winner).f)
                    winner = i;

            current = openSet.get(winner);

            //sprawdzamy, czy ten punkt nie jest może najlepszym z niedostępnej ścieżki, na wypadek końca algorytmu
            if (Utils.dist(player.position.x, player.position.y, current.i * TILE_WIDTH, current.j * TILE_WIDTH) <
                Utils.dist(player.position.x, player.position.y, bestOfWorst.i * TILE_WIDTH, bestOfWorst.j * TILE_WIDTH))
                bestOfWorst = current;

            //jeżeli dotarliśmy do celu, kończymy algorytm
            if (current == end)
                break;

            //każdy punkt po iteracji jest usuwany ze zbioru otwartego i dodawany do zamkniętego - z ich definicji
            openSet.remove(current);
            closedSet.add(current);
            List<Spot> neighbors = current.neighbors;

            //iterujemy przez wszystkich sąsaidów w poszukiwaniu potencjalnego kandydata na następny punkt ścieżki
            for (Spot neighbor : neighbors)
            {
                //jeśli sąsaid jest już w zbiorze zamkniętym, lub jest przeskodą, nie interesuje nas
                if (closedSet.contains(neighbor) || neighbor.wall)
                    continue;

                //potencjalna nowa wartość g dla sąsaida
                double tempG = current.g + Utils.dist(neighbor.i, neighbor.j, current.i, current.j);

                //mówi, czy sąsaid ten będzie częścią jednej z potencjalnych ścieżek
                boolean newPath = false;

                //jeżeli sąsiad istnieje już w zbiorze otwartym, możliwe jest, że tempG jest lesze od jego g
                //w takim przypadku, staje się częścią jednej z potencjalnych ścieżek
                if (openSet.contains(neighbor))
                {
                    if (tempG < neighbor.g)
                    {
                        neighbor.g = tempG;
                        newPath = true;
                    }
                }
                //jeśli nie ma go w zbiorze otwartym, dodajemy go tam, staje się częścią jednej z potencjalnych ścieżek
                else
                {
                    neighbor.g = tempG;
                    newPath = true;
                    openSet.add(neighbor);
                }

                //jeżeli sąsiad jest częścią jednej z potencjalnych ścieżek, estymujemy jego
                //koszt dojścia do punktu końcowego - jest to suma kosztu dojścia do niego od początku
                //plus estymata kosztu dojścia do końca - estymujemy najlepszy przypadek czyli odległość w prostej linii
                //ponadto, w strukturze ścieżki, obecny punkt jest poprzednikiem sąsiada, więc przypisujemy
                //go do zmiennej previous
                if (newPath)
                {
                    neighbor.heuristic = Utils.dist(neighbor.i, neighbor.j, end.i, end.j);
                    neighbor.f = neighbor.g + neighbor.heuristic;
                    neighbor.previous = current;
                }

            }
        }
        //decydujemy, co ostatecznie ma być celem:
        //jeśli przerwaliśmy pętlę z powodu wyczerpania się zbioru otwartego, znaczy to, że
        //nie istnieje ścieżka do końa i musimy zadowolić się punktem bestOfWorst
        //w przeciwnym wypadku, ustalamy cel na punkt końcowy
        Spot destination;
        if (openSet.size() == 0)
            destination = bestOfWorst;
        else
            destination = current;

        //ustalamy zmienną objective, poruszając się po ścieżce od końca
        //objective jest tak naprawdę jedynie pierwszym punktem (poza początkowym) w ścieżce
        //ponieważ i tak wykonujemy tą funkcję co klatkę, bo gracz ciągle zmienia swoją pozycją
        Spot temp = destination;
        while (temp.previous != null)
        {
            objective = new Vector(temp.i + 0.5, temp.j + 0.5).mult(TILE_WIDTH);
            temp = temp.previous;
        }

        //sprawdzamy, czy na komórce na którą wskazuje objective znajduje się ekplozja
        //jeżeli tak jest, w tej klatce gry nie poruszamy się
        if(currentLevel.map[(int)Math.floor(objective.x / TILE_WIDTH)][(int)Math.floor(objective.y / TILE_WIDTH)].getExplosion() != null)
            objective = position;

        //ustalamy zmienną facing na podstawie trygonometrii
        Vector velocity = objective.copy().sub(position);
        String[] animationKeys = { "right", "down", "left", "up" };
        double orientation = velocity.heading() >= 0 ? velocity.heading() : velocity.heading() + Math.PI * 2;
        int index = (int)Math.floor((orientation / (Math.PI * 2)) * animationKeys.length);
        facing = animationKeys[index];
        objectiveSetTS = GameManager.frameCount;
    }

    /**
     * Dodaje animacje do menedżera. Wykorzystuje zmienną {@link Utils#smartEnemyFrames}
     */
    void setAnimations()
    {
        int animationSpeed = Integer.parseInt(properties.getProperty("animationSpeed"));
        walkingAnimations = new AnimationManager();
        walkingAnimations.addAnimation(Utils.smartEnemyFrames[0], animationSpeed, "left");
        walkingAnimations.addAnimation(Utils.smartEnemyFrames[1], animationSpeed, "right");
        walkingAnimations.addAnimation(Utils.smartEnemyFrames[2], animationSpeed, "up");
        walkingAnimations.addAnimation(Utils.smartEnemyFrames[3], animationSpeed, "down");
    }
}
