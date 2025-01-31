package peripheralsimulation.model;

import model.modeling.atomic;
import model.modeling.message;
import GenCol.entity;

public class SCTimerModel extends atomic {
    private int timerValue;

    public SCTimerModel() {
        super("SCTimer");
        addInport("start");
        addOutport("timeout");
        timerValue = 0;
    }

    @Override
    public void deltint() {
        if (timerValue > 0) {
            timerValue--;
            System.out.println("[SCTimerModel] Odpočítavam: " + timerValue);
        }
    }

    @Override
    public void deltext(double e, message x) {
        if (messageOnPort(x, "start", 0)) {
            timerValue = 10; // Po prijatí signálu sa časovač spustí na 10 tickov
            System.out.println("[SCTimerModel] Časovač spustený na 10 tickov.");
        }
    }

    @Override
    public message out() {
        message m = new message();
        if (timerValue == 0) {
            System.out.println("[SCTimerModel] Timeout! Odosielam správu.");
            m.add(makeContent("timeout", new entity("TimerExpired")));
        }
        return m;
    }

    @Override
    public double ta() {
        return timerValue > 0 ? 1.0 : INFINITY; // Ak beží, znížime ho každú jednotku času.
    }
}
