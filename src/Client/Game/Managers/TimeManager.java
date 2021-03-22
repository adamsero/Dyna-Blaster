package Client.Game.Managers;

import Client.Game.Utilities.TimeInterval;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;

/**
 * Klasa odpowiadająca za zarządzanie czasem gry oraz punktacją ze względu na czas
 * */
public class TimeManager {

    /**
     * Zmienna typu {@code Instant} reprezentująca czas startu gry
     */
    private Instant startTime;

    /**
     * Zmienna typu {@code Instant} reprezentująca obecny czas pauzy gry
     */
    private Instant pauseTime;

    /**
     * Zmienna typu {@code Instant} reprezentująca obecny wznowienia gry
     */
    private Instant resumeTime;

    /**
     * Zmienna typu {@code Instant} reprezentująca czas zatrzymania gry
     */
    private Instant stopTime;

    /**
     * Zmienna typu {@code Duration} reprezentująca całkowity czas gry
     */
    private Duration totalGameTime;

    /**
     * Zmienna typu {@code double} określająca mnożnik punktów
     */
    private double multiplier;

    /**
     * Zmienna typu {@code boolean} określająca czy gra została zapauzowana
     */
    public boolean paused = false;

    /**
     * Lista dowiązaniowa przechowująca interwały gry typu {@code TimeInterval}
     */
    private LinkedList<TimeInterval> intervals = new LinkedList<>();

    /**
     * Konstruktor
     * @param multiplier określa mnożnik punktów dla danego obiektu
     */
    public TimeManager(double multiplier){
        this.multiplier = multiplier;
    }

    /**
     * Metoda rozpoczynająca odliczanie czasu gry
     */
    public void start(){
        this.startTime = Instant.now();
    }

    /**
     * Metoda zatrzymująca odliczanie czasu gry
     */
    public void pause(){
        this.pauseTime = Instant.now();

        //sytuacja gdy pierwsza pauza
        if (resumeTime == null){
            intervals.add(new TimeInterval(startTime,pauseTime));
        }
        else{
            intervals.add(new TimeInterval(resumeTime,pauseTime));
        }
        paused = true;
    }

    /**
     * Metoda wznawiająca odliczanie czasu gry
     */
    public void resume(){
        this.resumeTime = Instant.now();
        paused = false;
    }

    /**
     * Metoda zatrzymująca odliczanie czasu gry
     */
    public void stop(){
        this.stopTime = Instant.now();

        //sytuacja gdy gra nigdy nie pauzowana
        if(resumeTime == null){
            intervals.add(new TimeInterval(startTime,stopTime));
        }
        else {
            intervals.add(new TimeInterval(resumeTime,stopTime));
        }
    }

    /**
     * Metoda obliczająca obecny czas gry
     * @return obiekt typu {@code Duration} reprezentujący obecny czas gry
     */
    Duration calculateCurrentGameTime(){
        Instant currentTime = Instant.now();
        if(resumeTime == null){
            return Duration.between(startTime,currentTime);
        }
        else{
            return calculateTotalGameTime().plus(Duration.between(resumeTime,currentTime));
        }
    }

    /**
     * Metoda obliczająca całkowity końcowy czas gry
     * @return obiekt typu {@code Duration} reprezentujący końcowy czas gry
     */
    private Duration calculateTotalGameTime(){
        Duration total = null;
        for (TimeInterval interval : intervals) {
            if (total == null){
                total = interval.getInterval();
            }
            else{
                total = total.plus(interval.getInterval());
            }
        }
        totalGameTime = total;
        return total;
    }

    /**
     * Metoda obliczająca ilość punktów do odjęcia na podstawie mnożnika
     * @return ilość punktów do odjecia od punktów startowych {@code double}
     */
    public double calculatePointsToSubtract(){
        return calculateTotalGameTime().getSeconds() * multiplier;
    }

    /**
     * Getter pola {@link TimeManager#startTime}
     * @return obiekt typu {@code Instant} reprezentujący czas startu gry
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Metoda obliczająca ilość punktów do odjęcia na podstawie mnożnika
     * @return ilość punktów do odjecia od punktów startowych {@code double}
     */
    public double calculatePointsToSubtractDuringGame(){
        return calculateCurrentGameTime().getSeconds() * multiplier;
    }

}