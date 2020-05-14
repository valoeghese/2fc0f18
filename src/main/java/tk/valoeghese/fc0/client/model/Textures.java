package tk.valoeghese.fc0.client.model;

import tk.valoeghese.fc0.client.system.Resources;
import tk.valoeghese.fc0.client.system.TextureLoader;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.UncheckedIOException;

public class Textures {
	public static final int TILE_ATLAS;

	static {
		try {
			TILE_ATLAS = TextureLoader.textureARGB(ImageIO.read(Resources.loadURL("assets/texture/tile_atlas.png")));
		} catch (IOException e) {
			throw new UncheckedIOException("Error loading Tile Atlas", e);
		}
	}
}
