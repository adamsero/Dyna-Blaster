package Client.Sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer implements Runnable{

    /**
     * Typ wyliczeniowy odpowiedzialny za przechowywanie informacji o stanie odtwarzanego dźwięku
     */
    private enum Status {PLAYING,STOPPED,PAUSED}

    /**
     * Zmienna typu {@code File} przechowująca plik audio do odtwarzania
     */
    private File file;
    /**
     * Zmienna typu {@code Clip} realizująca odtwarzanie dźwięku
     */
    private Clip clip;
    /**
     * Zmienna typu {@code AudioInputStream} przechowująca strumień wejściowy danych do odtwarzania
     */
    private AudioInputStream audioInputStream;

    /**
     * Zmienna określający obecny stan odtwarzania dźwięku
     */
    public Status status;
    /**
     * Zmienna typu {@code boolean} określająca czy dźwięk ma być zapętlany
     */
    public boolean loop;

    /**
     * Konstruktor
     * @param file przekazuje plik do odtwarzania
     * @param loop określa czy dźwięk ma być zapętlany
     */
    public SoundPlayer(File file,boolean loop){
        status = Status.STOPPED;
        this.loop = loop;
        this.file = file;
    }

    /**
     * Metoda rozpoczynająca odtwarzanie dźwięku na wątku przy pomocy {@link SoundPlayer#play()}
     */
    @Override
    public void run(){
        try {
            play();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Metoda rozpoczynająca odtwarzanie dźwięku
     * @throws IOException wyjątek
     * @throws InterruptedException wyjątek
     * @throws LineUnavailableException wyjątek
     * @throws UnsupportedAudioFileException wyjątek
     */
    private void play() throws IOException, InterruptedException, LineUnavailableException, UnsupportedAudioFileException {
        if (status == Status.STOPPED){
            audioInputStream = AudioSystem.getAudioInputStream(file.getAbsoluteFile());
            AudioListener audioListener = new AudioListener();
            try{
                clip = AudioSystem.getClip();
                clip.addLineListener(audioListener);
                clip.open(audioInputStream);
                if(loop){
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
                try {
                    clip.start();
                    this.status = Status.PLAYING;
                    audioListener.waitUntilDone();
                }
                finally{
                    clip.close();
                }
            }
            finally{
                audioInputStream.close();
                this.status = Status.STOPPED;
            }
        }
    }

}
