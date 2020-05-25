package tk.valoeghese.sod;

import java.util.Iterator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class IntArrayDataSection implements BaseDataSection<Integer> {
	public IntArrayDataSection() {
		this.array = new IntArrayList();
	}

	private final IntList array;

	public void writeInt(int value) {
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
		if (data instanceof Integer) {
			this.writeInt((int) data);
		} else {
			throw new UnsupportedOperationException("Invalid data type parameter for this data section");
		}
	}

	public int readInt(int index) {
		return this.array.getInt(index);
	}

	@Override
	public Iterator<Integer> iterator() {
		return this.array.iterator();
	}
}
