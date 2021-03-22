package Client.Game.Managers;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Klasa służąca do zarządzania plikami i wczytywania plików XML przechowywujących informację
 * o elementach interfejsu użytkownika UI
 * Docelowo klasa ma obsługiwać też wczytywanie planszy gry - obecnie jest to realizowane w inny sposób
 */
public class  FileManager {

    /**
     * Pole typu {@code DocumentBuilderFactory} pozwalające na odczyt XML
     */
    private DocumentBuilderFactory builderFactory;

    /**
     * Pole typu {@code DocumentBuilder} pozwalające na odczyt XML
     */
    private DocumentBuilder builder;

    /**
     * Konstruktor
     * Inicjuje pola klasy
     */
    public FileManager(){
        builderFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda służąca do parsowania pliku XML
     * @param path ścieżka do pliku
     * @return obiekt typu {@code Document} reprezentujący rozparsowany plik XML
     */
    public Document parseXML(String path){
        Document document = null;

        try {
            document = builder.parse(new FileInputStream(path));
            return document;
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return document;
    }
}
