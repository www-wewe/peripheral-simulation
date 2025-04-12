package peripheralsimulation.engine;

public enum UserEventType {
	TOGGLE_BIT, // toggles a bit
	SET_BIT, // sets a bit to 1
	CLEAR_BIT, // sets a bit to 0
	WRITE_VALUE // write entire register or field with 'value'
}
