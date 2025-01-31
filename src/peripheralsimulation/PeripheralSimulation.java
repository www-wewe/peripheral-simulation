package peripheralsimulation;

import model.modeling.digraph;
import peripheralsimulation.engine.InputGenerator;
import peripheralsimulation.model.SCTimerModel;

public class PeripheralSimulation extends digraph {

    public PeripheralSimulation() {
        super("PeripheralSimulation");

        InputGenerator generator = new InputGenerator();
        SCTimerModel timer = new SCTimerModel();

        add(generator);
        add(timer);

        addInport("globalStart");
        addOutport("globalTimeout");

        addCoupling(this, "globalStart", timer, "start");
        addCoupling(timer, "timeout", this, "globalTimeout");
        addCoupling(generator, "start", timer, "start");

        System.out.println("[PeripheralSimulation] Model inicializovaný s prepojeniami.");
    }
}
