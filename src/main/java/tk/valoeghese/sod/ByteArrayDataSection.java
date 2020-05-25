package tk.valoeghese.sod;

import java.util.Iterator;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;

public class ByteArrayDataSection implements BaseDataSection<Byte> {
	public ByteArrayDataSection() {
		this.array = new ByteArrayList();
	}

	private final ByteList array;

	public void writeByte(byte value) {
		this.array.add(value);
	}

	public int size() {
		return array.size();
	}

	/**
	 * @deprecated Should only be used by the parser! Please use the type specific methods instead for writing data.
	 */
	@Override
	public <T> void writeForParser(T data) throws UnsupportedOperationException {
		if (data instanceof Byte) {
			this.writeByte((byte) data);
		} else {
			throw new UnsupportedOperationException("Invalid data type parameter for this data section");
		}
	}

	public byte readByte(int index) {
		return this.array.getByte(index);
	}

	@Override
	public Iterator<Byte> iterator() {
		return this.array.iterator();
	}
}
