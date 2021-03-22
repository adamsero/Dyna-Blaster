package Client.Game.Managers;

import javax.swing.*;
import java.util.HashMap;

/**
 * Klasa zarządzająca zestawem animacji
 */
public class AnimationManager
{
    /**
     * Klasa obsługująca pojedyńczą animację
     */
    private static class Animation
    {
        /**
         * Tablica obrazków animacji
         */
        private ImageIcon[] frames;

        /**
         * Ilość kaltek gry przez czas których wyświetlany jest jeden obrazek animacji
         */
        private int framesPerImage;

        /**
         * Ilość klatek, jaka upłynęła od ostatniej zmiany obrazka
         */
        private int framesElapsed = 0;

        /**
         * Indeks obecnie wyświetlanego obrazka w tablicy
         */
        private int currentFrameIndex = 0;

        /**
         * Konstruktor, przypisuje wartości zmeinnym
         * @param arr Tablica obrazków animacji
         * @param framesPerImage_ Ilość kaltek gry przez czas których wyświetlany jest jeden obrazek animacji
         */
        Animation(ImageIcon[] arr, int framesPerImage_)
        {
            frames = arr;
            framesPerImage = framesPerImage_;
        }

        /**
         * Zmienia obecnie wyświetlany obrazek w animacji na kolejny
         */
        public void step()
        {
            framesElapsed++;
            if (framesElapsed >= framesPerImage)
            {
                currentFrameIndex = (currentFrameIndex + 1) % frames.length;
                framesElapsed = 0;
            }
        }

        /**
         * Zwraca obecny obrazek animacji
         * @return Obecny obrazek animacji
         */
        public ImageIcon getFrame()
        {
            return frames[currentFrameIndex];
        }
    }

    /**
     * Mapa animacji obsługiwanych przez menadżera
     */
    private HashMap<String, Animation> animations = new HashMap<>();

    /**
     * Referencja do obecnej animacji
     */
    private Animation currentAnimation = null;

    /**
     * Dodaje animacje do mapy
     * @param frames Tablica obrazków animacji
     * @param fpf Ilość kaltek gry przez czas których wyświetlany jest jeden obrazek animacji
     * @param name Klucz do mapy - {@link #animations}
     */
    public void addAnimation(ImageIcon[] frames, int fpf, String name)
    {
        Animation animation = new Animation(frames, fpf);
        animations.put(name, animation);
        currentAnimation = animation;
    }

    /**
     * Ustawia obecną animację
     * @param name Klucz mapy animacji
     */
    public void setCurrentAnimation(String name)
    {
        Animation currentAnimationUnchecked = animations.get(name);
        if(currentAnimationUnchecked == null)
        {
            System.out.println("Nie istnieje animacja o takiej nazwie");
            return;
        }
        currentAnimation = currentAnimationUnchecked;
    }

    /**
     * Wywołuje metodę {@link Animation#step()} dla obecnej animacji
     */
    public void step()
    {
        if (currentAnimation != null)
            currentAnimation.step();
    }

    /**
     * Zwraza obecny obrazek obecnej animacji
     * @return Obecny obrazek obecnej animacji
     */
    public ImageIcon getFrame()
    {
        if (currentAnimation == null)
            return null;
        return currentAnimation.getFrame();
    }
}
