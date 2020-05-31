package tk.valoeghese.fc0.client.system.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ResourceLoader {
	public static URL loadURL(String location) {
		return ResourceLoader.class.getClassLoader().getResource(location);
	}

	public static InputStream load(String location) {
		return ResourceLoader.class.getClassLoader().getResourceAsStream(location);
	}

	public static String loadAsString(String location) throws IOException {
		InputStream is = load(location);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nBytesRead;
		byte[] bufferBuffer = new byte[0x4000];

		while ((nBytesRead = is.read(bufferBuffer, 0, bufferBuffer.length)) != -1) {
			buffer.write(bufferBuffer, 0, nBytesRead);
		}

		return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
	}
}
