package peripheralsimulation.engine;

import model.modeling.atomic;
import model.modeling.message;
import GenCol.entity;

/**
 * CounterInputGenerator je DEVS atomický model, ktorý generuje vstupy pre čítač.
 * Každých 3 časové jednotky posiela signál "increment", ktorý zvyšuje hodnotu čítača.
 */
public class CounterInputGenerator extends atomic {

    /**
     * Konštruktor modelu CounterInputGenerator.
     * Inicializuje názov modelu a pridáva výstupný port "increment".
     */
    public CounterInputGenerator() {
        super("CounterInputGenerator");
        addOutport("increment");
    }

    /**
     * Táto metóda sa volá po vykonaní vnútornej udalosti (po naplánovanom čase).
     * Model sa deaktivuje a čaká na ďalší časový krok.
     */
    @Override
    public void deltint() {
        passivate(); // Po každom kroku model sa deaktivuje, kým nedostane ďalší podnet.
    }

    /**
     * Táto metóda generuje výstupnú správu "increment", ktorú odosiela čítaču.
     * Pri každom spustení simulácie posiela signál na zvýšenie hodnoty čítača.
     * 
     * @return message obsahujúca entitu "INCREMENT" na výstupnom porte "increment".
     */
    @Override
    public message out() {
        message m = new message();
        System.out.println("[CounterInputGenerator] Posielam signál INCREMENT");
        m.add(makeContent("increment", new entity("INCREMENT")));
        return m;
    }

    /**
     * Určuje, kedy sa má spustiť ďalšia vnútorná udalosť modelu.
     * Každých 3 časové jednotky vygeneruje signál "increment".
     * 
     * @return Čas do ďalšej udalosti (3.0 jednotky času).
     */
    @Override
    public double ta() {
        return 3.0; // Každé 3 časové jednotky pošle signál "increment"
    }
}
