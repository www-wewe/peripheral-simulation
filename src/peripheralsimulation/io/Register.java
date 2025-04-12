package peripheralsimulation.io;

public class Register {

	private final String name;
	private final int size;
	private final boolean readOnly;
	private int value;
	private int address;

	public Register(int address, String name, int size, boolean readOnly, int value) {
		this.address = address;
		this.name = name;
		this.size = size;
		this.readOnly = readOnly;
		this.value = value;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		if (readOnly) {
			throw new IllegalStateException("Register is read-only");
		}
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public String toString() {
		return name + " = " + value;
	}

}
