package peripheralsimulation.model;

import model.modeling.atomic;
import model.modeling.message;
import GenCol.entity;

/**
 * CounterModel je DEVS atomický model, ktorý reprezentuje čítač.
 * Spracováva vstupné správy "increment" a zvyšuje svoju internú hodnotu.
 * Každých 5 časových jednotiek odosiela svoju aktuálnu hodnotu na výstup.
 */
public class CounterModel extends atomic implements PeripheralModel {
    private int count; // Aktuálna hodnota čítača

    /**
     * Konštruktor modelu CounterModel.
     * Inicializuje názov modelu a pridáva vstupný port "increment" a výstupný port "output".
     */
    public CounterModel() {
        super("CounterModel");
        addInport("increment");
        addOutport("output");
        count = 0;
    }

    /**
     * Externá prechodová funkcia (delta-ext).
     * Spracováva prichádzajúce správy na porte "increment" a zvyšuje hodnotu čítača.
     * 
     * @param e Uplynutý čas od poslednej udalosti.
     * @param x Vstupná správa obsahujúca signál "increment".
     */
    @Override
    public void deltext(double e, message x) {
        if (messageOnPort(x, "increment", 0)) {
            count++;
            System.out.println("[CounterModel] Prijatý signál INCREMENT, nová hodnota: " + count);
        }
    }

    /**
     * Interná prechodová funkcia (delta-int).
     * Po každom naplánovanom výstupe model zostáva aktívny a udržiava svoju hodnotu.
     */
    @Override
    public void deltint() {
        passivate(); // Čítač ostáva v aktuálnom stave, kým nepríde nový podnet.
    }

    /**
     * Výstupná funkcia (lambda).
     * Posiela aktuálnu hodnotu čítača na výstup každých 5 časových jednotiek.
     * 
     * @return message obsahujúca aktuálnu hodnotu čítača.
     */
    @Override
    public message out() {
        message m = new message();
        System.out.println("[CounterModel] Odosielam aktuálnu hodnotu: " + count);
        m.add(makeContent("output", new entity(String.valueOf(count))));
        return m;
    }

    /**
     * Časová funkcia (ta).
     * Určuje interval, v ktorom model odosiela výstupnú hodnotu.
     * 
     * @return Čas do ďalšej udalosti (5.0 jednotiek času).
     */
    @Override
    public double ta() {
        return 5.0; // Každých 5 časových jednotiek posiela výstupnú hodnotu
    }
}

