package tk.valoeghese.fc0.client.render;

import tk.valoeghese.fc0.client.render.system.util.ResourceLoader;
import tk.valoeghese.fc0.client.render.system.util.TextureLoader;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.UncheckedIOException;

public class Textures {
	private static int load(String arg0, boolean fallbackToNull) {
		try {
			return TextureLoader.textureARGB(ImageIO.read(ResourceLoader.loadURL("assets/texture/" + arg0 + ".png")));
		} catch (IOException e) {
			e.printStackTrace();

			if (fallbackToNull) {
				return 0;
			} else {
				throw new UncheckedIOException("Error loading Texture " + arg0, e);
			}
		}
	}

	public static final int TILE_ATLAS = load("tile_atlas", false);
	public static final int WATER_OVERLAY = load("water_overlay", true);
	public static final int FONT_ATLAS = load("font_atlas", false);
	public static final int STARTUP = load("startup", false);
	public static final int SELECTED = load("selected", true);
	public static final int CRAFT = load("craft", true);
}
