package tk.valoeghese.sod;

import java.util.Iterator;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class LongArrayDataSection implements BaseDataSection<Long> {
	public LongArrayDataSection() {
		this.array = new LongArrayList();
	}

	private final LongList array;

	public void writeLong(long value) {
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
		if (data instanceof Long) {
			this.writeLong((long) data);
		} else {
			throw new UnsupportedOperationException("Invalid data type parameter for this data section");
		}
	}

	public long readLong(int index) {
		return this.array.getLong(index);
	}

	@Override
	public Iterator<Long> iterator() {
		return this.array.iterator();
	}
}
