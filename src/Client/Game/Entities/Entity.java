package Client.Game.Entities;

import Client.Game.Managers.Level;
import Client.Game.Managers.GameManager;
import Client.Game.Environment.Tile;
import Client.Game.Utilities.Vector;

/**
 * Klasa abstrakcyjna reprezentująca jednostkę gry - gracza lub przeciwnika
 */
public abstract class Entity
{
    /**
     * Pozycja jednostki na mapie
     */
    public Vector position;

    /**
     * Referencja do obecnego poziomu gry
     */
    protected Level currentLevel;

    /**
     * Liczba żyć postaci
     */
    int lives;

    /**
     * Flaga mówiąca czy postać została zabita
     */
    boolean isDead = false;

    /**
     * Konstruktor, ustala wartość zmiennej poziomu gry
     * @param level Referencja do obecnego poziomu gry
     */
    public Entity(Level level)
    {
        currentLevel = level;
    }

    /**
     * Zwraca komórkę, na której obecnie znajduje się jednostka, wykorzystując jej pozycję
     * @return Komórka, na której obecnie znajduje się jednostka
     */
    public Tile currentTile() {
        return currentLevel.map[(int)Math.floor(position.x / GameManager.DEFAULT_TILE_WIDTH)][(int)Math.floor(position.y / GameManager.DEFAULT_TILE_WIDTH)];
    }

    /**
     * Sprawdza, czy jednostka koliduje z inną
     * @param entityPos Pozycja drugiej jednostk
     * @param colliderWidth Szerokość fizycznej reprezentacji jednostki
     * @param colliderHeight Wysokość fizycznej reprezentacji jednostki
     * @return Wartość logiczna kolizji
     */
    protected boolean collideEntity(Vector entityPos, double colliderWidth, double colliderHeight)
    {
        double r1x = position.x - colliderWidth / 2, r1y = position.y - colliderHeight / 2;
        double r1w = colliderWidth, r1h = colliderHeight;

        double r2x = entityPos.x - colliderWidth / 2, r2y = entityPos.y - colliderHeight / 2;
        double r2w = colliderWidth, r2h = colliderHeight;

        return (r1x + r1w >= r2x && r1x <= r2x + r2w && r1y + r1h >= r2y && r1y <= r2y + r2h);
    }

    /**
     * metoda obsługująca kolizje {@code Entity} z eksplozją
     * @param tile komorka na ktorej znajduje sie explozja
     * @return informacje {@code boolean} czy zachodzi kolizja
     */
    public boolean collideExplosion(Tile tile){
        if (tile.getExplosion() != null && tile.getExplosion().canHurt()) {
            return this.collideEntity(tile.getExplosion().position,Player.colliderWidth,Player.colliderHeight);
        }
        else {
            return false;
        }
    }

    /**
     * Metoda zmniejszająca liczbę żyć
     */
    void loseLife(){
        lives--;
    }

    /**
     * Metoda getter zwracająca liczbę żyć
     * @return {@code int} liczba żyć
     */
    public int getLives() {
        return lives;
    }

    /**
     * Metoda ustawiająca {@code boolean} {@link #isDead}
     * @param dead wartosc jaka ma przyjac {@link #isDead}
     */
    public void setDead(boolean dead) {
        isDead = dead;
    }

    /**
     * Metoda getter do pola {@link #isDead}
     * @return zmienna {@link #isDead}
     */
    public boolean isDead() {
        return isDead;
    }
}
