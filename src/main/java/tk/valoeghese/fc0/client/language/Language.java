package tk.valoeghese.fc0.client.language;

import tk.valoeghese.fc0.client.system.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Language {
	private Language(String id, String name) {
		this.id = id;
		this.name = name;
		this.load();
	}

	public final String name;
	private final String id;
	private Properties values;

	public void load() {
		this.values = new Properties();

		try (InputStream is = Resources.load("data/language/" + this.id + ".txt")) {
			this.values.load(is);
		} catch (IOException e) {
			throw new RuntimeException("Error loading language file!");
		}
	}

	public String translate(String key) {
		return this.values.getProperty(key, key);
	}

	public static final Language EN_GB = new Language("en.gb", "British English");
	public static final Language EN_US = new Language("en.us", "American English");
}
