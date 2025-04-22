package peripheralsimulation.model.flexio;

public enum FlexIOOutputs {

	PIN0,
	SHIFTBUF0,
	TIMER0_TOGGLE;

	public static String[] getOutputNames() {
		return new String[] { PIN0.toString(), SHIFTBUF0.toString(), TIMER0_TOGGLE.toString() };
	}
}
