/** Copyright (c) 2025, Veronika Lenková */
package peripheralsimulation.model.flexio;

/**
 * Class representing a FlexIO timer.
 * 
 * This class provides methods to configure and manage the timer's settings,
 * including its mode, pin selection, and compare value.
 * 
 * @author Veronika Lenková
 */
public class FlexIOTimer {

	/** Timer disabled mode */
	private static final int TIMOD_DISABLED = 0b00;

	/** Dual 8-bit counters baud/bit mode */
	private static final int TIMOD_BAUDBIT = 0b01;

	/** Dual 8-bit counters PWM mode */
	private static final int TIMOD_PWM = 0b10;

	/** Single 16-bit timer mode */
	private static final int TIMOD_16BIT = 0b11;

	/** Index of the timer */
	private int index;

	/** FlexIO configuration object */
	private FlexIOConfig config;

	/** Counter value (low / high) */
	private int counterLow, counterHigh;

	/** Actual output level */
	private boolean outLevel;

	/** Previous output level */
	private boolean prevOutLevel;

	/** Reload value (low / high) from CMP register */
	private int lowReload, highReload;

	/** Boolean flag indicating if the timer is running */
	private boolean running;

	/** Counts down when the timer stops */
	private int stopDelay;

	/** When TSTOP bit-0 is set, the timer stops on compare */
	private boolean stopOnCompare; // TSTOP bit-0

	/** When TSTOP bit-1 is set, the timer stops on disable */
	private boolean stopOnDisable;

	/** True when the timer is stopped and waiting for the next edge */
	private boolean stopPending;

	/*****************************************************************/
	/* TIMCTL */
	/*****************************************************************/

	/** Mode of the timer (disabled, Dual 8-bit counters baud/bit, PWM, etc.) */
	private int timerMode;

	/** Pin polarity, 0 - Pin is active high, 1 - active low */
	private int timerPinPolarity;

	/** Pin selection, 0 - Pin 0, 1 - Pin 1, etc. */
	private int timerPinSelect;

	/** Pin configuration (output disabled, open drain, etc.) */
	private int timerPinConfiguration;

	/** Trigger Source, 0 - external trigger, 1 - internal trigger */
	private int triggerSource;

	/** Trigger polarity, 0 - Trigger is active high, 1 - active low */
	private int triggerPolarity;
	/**
	 * Trigger selection (pin 2n input, shifter n status flag, pin 2n+1 input, timer
	 * n trigger output)
	 */
	private int triggerSelect;

	/*****************************************************************/
	/* TIMCFG */
	/*****************************************************************/

	/** Timer output, configures the initial state of the timer output */
	private int timerOutput;
	/**
	 * Timer decrement, configures the source of the timer decrement and source of
	 * the shift clock
	 */
	private int timerDecrement;

	/** Timer reset - condition that resets the timer */
	private int timerReset;

	/** Timer disable - condition that disables the timer */
	private int timerDisable;

	/** Timer enable - condition that enables the timer */
	private int timerEnable;

	/** Timer stop bit */
	private int timerStopBit;

	/** Timer start bit */
	private int timerStartBit;

	/** TIMCMP - Timer compare value */
	private int timerCompareValue;

	/**
	 * Constructor for FlexIOTimer.
	 *
	 * @param config The FlexIO configuration object.
	 * @param index  The index of the timer.
	 */
	public FlexIOTimer(FlexIOConfig config, int index) {
		this.index = index;
		this.config = config;
		setControlRegister(config.getTimCtl(index));
		setConfigRegister(config.getTimCfg(index));
		setTimerCompareValue(config.getTimCmp(index));
	}

	/**
	 * Sets the timer control register.
	 *
	 * @param timerControlRegister The control register value.
	 */
	public void setControlRegister(int timerControlRegister) {
		this.timerMode = timerControlRegister & 3;
		this.timerPinPolarity = (timerControlRegister >> 7) & 1;
		this.timerPinSelect = (timerControlRegister >> 8) & 7;
		this.timerPinConfiguration = (timerControlRegister >> 16) & 3;
		this.triggerSource = (timerControlRegister >> 22) & 1;
		this.triggerPolarity = (timerControlRegister >> 23) & 1;
		this.triggerSelect = (timerControlRegister >> 24) & 0xF;
	}

	/**
	 * Sets the timer configuration register.
	 *
	 * @param timerConfigRegister The configuration register value.
	 */
	public void setConfigRegister(int timerConfigRegister) {
		this.timerOutput = (timerConfigRegister >> 24) & 3;
		this.timerDecrement = (timerConfigRegister >> 20) & 3;
		this.timerReset = (timerConfigRegister >> 16) & 7;
		this.timerDisable = (timerConfigRegister >> 12) & 7;
		this.timerEnable = (timerConfigRegister >> 8) & 7;
		this.timerStopBit = (timerConfigRegister >> 4) & 3;
		this.timerStartBit = (timerConfigRegister >> 1) & 1;

		stopOnCompare = (timerStopBit & 0b01) != 0;
		stopOnDisable = (timerStopBit & 0b10) != 0;
	}

