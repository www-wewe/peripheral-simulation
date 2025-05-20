/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import peripheralsimulation.engine.test.SimulationEngineTest;
import peripheralsimulation.engine.test.SimulationEventTest;
import peripheralsimulation.engine.test.UserEventGeneratorTest;
import peripheralsimulation.model.test.FlexIOModelTest;
import peripheralsimulation.model.test.SysTickTimerModelTest;

/**
 * Aggregates all unit tests for the Peripheral-Simulation project so they can
 * be executed with a single JUnit 4 run configuration (IDE, Maven Surefire,
 * Gradle, etc.).
 *
 * @author Veronika Lenková
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	    SimulationEngineTest.class,
	    SimulationEventTest.class,
        UserEventGeneratorTest.class,
        SysTickTimerModelTest.class,
        FlexIOModelTest.class,
})
public class AllTests {
	/*
	 * The class remains empty. It is used only as a holder of the above
	 * annotations.
	 */
}
