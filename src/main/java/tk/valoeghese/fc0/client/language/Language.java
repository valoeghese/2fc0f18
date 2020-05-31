package tk.valoeghese.fc0.client.language;

import tk.valoeghese.fc0.client.system.util.ResourceLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Language {
	private Language(String id, String name) {
		this(id, name, null);
	}

	private Language(String id, String name, @Nullable String parent) {
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.load();
	}

	public final String name;
	private final String id;
	@Nullable
	private final String parent;
	private Properties values;

	public void load() {
		Properties defaults = null;

		if (this.parent != null) {
			defaults = new Properties();

			try (InputStream is = ResourceLoader.load("data/language/" + this.parent + ".txt")) {
				defaults.load(is);
			} catch (IOException e) {
				throw new RuntimeException("Error loading parent language file!");
			}
		}

		this.values = defaults == null ? new Properties() : new Properties(defaults);

		try (InputStream is = ResourceLoader.load("data/language/" + this.id + ".txt")) {
			this.values.load(is);
		} catch (IOException e) {
			throw new RuntimeException("Error loading language file!");
		}
	}

	public String translate(String key) {
		return this.values.getProperty(key, key);
	}

	public static final Language EN_GB = new Language("en.gb", "British English");
	public static final Language EN_US = new Language("en.us", "American English", "en.gb");
}
