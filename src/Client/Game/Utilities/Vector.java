package Client.Game.Utilities;

/**
 * Klasa reprezentująca fizyczny wektor
 */
public class Vector
{
    /**
     * Składowa x wektora
     */
    public double x;

    /**
     * Składowa y wektora
     */
    public double y;

    /**
     * Przypisuje wartości do składowych
     * @param x Składowa x wektora
     * @param y Składowa y wektora
     */
    public Vector(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Dodaje wektor do obecnego oraz zwraca wynik
     * @param v Wektor, który zostaje dodany
     * @return Wynik dodawania wektorów
     */
    public Vector add(Vector v)
    {
        x += v.x;
        y += v.y;
        return this;
    }

    /**
     * Odejmuje wektor od obecnego oraz zwraca wynik
     * @param v Wektor, który zostaje odjęty
     * @return Wynik odejmowania wektorów
     */
    public Vector sub(Vector v)
    {
        x -= v.x;
        y -= v.y;
        return this;
    }

    /**
     * Mnoży wektor przez skalar oraz zwraca wynik
     * @param k Skalar
     * @return Wynik mnożenia
     */
    public Vector mult(double k)
    {
        x *= k;
        y *= k;
        return this;
    }

    /**
     * Oblicza długość wektora
     * @return Długość wektora
     */
    public double mag()
    {
        return Math.sqrt((x * x) + (y * y));
    }

    /**
     * Sprowadza wektor do wersora (zmienia długość na 1)
     */
    private void normalize()
    {
        double mag = mag();
        if(mag == 0 || mag == 1)
            return;
        x /= mag;
        y /= mag;
    }

    /**
     * Ustawia długość wektora
     * @param desiredMag Żądana długość
     */
    public void setMag(double desiredMag)
    {
        normalize();
        mult(desiredMag);
    }

    /**
     * Zwraca kąt nachylenia wektora
     * @return Kąt nachylenia
     */
    public double heading()
    {
        return Math.atan2(y, x);
    }

    /**
     * Zwraca kopię wektora
     * @return Kopia wektora
     */
    public Vector copy()
    {
        return new Vector(x, y);
    }

    /**
     * Zwraca reprezentację tekstową wektora
     * @return Tekst reprezentujący wektor
     */
    @Override
    public String toString()
    {
        return "x: " + x + ", y: " + y;
    }
}
