package peripheralsimulation.engine;

import model.modeling.atomic;
import model.modeling.message;
import GenCol.entity;

public class InputGenerator extends atomic {

    public InputGenerator() {
        super("InputGenerator");
        addOutport("start");
    }

    @Override
    public void deltint() {
        passivate(); // Po každom kroku model sa deaktivuje, kým nedostane ďalší podnet.
    }

    @Override
    public message out() {
        message m = new message();
        System.out.println("[InputGenerator] Posielam podnet 'start'");
        m.add(makeContent("start", new entity("StartSignal")));
        return m;
    }

    @Override
    public double ta() {
        return 5.0; // Generuje vstup každých 5 časových jednotiek
    }
}
