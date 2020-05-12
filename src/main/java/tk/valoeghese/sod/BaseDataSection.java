package tk.valoeghese.sod;

public interface BaseDataSection<E> extends Iterable<E> {
	/**
	 * @deprecated Should only be used by the parser! Please use the type specific methods instead for writing data.
	 */
	<T> void writeForParser(T data);
}
