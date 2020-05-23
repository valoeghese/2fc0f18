package tk.valoeghese.fc0.client.model;

import tk.valoeghese.fc0.client.system.Resources;
import tk.valoeghese.fc0.client.system.TextureLoader;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.UncheckedIOException;

public class Textures {
	private static int load(String arg0, boolean fallbackToNull) {
		try {
			return TextureLoader.textureARGB(ImageIO.read(Resources.loadURL("assets/texture/" + arg0 + ".png")));
		} catch (IOException e) {
			e.printStackTrace();

			if (fallbackToNull) {
				return 0;
			} else {
				throw new UncheckedIOException("Error loading Tile Atlas", e);
			}
		}
	}

	public static final int TILE_ATLAS = load("tile_atlas", false);
	public static final int VERSION = load("version", true);
	public static final int WATER_OVERLAY = load("water_overlay", true);
	public static final int FONT_ATLAS = load("font_atlas", false);
}
