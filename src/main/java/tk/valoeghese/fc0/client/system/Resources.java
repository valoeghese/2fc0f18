package tk.valoeghese.fc0.client.system;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public final class Resources {
	public static URL loadURL(String location) {
		return Resources.class.getClassLoader().getResource(location);
	}

	public static File load(String location) {
		File file = new File(Resources.class.getClassLoader().getResource(location).getFile());
		return file.exists() ? file : null;
	}

	public static String loadAsString(String location) throws IOException {
		return new String(Files.readAllBytes(load(location).toPath()));
	}
}
