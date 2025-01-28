package peripheralsimulation;

import model.modeling.*;
import model.simulation.*;
import peripheralsimulation.engine.SCTimerModel;

public class PeripheralSimulation extends digraph {
    public PeripheralSimulation() {
        super("PeripheralSimulation");

        // Vytvorte atomic model pre SCTimer
        SCTimerModel timer = new SCTimerModel();

        // Pridajte model do simulácie
        add(timer);

        // Definujte porty na coupled modeli
        addInport("globalStart");
        addOutport("globalTimeout");

        // Prepojte globalStart s input portom timeru
        addCoupling(this, "globalStart", timer, "start");

        // Prepojte output port timeru s globalTimeout
        addCoupling(timer, "timeout", this, "globalTimeout");
    }
    @Override
    public MessageInterface<Object> Out() {
    	// TODO Auto-generated method stub
    	return super.Out();
    }
    
    public String getSimulationOutput() {
        // Vráťte výstup simulácie (napr. stav periférií)
        return "Príkladová správa"; // Nahradiť skutočnými dátami
    }
}

