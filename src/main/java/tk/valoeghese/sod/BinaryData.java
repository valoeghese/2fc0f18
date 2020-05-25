package tk.valoeghese.sod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
		try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
			Parser.write(this, dos);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void writeGzipped(File file) throws IOException {
		try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file)))) {
			Parser.write(this, dos);
		}
	}

	@Override
	public Iterator<Map.Entry<String, BaseDataSection>> iterator() {
		return this.sections.entrySet().iterator();
	}

	public static BinaryData read(File file) throws SODParseException {
		try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
			long magic = dis.readLong();

			if (magic != 0xA77D1E) {
				throw new SODParseException("Not a valid SOD file!");
			}

			return Parser.parse(dis);
		} catch (IOException e) {
			throw new SODParseException("Unhandled IOException in parsing file " + file.toString(), e);
		}
	}

	public static BinaryData readGzipped(File file) throws SODParseException {
		try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(file))) {
			// thanks stack overflow for having more efficient code than I probably would have written
			ByteArrayOutputStream uncompressed = new ByteArrayOutputStream();
			byte[] buf = new byte[0x1000];
			int bytesRead;

			// while file has not ended, copy up to 0x4000 bytes of data into a byte[] buffer
			// and store the number of bytes read
			while ((bytesRead = gis.read(buf, 0, buf.length)) != -1) {
				// add to uncompressed output stream, omitting any bytes that weren't part of the read data
				uncompressed.write(buf, 0, bytesRead);
			}

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(uncompressed.toByteArray()));

			long magic = dis.readLong();

			if (magic != 0xA77D1E) {
				throw new SODParseException("Not a valid GZIPPED SOD file!");
			}

			return Parser.parse(dis);
		} catch (IOException e) {
			throw new SODParseException("Unhandled IOException in parsing file " + file.toString(), e);
		}
	}
}
