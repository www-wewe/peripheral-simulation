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
	/** Actual state of the output pin */
	private boolean pwmPin;
	/** Absolute time of the next change */
	private double nextPwmEdgeTime;

	/*------------- UART --------------*/
	/** Actual state of the TX pin */
	private boolean uartTxPin;
	/** Bit which we are currently sending (0-9: start, 8 data, stop) */
	private int uartBitPos;
	/** Value which we are currently sending */
	private int uartTxShiftReg;
	/** Time when we will send the next bit */
	private double nextUartBitTime;

	/** Flag indicating that the RX pin is ready to receive data */
	private boolean uartRxReady;
	/** Value of the RX */
	private int uartRxByte;

	/** Seconds per clock cycle */
	private double secondsPerCycle;

	/**
	 * Constructor for FlexIOModel.
	 *
	 * @param config The configuration object containing the FlexIO settings.
	 */
	public FlexIOModel(FlexIOConfig config) {
		this.config = config;
		secondsPerCycle = 1.0 / config.getClockFrequency();
	}

	@Override
	public void initialize(SimulationEngine engine) {
		// 1) zistíme, ktorý timer/shifter beží v PWM, resp. UART móde
		initPwm(engine);
		initUart(engine);

		// v prípade, že modul nebol povolený, žiadne udalosti neplánujeme

	}

	@Override
	public void update(SimulationEngine engine) {
		double now = engine.getCurrentTime();

		/* ---------------- PWM ---------------- */
		if (nextPwmEdgeTime >= 0 && now + 1e-12 >= nextPwmEdgeTime) {
			pwmPin = !pwmPin; // toggle
			scheduleNextPwmEdge(engine, now);
		}

		/* ---------------- UART TX ------------- */
		if (nextUartBitTime >= 0 && now + 1e-12 >= nextUartBitTime) {
			uartAdvanceBit(engine, now);
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
		return new Object[] { pwmPin, uartTxPin, uartRxReady, uartRxReady ? uartRxByte : 0 };
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

	/* =================================================================== */
	/* 									PWM								   */
	/* =================================================================== */

	/**
	 * Initializes the PWM output settings.
	 *
	 * @param engine the simulation engine
	 */
	private void initPwm(SimulationEngine engine) {
		FlexIOTimer pwmTimer = null;
		for (FlexIOTimer timer : config.timers)
			if (timer.getTimod() == 0b10) { // PWM mode
				pwmTimer = timer;
				break;
			}
		if (pwmTimer == null) {
			nextPwmEdgeTime = -1;
			return;
		}

		int highCycles = (pwmTimer.getCmp() & 0xFF) + 1; // podľa RM
		int lowCycles = ((pwmTimer.getCmp() >> 8) & 0xFF) + 1;
		double highTime = highCycles * secondsPerCycle;
		double lowTime = lowCycles * secondsPerCycle;

		pwmPin = (pwmTimer.getTimOut() & 0b01) == 0; // init level (00/10 ->1, 01/11 ->0)
		nextPwmEdgeTime = engine.getCurrentTime() + (pwmPin ? highTime : lowTime);

		engine.scheduleEvent(nextPwmEdgeTime, () -> engine.getCurrentTime()); // “do nothing” placeholder
	}

	/**
	 * Schedules the next PWM edge event based on the current state of the PWM
	 * timer.
	 *
	 * @param engine the simulation engine
	 * @param now    the current simulation time
	 */
	private void scheduleNextPwmEdge(SimulationEngine engine, double now) {
		FlexIOTimer pwmTimer = null;
		for (FlexIOTimer timer : config.timers)
			if (timer.getTimod() == 0b10) {
				pwmTimer = timer;
				break;
			}
		if (pwmTimer == null) {
			nextPwmEdgeTime = -1;
			return;
		}

		int highCycles = (pwmTimer.getCmp() & 0xFF) + 1;
		int lowCycles = ((pwmTimer.getCmp() >> 8) & 0xFF) + 1;

		double delta = (pwmPin ? lowCycles : highCycles) * secondsPerCycle;
		nextPwmEdgeTime = now + delta;
		engine.scheduleEvent(nextPwmEdgeTime, () -> engine.getCurrentTime());
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
		// hľadáme dvojicu (timer baud, shifter TX) + (timer baud, shifter RX)
		FlexIOTimer baudTimer = null;
		FlexIOShifter txSh = null;
		FlexIOShifter rxSh = null;

		for (FlexIOTimer timer : config.timers)
			if (timer.getTimod() == 0b01) { // Dual 8-bit counters baud/bit mode.
				baudTimer = timer;
				break;
			}

		if (baudTimer != null) {
			for (FlexIOShifter shifter : config.shifters) {
				if (shifter.getTimSel() == baudTimer.getIndex()) {
					if (shifter.getSmod() == 0b010) // Transmit
						txSh = shifter;
					if (shifter.getSmod() == 0b001) // Receive
						rxSh = shifter;
				}
			}
		}

		// ak nemáme TX, simulujeme len PWM
		if (baudTimer == null || txSh == null) {
			nextUartBitTime = -1;
			return;
		}

		int baudDiv = ((baudTimer.getCmp() & 0xFF) + 1) * 2; // Dual 8-bit baud/bit mode
		double bitTime = baudDiv * secondsPerCycle;

		uartTxShiftReg = txSh.getBuffer() & 0xFF;
		uartBitPos = 0; // vysielame štart bit
		uartTxPin = true; // iddle = 1 (úroveň stop bitu)
		nextUartBitTime = engine.getCurrentTime() + bitTime;
		engine.scheduleEvent(nextUartBitTime, () -> engine.getCurrentTime());

		// Receive nie je plne implementovaný – status flag len simulujeme
		uartRxReady = false;
	}

	/**
	 * Move to the next bit in the UART transmission.
	 *
	 * @param engine the simulation engine
	 * @param now    the current simulation time
	 */
	private void uartAdvanceBit(SimulationEngine engine, double now) {
		FlexIOTimer baudTimer = null;
		for (FlexIOTimer timer : config.timers)
			if (timer.getTimod() == 0b01) {
				baudTimer = timer;
				break;
			}
		if (baudTimer == null) {
			nextUartBitTime = -1;
			return;
		}

		int baudDiv = ((baudTimer.getCmp() & 0xFF) + 1) * 2;
		double bitTm = baudDiv * secondsPerCycle;

		switch (uartBitPos) {
		case 0 -> uartTxPin = false; // start 0
		case 9 -> uartTxPin = true; // stop 1
		default -> { // data 0-7
			int dataBit = (uartTxShiftReg >> (uartBitPos - 1)) & 1;
			uartTxPin = (dataBit != 0);
		}
		}

		uartBitPos++;
		if (uartBitPos > 9) { // celý bajt vyslaný
			uartBitPos = 0;
			// v praxi by sme načítali ďalší bajt zo SHIFTBUF
			// (pre demo posielame stále tú istú hodnotu)
		}

		nextUartBitTime = now + bitTm;
		engine.scheduleEvent(nextUartBitTime, () -> engine.getCurrentTime());
	}

	/* =================================================================== */
	/* Konfiguračné (helper) metódy */
	/* =================================================================== */

	/** Povolenie/zakázanie PWM výstupu za behu. */
	public void enablePwm(boolean en) {
		if (!en)
			nextPwmEdgeTime = -1;
	}

	/** Nahraje bajt do UART TX SHIFTBUF. Volajte pred spustením simulácie. */
	public void loadUartTxByte(int b) {
		uartTxShiftReg = b & 0xFF;
	}

}
