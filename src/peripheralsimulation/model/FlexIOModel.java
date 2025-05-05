/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model;

import peripheralsimulation.engine.SimulationEngine;
import peripheralsimulation.model.flexio.FlexIOConfig;
import peripheralsimulation.model.flexio.FlexIOOutputs;
import peripheralsimulation.model.flexio.FlexIOShifter;
import peripheralsimulation.model.flexio.FlexIOTimer;

/**
 * FlexIO peripheral model, which can be simulated within the SimulationEngine.
 * Class implements the PeripheralModel interface and provides methods to
 * initialize, update, and manage the FlexIO peripheral's configuration
 *
 * <p>
 * Podporované módy:
 * </p>
 * <ul>
 * <li><b>PWM generator</b> – Timer TIMOD = 10 (dual 8-bit PWM)</li>
 * <li><b>UART TX/RX</b> – Timer TIMOD = 01 (dual 8-bit baud/bit) + Shifter SMOD
 * = Transmit/Receive</li>
 * </ul>
 *
 * <p>
 * Názvy výstupov (poradie v poliach <code>outputs</code>):
 * </p>
 * <ol>
 * <li>PWM_PIN_STATE (boolean)</li>
 * <li>UART_TX_PIN (boolean)</li>
 * <li>UART_RX_READY (boolean)</li>
 * <li>UART_RX_BYTE (int 0-255)</li>
 * </ol>
 *
 * @author Veronika Lenková
 */
public class FlexIOModel implements PeripheralModel {

	/* Output indices */
	private static final int IDX_PWM_PIN = 0;
	private static final int IDX_UART_TX_PIN = 1;
	private static final int IDX_UART_RX_RDY = 2;
	private static final int IDX_UART_RX_BYTE = 3;

	/** Output names */
	private static final String[] OUTPUT_NAMES = { "PWM_PIN", "UART_TX_PIN", "UART_RX_READY", "UART_RX_BYTE" };
	// private static final String[] OUTPUT_NAMES = FlexIOOutputs.getOutputNames();

	/** The configuration object with all the "register" bits */
	private final FlexIOConfig config;

	/*------------- PWM ---------------*/
	/** Timer which generates PWM signal */
	private FlexIOTimer pwmTimer;
	/** Actual state of the output pin */
	private boolean pwmPinState;
	/** Absolute time of the next change */
	private double nextPwmEdgeTime = -1;

	/*------------- UART --------------*/
	/** Timer which generates UART signal */
	private FlexIOTimer  uartTimer;
	/** TX a RX shifter */
    private FlexIOShifter txShifter, rxShifter;
	/** Actual state of the TX pin */
	private boolean uartTxPinLevel;
	/** Bit which we are currently sending (0-9: start, 8 data, stop) */
	private int txBitPos;
	/** Value which we are currently sending */
	private int uartTxShiftReg;
	/** Time when we will send the next bit */
	private double nextUartBitTime;

	/** Flag indicating that the RX pin is ready to receive data */
	private boolean uartRxReady;
	/** Value of the RX */
	private int uartRxByte;
	/** Value of the RX pin */
	private int rxShiftReg;
	/** Bit position of the RX pin */
	private int rxBitPos;

	private double bitPeriod;
	/** Helper variable for RX sampling */
	private double nextRxEdge = -1;

	/** Seconds per clock cycle */
	private double clkPeriod;

	/**
	 * Constructor for FlexIOModel.
	 *
	 * @param config The configuration object containing the FlexIO settings.
	 */
	public FlexIOModel(FlexIOConfig config) {
		this.config = config;
		clkPeriod = 1.0 / config.getClockFrequency();
	}

	@Override
	public void initialize(SimulationEngine engine) {
		if (!config.isEnabled()) return;

        detectPeripherals();
        initPwm(engine);
        initUart(engine);
	}

	@Override
	public void update(SimulationEngine engine) {
		double now = engine.getCurrentTime();

		/* --- PWM --------------------------------------------------------- */
		if (pwmTimer != null && now + 1e-15 >= nextPwmEdgeTime) {
			pwmPinState = !pwmPinState;
			scheduleNextPwmEdge(engine, now);
		}

		/* --- UART TX ----------------------------------------------------- */
		if (uartTimer != null && txShifter != null && now + 1e-15 >= nextUartBitTime) {
			transmitNextBit(engine, now);
		}

		/* --- UART RX (sampluje na stred bitu) ---------------------------- */
		if (uartTimer != null && rxShifter != null) {
			sampleRx(now);
		}
	}

	@Override
	public String getOutputName(int index) {
		return OUTPUT_NAMES[index];
	}

	@Override
	public String[] getOutputNames() {
		return OUTPUT_NAMES;
	}

	@Override
	public Object[] getOutputs() {
		return new Object[] { pwmPinState, uartTxPinLevel, uartRxReady, uartRxReady ? uartRxByte : 0 };
	}

	@Override
	public int getOutputIndex(String name) {
		for (int i = 0; i < OUTPUT_NAMES.length; ++i)
			if (OUTPUT_NAMES[i].equals(name))
				return i;
		throw new IllegalArgumentException("Unknown output " + name);
	}

	@Override
	public void setRegisterValue(int addr, int value) {
		config.writeByAddress(addr, value);
	}

	@Override
	public Integer getRegisterValue(int addr) {
		return config.readByAddress(addr);
	}

