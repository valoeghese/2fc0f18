package tk.valoeghese.sod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import tk.valoeghese.sod.exception.SODParseException;

final class Parser {
	@SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
	static BinaryData parse(DataInputStream input) throws IOException, SODParseException {
		BinaryData data = new BinaryData();

		DataType dataType;
		DataType sectionType;
		dataType = DataType.of(input.readByte());
		sectionType = dataType;

		BaseDataSection currentSection;
		int arraySizeCountdown = 0;

		try {
			currentSection = dataType.createSection();
			if (dataType != DataType.SECTION) {
				arraySizeCountdown = input.readInt();
			}
		} catch (SODParseException e) {
			throw new SODParseException("Data must be segregated into sections!");
		}

		data.put(input.readUTF(), currentSection);

		while (input.available() > 0) {
			switch (sectionType) {
			case BYTE_ARRAY_SECTION:
				while (arraySizeCountdown --> 0) {
					currentSection.writeForParser(input.readByte());
				}
				// create new section if available
				if (input.available() > 0) {
					try {
						dataType = DataType.of(input.readByte());
						sectionType = dataType;
						currentSection = dataType.createSection();
						data.put(input.readUTF(), currentSection);
						// set countdown
						if (dataType != DataType.SECTION) {
							arraySizeCountdown = input.readInt();
						}
					} catch (SODParseException e) {
						throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
					}
				}
				break;
			case SHORT_ARRAY_SECTION:
				while (arraySizeCountdown --> 0) {
					currentSection.writeForParser(input.readShort());
				}
				// create new section if available
				if (input.available() > 0) {
					try {
						dataType = DataType.of(input.readByte());
						sectionType = dataType;
						currentSection = dataType.createSection();
						data.put(input.readUTF(), currentSection);
						// set countdown
						if (dataType != DataType.SECTION) {
							arraySizeCountdown = input.readInt();
						}
					} catch (SODParseException e) {
						throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
					}
				}
				break;
			case INT_ARRAY_SECTION:
				while (arraySizeCountdown --> 0) {
					currentSection.writeForParser(input.readInt());
				}
				// create new section if available
				if (input.available() > 0) {
					try {
						dataType = DataType.of(input.readByte());
						sectionType = dataType;
						currentSection = dataType.createSection();
						data.put(input.readUTF(), currentSection);
						// set countdown
						if (dataType != DataType.SECTION) {
							arraySizeCountdown = input.readInt();
						}
					} catch (SODParseException e) {
						throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
					}
				}
				break;
			case LONG_ARRAY_SECTION:
				while (arraySizeCountdown --> 0) {
					currentSection.writeForParser(input.readLong());
				}
				// create new section if available
				if (input.available() > 0) {
					try {
						dataType = DataType.of(input.readByte());
						sectionType = dataType;
						currentSection = dataType.createSection();
						data.put(input.readUTF(), currentSection);
						// set countdown
						if (dataType != DataType.SECTION) {
							arraySizeCountdown = input.readInt();
						}
					} catch (SODParseException e) {
						throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
					}
				}
				break;
			case FLOAT_ARRAY_SECTION:
				while (arraySizeCountdown --> 0) {
					currentSection.writeForParser(input.readFloat());
				}
				// create new section if available
				if (input.available() > 0) {
					try {
						dataType = DataType.of(input.readByte());
						sectionType = dataType;
						currentSection = dataType.createSection();
						data.put(input.readUTF(), currentSection);
						// set countdown
						if (dataType != DataType.SECTION) {
							arraySizeCountdown = input.readInt();
						}
					} catch (SODParseException e) {
						throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
					}
				}
				break;
			case DOUBLE_ARRAY_SECTION:
				while (arraySizeCountdown --> 0) {
					currentSection.writeForParser(input.readDouble());
				}
				// create new section if available
				if (input.available() > 0) {
					try {
						dataType = DataType.of(input.readByte());
						sectionType = dataType;
						currentSection = dataType.createSection();
						data.put(input.readUTF(), currentSection);
						// set countdown
						if (dataType != DataType.SECTION) {
							arraySizeCountdown = input.readInt();
						}
					} catch (SODParseException e) {
						throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
					}
				}
				break;
			case STRING_ARRAY_SECTION:
				while (arraySizeCountdown --> 0) {
					currentSection.writeForParser(input.readUTF());
				}
				// create new section if available
				if (input.available() > 0) {
					try {
						dataType = DataType.of(input.readByte());
						sectionType = dataType;
						currentSection = dataType.createSection();
						data.put(input.readUTF(), currentSection);
						// set countdown
						if (dataType != DataType.SECTION) {
							arraySizeCountdown = input.readInt();
						}
					} catch (SODParseException e) {
						throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
					}
				}
				break;
			case SECTION:
			default:
				dataType = DataType.of(input.readByte());

				switch (dataType) {
				case BYTE:
					currentSection.writeForParser(input.readByte());
					break;
				case DOUBLE:
					currentSection.writeForParser(input.readDouble());
					break;
				case FLOAT:
					currentSection.writeForParser(input.readFloat());
					break;
				case INT:
					currentSection.writeForParser(input.readInt());
					break;
				case LONG:
					currentSection.writeForParser(input.readLong());
					break;
				case SHORT:
					currentSection.writeForParser(input.readShort());
					break;
				case STRING:
					currentSection.writeForParser(input.readUTF());
					break;
				default:
					try {
						currentSection = dataType.createSection();
						sectionType = dataType;
						data.put(input.readUTF(), currentSection);
						if (dataType != DataType.SECTION) {
							arraySizeCountdown = input.readInt();
						}
					} catch (SODParseException e) {
						throw new RuntimeException("This error should never be thrown! If this error occurs, the parser is not properly dealing with a most-likely-invalid data type.");
					}
					break;
				}
				break;
			}
		}

		return data;
	}