	/**
	 * Sets the timer compare value.
	 *
	 * @param cmp The compare value to set.
	 */
	public void setTimerCompareValue(int cmp) {
		this.timerCompareValue = cmp & 0xFFFF;
		switch (timerMode) {
		case TIMOD_PWM -> { // low / high perióda
			lowReload = (cmp & 0xFF) + 1;
			highReload = ((cmp >>> 8) & 0xFF) + 1;
		}
		case TIMOD_BAUDBIT -> { // baud-divider + bits*2-1
			lowReload = ((cmp & 0xFF) + 1) * 2;
			highReload = ((cmp >>> 8) & 0xFF) + 1; // N = CMP + 1
		}
		case TIMOD_16BIT -> { // celý 16-bit counter
			lowReload = (cmp + 1) * 2; // divider
			highReload = 1; // nepoužíva sa
		}
		default -> {
			lowReload = highReload = 1;
		}
		}
	}

	/**
	 * This method sets the initial values for the timer's counter and output level
	 * based on the configuration.
	 */
	public void reset() {
		counterLow = lowReload;
		counterHigh = highReload;
		outLevel = (timerOutput & 1) == 0; // RM: 00/10 -> logic 1
		prevOutLevel = outLevel;
		// Timer output also decides the initial state of the pin after reset.
		running = true;
		// Now it is always true, but should be running according timerEnable, timerDisable
		// Timer reset is not implemented yet
		// Timer decrement is always from the clock, the other source is not implemented yet
		stopPending = false;
	}

	/**
	 * This method simulates the timer's behavior, decrementing the counter and
	 * toggling the output level based on the timer mode.
	 *
	 * @return true if the output level changed, false otherwise.
	 */
	public Edge tick() {

		// Stop bit
		if (stopDelay > 0) {
			if (--stopDelay == 0)
				running = true; // znova sa spustí
			return Edge.NONE; // žiadna hrana
		}

		if (!running) {
			if (stopOnDisable && stopDelay == 0) {
				/* stop-bit on disable */
				stopDelay = lowReload * 2; // 1 bit-time
			}
			return Edge.NONE;
		}

		boolean edge = false;
		switch (timerMode) {
		/*---------------- DUAL-8-bit PWM ----------------------------*/
		case TIMOD_PWM -> {
			if (outLevel) {
				if (--counterLow == 0) {
					counterLow = lowReload;
					outLevel = false;
					edge = true;
				}
			} else {
				if (--counterHigh == 0) {
					counterHigh = highReload;
					outLevel = true;
					edge = true;
				}
			}
			// For the modes with PIN/Trigger have to be set edge=true when external edge occurs
		}

		/*---------------- DUAL-8-bit BAUD/BIT -----------------------*/
		case TIMOD_BAUDBIT -> {
			if (--counterLow == 0) {
				counterLow = lowReload;
				outLevel = !outLevel; // ⇨ hrana
				edge = true;

				/* ––––– ak sme už na stop-bite, tu timer na chvíľu zastavíme ––––– */
				if (stopPending) {
					stopDelay = lowReload * 2; // 1 bit-time
					running = false;
					stopPending = false;
				}

				/* ––– odpočítavanie počtu bitov v slove ––– */
				if (--counterHigh == 0) {
					counterHigh = highReload;

					if (stopOnCompare) {
						stopPending = true; // pauzu vložíme na ďalšej hrane
					}
					config.setTimStat(1 << index);
				}
			}
		}

		default -> {
			/* ostatné TIMOD neimplementované */ }
		}

		edge = (outLevel != prevOutLevel);
		Edge result = Edge.NONE;
		if (edge) {
			result = outLevel ? Edge.POSEDGE : Edge.NEGEDGE;
		}
		prevOutLevel = outLevel;
		return result;
	}

	/**
	 * Returns the timer output level.
	 *
	 * @return The current output level of the timer.
	 */
	public boolean isClockLevelHigh() {
		boolean level = outLevel;
		if (timerPinPolarity == 1) {
			level = !level;
		}
		return level;
	}

	/**
	 * Returns the number of high reload ticks. For baud/bit mode, this is the
	 * number of bits * 2.
	 *
	 * @return The number of high reload ticks.
	 */
	public int getHighReload() {
		return highReload;
	}

	/**
	 * Returns the current counter value of the timer.
	 *
	 * @return The current counter value.
	 */
	public int getCurrentCounter() {
		return (timerMode == TIMOD_BAUDBIT) ? counterLow : (outLevel ? counterLow : counterHigh);
	}

	/**
	 * Returns timer status flag.
	 *
	 * @return true if status flag is set, false otherwise.
	 */
	public boolean isStatusFlagSet() {
		return (config.getTimStat() & (1 << index)) != 0;
	}

}
