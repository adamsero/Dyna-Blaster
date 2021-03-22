package Client.Game.Utilities;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.TemporalUnit;

/**
 * Klasa przechowująca informacje o danym interwale czasowym gry
 */
public class TimeInterval {

    /**
     * Pole typu {@code Instant} reprezentujące czas startu interwału
     */
    private final Instant startTime;

    /**
     * Pole typu {@code Instant} reprezentujące czas końca interwału
     */
    private final Instant endTime;

    /**
     * Pole typu {@code Duration} reprezentujące długość interwału
     */
    private final Duration interval;

    /**
     * Konstruktor
     * @param startTime inicjalizuje początek interwału
     * @param endTime inicjalizuje koniec interwału
     */
    public TimeInterval(Instant startTime,Instant endTime){
        this.startTime = startTime;
        this.endTime = endTime;
        this.interval = calculateTime();
    }

    /**
     * Metoda przeliczająca długość interwału
     * @return obiekt typu {@code Duration} reprezentujący długość interwału
     */
    private Duration calculateTime(){
        return Duration.between(startTime,endTime);
    }

    /**
     * Getter dla pola {@link TimeInterval#interval}
     * @return obiekt typu {@code Duration} reprezentujący długość interwału
     */
    public Duration getInterval(){
        return interval;
    }
}
