package tk.valoeghese.sod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringArrayDataSection implements BaseDataSection<String> {
	public StringArrayDataSection() {
		this.array = new ArrayList<String>();
	}

	private final List<String> array;

	public void writeString(String value) {
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
		if (data instanceof String) {
			this.writeString((String) data);
		} else {
			throw new UnsupportedOperationException("Invalid data type parameter for this data section");
		}
	}

	public String readString(int index) {
		return this.array.get(index);
	}

	@Override
	public Iterator<String> iterator() {
		return this.array.iterator();
	}
}
