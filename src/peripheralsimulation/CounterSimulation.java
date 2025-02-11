package peripheralsimulation;

import model.modeling.digraph;
import peripheralsimulation.engine.CounterInputGenerator;
import peripheralsimulation.model.CounterModel;

public class CounterSimulation extends digraph implements PeripheralSimulation {

    public CounterSimulation() {
        super("CounterSimulation");

        CounterInputGenerator generator = new CounterInputGenerator();
        CounterModel counter = new CounterModel();

        add(generator);
        add(counter);

        // Spojenie výstupu generátora so vstupom čítača
        addCoupling(generator, "increment", counter, "increment");

        // Spojenie výstupu čítača s globálnym výstupom simulácie
        addCoupling(counter, "output", this, "globalOutput");
    }
}
