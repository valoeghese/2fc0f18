package tk.valoeghese.sod;

import java.util.Iterator;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class ShortArrayDataSection implements BaseDataSection<Short> {
	public ShortArrayDataSection() {
		this.array = new ShortArrayList();
	}

	private final ShortList array;

	public void writeShort(short value) {
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
		if (data instanceof Short) {
			this.writeShort((short) data);
		} else {
			throw new UnsupportedOperationException("Invalid data type parameter for this data section");
		}
	}

	public short readShort(int index) {
		return this.array.getShort(index);
	}

	@Override
	public Iterator<Short> iterator() {
		return this.array.iterator();
	}
}
