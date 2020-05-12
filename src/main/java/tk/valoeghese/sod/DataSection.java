package tk.valoeghese.sod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a section of SOD data.
 * @author Valoeghese
 */
public class DataSection implements BaseDataSection<Object> {
	public DataSection() {
		this.data = new ArrayList<>();
	}

	private final List<Object> data; 

	/**
	 * @deprecated Should only be used by the parser! Please use the type specific methods instead for writing data.
	 */
	@Override
	public <T> void writeForParser(T data) {
		this.data.add(data);
	}

	public void writeByte(byte data) {
		this.data.add(data);
	}

	public void writeShort(short data) {
		this.data.add(data);
	}

	public void writeInt(int data) {
		this.data.add(data);
	}

	public void writeLong(long data) {
		this.data.add(data);
	}

	public void writeFloat(float data) {
		this.data.add(data);
	}

	public void writeDouble(double data) {
		this.data.add(data);
	}

	public void writeString(String data) {
		this.data.add(data);
	}

	public void writeBoolean(boolean data) {
		this.data.add(data ? (byte) 1 : (byte) 0);
	}
	
	public <T extends Enum<?>> void writeEnum(T enumValue) {
		this.data.add(enumValue.ordinal());
	}

	public <T extends Enum<?>> void writeEnumAsString(T enumValue) {
		this.data.add(enumValue.toString());
	}

	public int size() {
		return this.data.size();
	}

	public byte readByte(int index) {
		return (byte) this.data.get(index);
	}

	public short readShort(int index) {
		return (short) this.data.get(index);
	}

	public int readInt(int index) {
		return (int) this.data.get(index);
	}

	public long readLong(int index) {
		return (long) this.data.get(index);
	}

	public float readFloat(int index) {
		return (float) this.data.get(index);
	}

	public double readDouble(int index) {
		return (double) this.data.get(index);
	}

	public String readString(int index) {
		return (String) this.data.get(index);
	}

	public boolean readBoolean(int index) {
		return ((byte) this.data.get(index)) != 0;
	}

	public <T extends Enum<T>> T readEnumString(int index, Class<T> type) {
		return Enum.valueOf(type, (String) this.data.get(index));
	}

	public <T extends Enum<?>> T readEnum(int index, T[] values) {
		Integer i = (Integer) this.data.get(index);

		if (i == null) {
			return null;
		}

		return values[i];
	}

	@Override
	public Iterator<Object> iterator() {
		return this.data.iterator();
	}
}
