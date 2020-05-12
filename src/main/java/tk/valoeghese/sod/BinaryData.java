package tk.valoeghese.sod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import tk.valoeghese.sod.exception.SODParseException;

@SuppressWarnings("rawtypes")
public class BinaryData implements Iterable<Map.Entry<String, BaseDataSection>> {
	public BinaryData() {
		this.sections = new HashMap<>();
	}

	private final Map<String, BaseDataSection> sections;

	public DataSection get(String name) {
		return (DataSection) this.sections.get(name);
	}

	public ByteArrayDataSection getByteArray(String name) {
		return (ByteArrayDataSection) this.sections.get(name);
	}

	public ShortArrayDataSection getShortArray(String name) {
		return (ShortArrayDataSection) this.sections.get(name);
	}

	public IntArrayDataSection getIntArray(String name) {
		return (IntArrayDataSection) this.sections.get(name);
	}

	public LongArrayDataSection getLongArray(String name) {
		return (LongArrayDataSection) this.sections.get(name);
	}

	public FloatArrayDataSection getFloatArray(String name) {
		return (FloatArrayDataSection) this.sections.get(name);
	}

	public DoubleArrayDataSection getDoubleArray(String name) {
		return (DoubleArrayDataSection) this.sections.get(name);
	}

	public StringArrayDataSection getStringArray(String name) {
		return (StringArrayDataSection) this.sections.get(name);
	}

	public DataSection getOrCreate(String name) {
		return (DataSection) this.sections.computeIfAbsent(name, k -> new DataSection());
	}

	public void put(String name, BaseDataSection section) {
		this.sections.put(name, section);
	}

	public boolean containsSection(String name) {
		return this.sections.containsKey(name);
	}

	public boolean write(File file) {
		try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
			Parser.write(this, dos);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Iterator<Map.Entry<String, BaseDataSection>> iterator() {
		return this.sections.entrySet().iterator();
	}

	public static BinaryData read(File file, boolean createIfNoError) throws SODParseException {
		try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
			long magic = dis.readLong();

			if (magic != 0xA77D1E) {
				throw new SODParseException("Not a valid SOD file!");
			}

			return Parser.parse(dis);
		} catch (IOException e) {
			if (!createIfNoError) {
				throw new SODParseException("Error in parsing file " + file.toString());
			}

			e.printStackTrace();
			return new BinaryData();
		}
	}
}
