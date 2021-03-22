package Client.Sound;

import java.io.File;

/**
 * Klasa zarządzająca muzyką w tle gry oraz efektami dzwiękoweymi przy poszczególnych eventach
 * */
public class SoundManager {

    /**
     * Zmienna typu File przechowująca plik audio z pozytywnym dźwiękiem kliknięcia
     */
    private File clickPositiveSoundFile;
    /**
     * Zmienna typu File przechowująca plik audio z negatywnym dźwiękiem kliknięcia
     */
    private File clickNegativeSoundFile;
    /**
     * Zmienna typu File przechowująca plik audio z motywem tła gry
     */
    private File backgroundSoundFile;

    /**
     * Zmienna typu File przechowująca plik audio z motywem końca gry
     */
    private File gameOverSoundFile;

    /**
     * Zmienna typu File przechowująca plik audio z dźwiękiem podniesienia znajdzki
     */
    private File collectSoundFile;

    /**
     * Zmienna typu File przechowująca plik audio z dźwiękiem przejścia na kolejny poziom
     */
    private File gateSoundFile;

    /**
     * Zmienna typu File przechowująca plik audio z dźwiękiem eksplozji bomby
     */
    private File explosionSoundFile;

    /**
     * Zmienna typu File przechowująca plik audio z dźwiękiem podkładania bomby
     */
    private File bombSoundFile;

    /**
     * Zmienna typu File przechowująca plik audio z dźwiękiem uderzenia postaci
     */
    private File hitSoundFile;

    /**
     * Wątek odpowiadający za odtwarzanie głównego motywu muzycznego
     */
    private Thread backgroundThemeThread;

    /**
     * Kontruktor
     * Tworzy instancje klasy {@code SoundManager}
     * ustawia obiekty typu File {@link Client.Sound.SoundManager#clickPositiveSoundFile}, {@link Client.Sound.SoundManager#clickNegativeSoundFile}, {@link Client.Sound.SoundManager#backgroundSoundFile}
     */
    public SoundManager(){
        clickPositiveSoundFile = new File("./data/sounds/click_sound_positive.wav").getAbsoluteFile();
        clickNegativeSoundFile = new File("./data/sounds/click_sound_negative.wav").getAbsoluteFile();
        backgroundSoundFile = new File("./data/sounds/background_theme.wav").getAbsoluteFile();
        gameOverSoundFile = new File("./data/sounds/gameover_theme.wav").getAbsoluteFile();
        collectSoundFile = new File("./data/sounds/collect_sound.wav").getAbsoluteFile();
        gateSoundFile = new File("./data/sounds/gate_sound.wav").getAbsoluteFile();
        explosionSoundFile = new File("./data/sounds/explosion_sound.wav").getAbsoluteFile();
        bombSoundFile = new File("./data/sounds/bomb_sound.wav").getAbsoluteFile();
        hitSoundFile = new File("./data/sounds/player_hit_sound.wav").getAbsoluteFile();
    }

    /**
     * Odtwarza negatywny dźwięk kliknięcia
     */
    public void playClickNegativeSound(){
        new Thread(new SoundPlayer(clickNegativeSoundFile,false)).start();
    }

    /**
     * Odtwarza pozytywny dźwięk kliknięcia
     */
    public void playClickPositiveSound(){
        new Thread(new SoundPlayer(clickPositiveSoundFile,false)).start();
    }

    /**
     * Odtwarza dźwięk tła gry
     */
    public void playBackgroundTheme(){
        backgroundThemeThread = new Thread(new SoundPlayer(backgroundSoundFile,true));
        backgroundThemeThread.start();
    }

    /**
     * Zatrzymuje odtwarzanie głównego motyu muzycznego
     */
    public void stopBackgroundTheme(){
        try {
            backgroundThemeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Odtwarza dźwięk przegranej
     */
    public void playGameOverTheme(){
        new Thread(new SoundPlayer(gameOverSoundFile,true)).start();
    }

    /**
     * Odtwarza dźwięk podniesienia znajdzki
     */
    public void playCollectSound(){
        new Thread(new SoundPlayer(collectSoundFile,false)).start();
    }

    /**
     * Odtwarza dźwięk zmiany poziomu
     */
    public void playGateSound(){
        new Thread(new SoundPlayer(gateSoundFile,false)).start();
    }

    /**
     * Odtwarza dźwięk eksplozji bomby
     */
    public void playExplosionSound(){
        new Thread(new SoundPlayer(explosionSoundFile,false)).start();
    }

    /**
     * Odtwarza dźwięk podkładania bomby
     */
    public void playBombSound(){
        new Thread(new SoundPlayer(bombSoundFile,false)).start();
    }

    /**
     * Odtwarza dźwięk uderzenia postaci
     */
    public void playHitSound(){
        new Thread(new SoundPlayer(hitSoundFile,false)).start();
    }
}
