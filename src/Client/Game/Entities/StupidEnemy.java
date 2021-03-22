package Client.Game.Entities;

import Client.Game.Managers.GameManager;
import Client.Game.Managers.AnimationManager;
import Client.Game.Managers.Level;
import Client.Game.Utilities.Vector;
import Client.Utils;

/**
 * Klasa reprezentująca "głupiego" przeciwnika - poruszającego się w sposób losowy
 */
public class StupidEnemy extends Enemy
{
    /**
     * Wywołuje konstruktor klasy rodzica
     * @param x Składowa x pozycji komórki, na której przseciwnik się pojawił
     * @param y Składowa y pozycji komórki, na której przseciwnik się pojawił
     * @param level Referencja do obecnego poziomu gry
     */
    public StupidEnemy(int x, int y, Level level) {
        super(x, y, level);
        pointValue = Double.parseDouble(properties.getProperty("pointValueStupid"));
    }

    /**
     * Obiera cel poruszania się - jest to losowa komórka spośród czterech dookoła przecinika,
     * o ile nie jest przeszkodą terenową.
     * @param player Referencja do postaci gracza
     */
    void setObjective(Player player)
    {
        int x = (int)Math.floor(position.x / GameManager.DEFAULT_TILE_WIDTH), y = (int)Math.floor(position.y / GameManager.DEFAULT_TILE_WIDTH);
        int[] directionsInRandomOrder = Utils.noRepGen(4, 4);
        for (Integer directionsInt : directionsInRandomOrder)
        {
            switch(directionsInt)
            {
                case 0:
                    if (x <= 0 || currentLevel.map[x - 1][y].getTileType().obstacle)
                        continue;
                    objective = position.copy().add(new Vector(-GameManager.DEFAULT_TILE_WIDTH, 0));
                    facing = "left";
                    break;
                case 1:
                    if (x >= currentLevel.mapWidth - 1 || currentLevel.map[x + 1][y].getTileType().obstacle)
                        continue;
                    objective = position.copy().add(new Vector(GameManager.DEFAULT_TILE_WIDTH, 0));
                    facing = "right";
                    break;
                case 2:
                    if (y <= 0 || currentLevel.map[x][y - 1].getTileType().obstacle)
                        continue;
                    objective = position.copy().add(new Vector(0, -GameManager.DEFAULT_TILE_WIDTH));
                    facing = "up";
                    break;
                case 3:
                    if (y >= currentLevel.mapHeight - 1 || currentLevel.map[x][y + 1].getTileType().obstacle)
                        continue;
                    objective = position.copy().add(new Vector(0, GameManager.DEFAULT_TILE_WIDTH));
                    facing = "down";
                    break;
            }
        }
        if(currentLevel.map[(int)Math.floor(objective.x / GameManager.DEFAULT_TILE_WIDTH)][(int)Math.floor(objective.y / GameManager.DEFAULT_TILE_WIDTH)].getExplosion() != null)
            objective = position;
        objectiveSetTS = GameManager.frameCount;
    }

    /**
     * Dodaje animacje do menedżera. Wykorzystuje zmienną {@link Utils#stupidEnemyFrames}
     */
    void setAnimations()
    {
        int animationSpeed = Integer.parseInt(properties.getProperty("animationSpeed"));
        walkingAnimations = new AnimationManager();
        walkingAnimations.addAnimation(Utils.stupidEnemyFrames[0], animationSpeed, "left");
        walkingAnimations.addAnimation(Utils.stupidEnemyFrames[1], animationSpeed, "right");
        walkingAnimations.addAnimation(Utils.stupidEnemyFrames[2], animationSpeed, "up");
        walkingAnimations.addAnimation(Utils.stupidEnemyFrames[3], animationSpeed, "down");
    }
}
