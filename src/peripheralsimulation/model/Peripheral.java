package peripheralsimulation.model;

public enum Peripheral {

	SCTIMER {
		@Override
		public String toString() {
			return "SCTimer";
		}
	},
	COUNTER {
		@Override
		public String toString() {
			return "Counter";
		}
    }

}