	@SuppressWarnings("rawtypes")
	static void write(BinaryData data, DataOutputStream dos) throws IOException {
		dos.writeLong(0xA77D1E);

		Iterator<Map.Entry<String, BaseDataSection>> sectionStream = data.iterator();

		while (sectionStream.hasNext()) {
			Map.Entry<String, BaseDataSection> entry = sectionStream.next();
			BaseDataSection section = entry.getValue();

			if (section instanceof DataSection) {
				dos.writeByte(DataType.SECTION.id);
				dos.writeUTF(entry.getKey());

				Iterator<Object> dataStream = ((DataSection) section).iterator();

				while (dataStream.hasNext()) {
					Object o = dataStream.next();

					if (o instanceof Byte) {
						dos.writeByte(DataType.BYTE.id);
						dos.writeByte((byte) o);
					} else if (o instanceof Short) {
						dos.writeByte(DataType.SHORT.id);
						dos.writeShort((short) o);
					} else if (o instanceof Integer) {
						dos.writeByte(DataType.INT.id);
						dos.writeInt((int) o);
					} else if (o instanceof Long) {
						dos.writeByte(DataType.LONG.id);
						dos.writeLong((long) o);
					} else if (o instanceof Float) {
						dos.writeByte(DataType.FLOAT.id);
						dos.writeFloat((float) o);
					} else if (o instanceof Double) {
						dos.writeByte(DataType.DOUBLE.id);
						dos.writeDouble((double) o);
					} else if (o instanceof String) {
						dos.writeByte(DataType.STRING.id);
						dos.writeUTF((String) o);
					}
				}
			} else if (section instanceof ByteArrayDataSection) { // byte array
				dos.writeByte(DataType.BYTE_ARRAY_SECTION.id);
				dos.writeUTF(entry.getKey());
				dos.writeInt(((ByteArrayDataSection) section).size());

				for (byte b : (ByteArrayDataSection) section) {
					dos.writeByte(b);
				}
			} else if (section instanceof ShortArrayDataSection) { // short array
				dos.writeByte(DataType.SHORT_ARRAY_SECTION.id);
				dos.writeUTF(entry.getKey());
				dos.writeInt(((ShortArrayDataSection) section).size());

				for (short s : (ShortArrayDataSection) section) {
					dos.writeShort(s);
				}
			} else if (section instanceof IntArrayDataSection) { // int array
				dos.writeByte(DataType.INT_ARRAY_SECTION.id);
				dos.writeUTF(entry.getKey());
				dos.writeInt(((IntArrayDataSection) section).size());

				for (int i : (IntArrayDataSection) section) {
					dos.writeInt(i);
				}
			} else if (section instanceof LongArrayDataSection) { // long array
				dos.writeByte(DataType.LONG_ARRAY_SECTION.id);
				dos.writeUTF(entry.getKey());
				dos.writeInt(((LongArrayDataSection) section).size());

				for (long l : (LongArrayDataSection) section) {
					dos.writeLong(l);
				}
			} else if (section instanceof FloatArrayDataSection) { // float array
				dos.writeByte(DataType.FLOAT_ARRAY_SECTION.id);
				dos.writeUTF(entry.getKey());
				dos.writeInt(((FloatArrayDataSection) section).size());

				for (float f: (FloatArrayDataSection) section) {
					dos.writeFloat(f);
				}
			} else if (section instanceof DoubleArrayDataSection) { // double array
				dos.writeByte(DataType.DOUBLE_ARRAY_SECTION.id);
				dos.writeUTF(entry.getKey());
				dos.writeInt(((DoubleArrayDataSection) section).size());

				for (double d : (DoubleArrayDataSection) section) {
					dos.writeDouble(d);
				}
			} else if (section instanceof StringArrayDataSection) { // string array
				dos.writeByte(DataType.STRING_ARRAY_SECTION.id);
				dos.writeUTF(entry.getKey());
				dos.writeInt(((StringArrayDataSection) section).size());

				for (String s : (StringArrayDataSection) section) {
					dos.writeUTF(s);
				}
			}
		}
	}
}

