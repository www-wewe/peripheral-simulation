# Simulation of Microcontroller Peripherals

This project is an **Eclipse Plug-in** that simulates various **microcontroller peripherals** (e.g. SysTick timer, FlexIO, simple counter). It provides a UI view within Eclipse that displays or plots the peripheral’s state changes in real time.

## Key Features

1. **Peripheral Simulation Engine**  
   - A discrete-event simulation core (`SimulationEngine`) that handles scheduling of peripheral updates and user-defined events.  
   - Manages multiple peripherals and steps through events in chronological order.

2. **Peripheral Models**  
   - **SysTickTimerModel**: Simulates the Cortex-M SysTick timer registers (CSR, RVR, CVR, CALIB).  
   - **CounterModel**: A basic counter with overflow behavior.  
   - **FlexIOModel** (placeholder example)  
   - Each model implements the `PeripheralModel` interface for a consistent approach.

3. **Register-Based Access**  
   - Models can expose registers by address (e.g., `0xE000E010` for `SYST_CSR`).  
   - Users or higher-level code can read/write registers, toggle bits, or handle partial writes.  

4. **User-Configurable Monitoring**  
   - The user can set the simulation time range, and how often the engine reports outputs.  
   - The user can also choose which outputs (e.g. “current value”, “interupt”) to display in either a table or a chart.

5. **UI Integration**  
   - A new **“Simulation View”** in Eclipse (`SimulationView`) that lets you:  
     - Pick the peripheral to simulate from a combo box.  
     - Start/stop/clear the simulation.  
     - Switch between a table or chart display (the table lists time + outputs in rows; the chart graphs them over time).  

6. **Periodic or One-Time Events**  
   - You can schedule user-defined events (e.g. toggling bits, writing registers) at specific times or intervals.  
   - For instance, “Every 10 ms, toggle bit #1 in the SysTick CSR register.”

## Project Structure

1. **`peripheralsimulation.engine`**  
   - Core simulation classes such as `SimulationEngine`, `SimulationEvent`, and user event definitions.  

2. **`peripheralsimulation.model`**  
   - Interface `PeripheralModel` plus specific peripheral implementations (SysTick, SCTimer, etc.).  
   - Each model handles time-based updates (`update(...)`) and initialization (`initialize(...)`).  
   - Models can define internal registers, bit logic, and how to apply user events.

3. **`peripheralsimulation.views`**  
   - Contains `SimulationView`, an Eclipse ViewPart that shows the simulation status, start/stop buttons, etc.  
   - Integrates with Eclipse’s extension points in `plugin.xml` for the UI.  

4. **`peripheralsimulation.ui`**  
   - Classes for rendering the outputs: `SimulationTable` (SWT table) or `SimulationChart` (SWTChart-based).  

5. **`peripheralsimulation.io`**  
   - Helper classes like `Register` or `UserPreferences`, enabling user- or file-based configurations, register definitions, etc.

6. **`plugin.xml`** & **`MANIFEST.MF`**  
   - Eclipse plugin definitions.  
   - The view is contributed to the Java Perspective, next to the “Problems” view by default.  
   - Bundles required: `org.eclipse.ui`, `org.eclipse.core.runtime`, `org.eclipse.swtchart`.

## How to Use

1. **Import Plugin into Eclipse**  
   - Place this project in your Eclipse workspace as a Plug-in Project.  
   - Ensure dependencies (`org.eclipse.swtchart` etc.) are available.
   - Download SWTChart here -> https://download.eclipse.org/swtchart/releases/0.14.0/repository/

2. **Launch an Eclipse Application**  
   - In Run Configurations, choose “Eclipse Application”.  
   - Run the new Eclipse instance.  
   - Open the **“Simulation View”** (Window → Show View → Other → Peripheral Simulation → Simulation View).

3. **Select a Peripheral**  
   - In the combo box, pick e.g. “System Tick Timer”.
   - TODO: import registers

4. **Settings**
   - Configure simulation in the “Settings...” dialog.
   - The “Settings...” button can let you choose which outputs to display, or set a different monitoring period, or choose “Table” vs. “Graph” mode.
   
5. **Run simulation** 
   - Press “Run simulation”.  
   - The simulation runs. Outputs appear either in a table or chart, depending on user preferences.

6. **Stop or Clear**  
   - Use “Clear simulation” to stop.  
   - “Clear simulation” resets the view.  

## Customizing or Extending

1. **Adding a New Peripheral**  
   - Implement `PeripheralModel`, define registers in `getRegisterByAddress(...)`.  
   - Set scheduling logic in `initialize(...)` and `update(...)`.

2. **Adding New User Events**  
   - The engine can handle scheduled `SimulationEvent`s or user-defined triggers (toggle bits, etc.).  
   - You can define these events in a config or UI using "User Events..." button in a simulation view.

3. **Bit/Field Manipulations**  
   - Each peripheral can implement partial writes (like “setBit(...)” or “toggleBit(...)”).  
   - The engine’s default method `applyUserEvent(...)` can call these.

## Development Notes

- **JavaSE-23** is set in `MANIFEST.MF` (`Bundle-RequiredExecutionEnvironment: JavaSE-23`).  
- **SWTChart** is required for the graphing functionality.  
- The **`UserPreferences`** class typically stores user’s chosen frequency, time range, selected outputs, etc.

## Contributing

- Fork or clone this project.  
- Create new branches for features or bug fixes.  
- Submit pull requests with meaningful commits and updates to docs if relevant.

## License

BSD 3-Clause License

---

Enjoy simulating microcontroller peripherals within Eclipse! For questions or issues, open an Issue or contact the maintainer.
