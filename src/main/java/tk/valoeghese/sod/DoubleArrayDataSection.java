package tk.valoeghese.sod;

import java.util.Iterator;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class DoubleArrayDataSection implements BaseDataSection<Double> {
	public DoubleArrayDataSection() {
		this.array = new DoubleArrayList();
	}

	private final DoubleList array;

	public void writeDouble(double value) {
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
		if (data instanceof Double) {
			this.writeDouble((double) data);
		} else {
			throw new UnsupportedOperationException("Invalid data type parameter for this data section");
		}
	}

	public double readDouble(int index) {
		return this.array.getDouble(index);
	}

	@Override
	public Iterator<Double> iterator() {
		return this.array.iterator();
	}
}