enum DataType {
	SECTION(0),
	BYTE(1),
	SHORT(2),
	INT(3),
	LONG(4),
	FLOAT(5),
	DOUBLE(6),
	STRING(7),
	BYTE_ARRAY_SECTION(8),
	SHORT_ARRAY_SECTION(9),
	INT_ARRAY_SECTION(10),
	LONG_ARRAY_SECTION(11),
	FLOAT_ARRAY_SECTION(12),
	DOUBLE_ARRAY_SECTION(13),
	STRING_ARRAY_SECTION(14);

	private DataType(int id) {
		this.id = (byte) id;
	}

	public final byte id;

	public static DataType of(byte id) throws SODParseException {
		switch (id) {
		case 0:
			return SECTION;
		case 1:
			return BYTE;
		case 2:
			return SHORT;
		case 3:
			return INT;
		case 4:
			return LONG;
		case 5:
			return FLOAT;
		case 6:
			return DOUBLE;
		case 7:
			return STRING;
		case 8:
			return BYTE_ARRAY_SECTION;
		case 9:
			return SHORT_ARRAY_SECTION;
		case 10:
			return INT_ARRAY_SECTION;
		case 11:
			return LONG_ARRAY_SECTION;
		case 12:
			return FLOAT_ARRAY_SECTION;
		case 13:
			return DOUBLE_ARRAY_SECTION;
		case 14:
			return STRING_ARRAY_SECTION;
		default:
			throw new SODParseException("Unknown data type " + String.valueOf(id));
		}
	}

	@SuppressWarnings("rawtypes")
	BaseDataSection createSection() {
		switch (this) {
		case SECTION:
			return new DataSection();
		case BYTE_ARRAY_SECTION:
			return new ByteArrayDataSection();
		case DOUBLE_ARRAY_SECTION:
			return new DoubleArrayDataSection();
		case FLOAT_ARRAY_SECTION:
			return new FloatArrayDataSection();
		case INT_ARRAY_SECTION:
			return new IntArrayDataSection();
		case LONG_ARRAY_SECTION:
			return new LongArrayDataSection();
		case SHORT_ARRAY_SECTION:
			return new ShortArrayDataSection();
		case STRING_ARRAY_SECTION:
			return new StringArrayDataSection();
		default:
			throw new SODParseException("Tried to create section from non-section data type!");
		}
	}
}