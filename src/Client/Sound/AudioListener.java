package Client.Sound;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;


/**
 * Klasa obsługująca zakończenie odtwarzania dźwięku
 */
public class AudioListener implements LineListener {
    /**
     * Zmienna typu {@code boolean} określająca stan dźwięku ustawiana przez listener
     */
    private boolean done = false;

    /**
     * Metoda pozwalająca określić czy odtwarzanie(strumień) zostało zakończone
     * @param event zdarzenie typu LineEvent pozwalające okreslić co dzieje się z dźwiękiem
     */
    @Override
    public synchronized void update(LineEvent event) {

        LineEvent.Type eventType = event.getType();

        if (eventType == LineEvent.Type.STOP || eventType == LineEvent.Type.CLOSE) {
            done = true;
            notifyAll();
        }
    }

    /**
     * Metoda oczekująca na ukończenie odtwarzania przez wątek i zmianę statusu {@link AudioListener#done}
     * @throws InterruptedException Rzucone, przy okazji {@code notify()} lub {@code notifyAll()}
     */
    synchronized void waitUntilDone() throws InterruptedException {
        while (!done) { wait(); }
    }
}
