package peripheralsimulation.engine;

import model.modeling.*;
import model.simulation.*;
import GenCol.*;

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
        // Internal transition logic: Decrement timer
        if (timerValue > 0) {
            timerValue--;
        }
    }

    @Override
    public void deltext(double e, message x) {
        // External transition logic: Start timer
        if (messageOnPort(x, "start", 0)) {
            timerValue = 100; // Set initial timer value
        }
    }

    @Override
    public message out() {
        // Generate output message when timer reaches 0
        message m = new message();
        if (timerValue == 0) {
            m.add(makeContent("timeout", new entity("TimerExpired")));
        }
        return m;
    }

    @Override
    public double ta() {
        // Time advance: 1 tick per step
        return timerValue > 0 ? 1 : INFINITY;
    }
}