	/** nájde prvý PWM-timer a UART (timer+shifter(y)) podľa konfigurácie */
	private void detectPeripherals() {
		/* --- PWM timer (TIMOD == 10) ----------------------------------- */
		for (FlexIOTimer timer : config.getTimers())
			if (timer.getTimod() == 0b10) {
				pwmTimer = timer;
				break;
			}

		/* --- UART (TIMOD == 01 + shifter) ------------------------------ */
		for (FlexIOTimer timer : config.getTimers())
			if (timer.getTimod() == 0b01) {
				uartTimer = timer;
				break;
			}

		if (uartTimer != null) {
			for (FlexIOShifter shifter : config.getShifters())
				if (shifter.getTimSel() == uartTimer.getIndex()) {
					if (shifter.getSmod() == 0b010 && txShifter == null)
						txShifter = shifter; // Transmit
					if (shifter.getSmod() == 0b001 && rxShifter == null)
						rxShifter = shifter; // Receive
				}
		}
	}

	/* =================================================================== */
	/* 									PWM								   */
	/* =================================================================== */

	/**
	 * Initializes the PWM output settings.
	 *
	 * @param engine the simulation engine
	 */
	private void initPwm(SimulationEngine engine) {
		if (pwmTimer == null)
			return;

		int high = (pwmTimer.getCmp() & 0xFF) + 1;
		int low = ((pwmTimer.getCmp() >> 8) & 0xFF) + 1;

		pwmPinState = (pwmTimer.getTimOut() & 1) == 0; // podľa RM
		nextPwmEdgeTime = engine.getCurrentTime() + (pwmPinState ? high : low) * clkPeriod;

		engine.scheduleEvent(nextPwmEdgeTime, () -> update(engine));
	}

	/**
	 * Schedules the next PWM edge event based on the current state of the PWM
	 * timer.
	 *
	 * @param engine the simulation engine
	 * @param now    the current simulation time
	 */
	private void scheduleNextPwmEdge(SimulationEngine engine, double now) {
		int high = (pwmTimer.getCmp() & 0xFF) + 1;
		int low = ((pwmTimer.getCmp() >> 8) & 0xFF) + 1;

		nextPwmEdgeTime = now + (pwmPinState ? high : low) * clkPeriod;
		engine.scheduleEvent(nextPwmEdgeTime, () -> update(engine));
	}

	/* =================================================================== */
	/*									 UART							   */
	/* =================================================================== */

	/**
	 * Initializes the UART transmission settings.
	 *
	 * @param engine the simulation engine
	 */
	private void initUart(SimulationEngine engine) {
		if (uartTimer == null || txShifter == null)
			return;

		int baudDiv = (uartTimer.getCmp() & 0xFF) + 1; // podľa RM
		bitPeriod = baudDiv * 2 * clkPeriod; // (CMP+1)*2/clk
		uartTxShiftReg = txShifter.getBuffer() & 0xFF; // buffer plnim ručne s loadUartTxByte
		txBitPos = 0; // start-bit
		uartTxPinLevel = true; // idle = 1
		nextUartBitTime = engine.getCurrentTime() + bitPeriod;
		engine.scheduleEvent(nextUartBitTime, () -> update(engine));

		/* RX – inicializácia */
		if (rxShifter != null) {
			uartRxReady = false;
			rxBitPos = 0;
		}
	}

	/**
	 * Transfer the next bit in the UART transmission.
	 *
	 * @param engine the simulation engine
	 * @param now    the current simulation time
	 */
	private void transmitNextBit(SimulationEngine engine, double now) {
		switch (txBitPos) {
		case 0 -> uartTxPinLevel = false; // start 0
		case 9 -> uartTxPinLevel = true; // stop 1
		default -> uartTxPinLevel = ((uartTxShiftReg >> (txBitPos - 1)) & 1) != 0;
		}
		txBitPos++;
		if (txBitPos > 9) { // celý rámec
			uartTxShiftReg = txShifter.getBuffer() & 0xFF; // načítaj nový
			txBitPos = 0;
		}
		nextUartBitTime = now + bitPeriod;
		engine.scheduleEvent(nextUartBitTime, () -> update(engine));
	}

	private void sampleRx(double now) {
		// jednoduchá simulácia len na demonštráciu: prijíma TX z toho istého modulu
		if (uartTxPinLevel == false && rxBitPos == 0) { // zachytený štart
			rxBitPos = 1;
			rxShiftReg = 0;
			nextRxEdge = now + bitPeriod * 1.5; // stred prvého bitu
		}
		if (rxBitPos > 0 && now + 1e-15 >= nextRxEdge) {
			if (rxBitPos >= 1 && rxBitPos <= 8) {
				rxShiftReg |= (uartTxPinLevel ? 1 : 0) << (rxBitPos - 1);
			}
			rxBitPos++;
			nextRxEdge += bitPeriod;
			if (rxBitPos == 10) { // stop-bit odmeraný
				uartRxByte = rxShiftReg & 0xFF;
				uartRxReady = true;
				rxBitPos = 0;
			}
		}
	}

	/* =================================================================== */
	/* Konfiguračné (helper) metódy */
	/* =================================================================== */

	/** Load byte into UART TX shifter register FLEXIO_SHIFTBUF */
	public void loadUartTxByte(int b) {
		uartTxShiftReg = b & 0xFF;
	}

}
