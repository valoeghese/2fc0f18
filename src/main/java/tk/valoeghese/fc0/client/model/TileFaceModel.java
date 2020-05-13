package tk.valoeghese.fc0.client.model;

import tk.valoeghese.fc0.client.system.Model;
import tk.valoeghese.fc0.client.system.Resources;
import tk.valoeghese.fc0.client.system.Shader;
import tk.valoeghese.fc0.client.system.TextureLoader;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

public class TileFaceModel extends Model {
	public static final int TILE_ATLAS;

	public TileFaceModel(int uIndex, int vIndex, int faceAxis) {
		super(GL_DYNAMIC_DRAW, Shaders.terrain);

		final float startU = (uIndex / 16.0f);
		final float startV = (vIndex / 16.0f);
		final float endU = startU + 0.0625f;
		final float endV = startV + 0.0625f;

		switch (faceAxis) {
			case 0:
			default:
				this.vertex(0.0f, 0.5f, -0.5f, startU, endV); // tl
				this.vertex(0.0f, -0.5f, -0.5f, startU, startV); // bl
				this.vertex(0.0f, 0.5f, 0.5f, endU, endV); // tr
				this.vertex(0.0f, -0.5f, 0.5f, endU, startV); // br
				break;
			case 1:
				this.vertex(-0.5f, 0.0f, 0.5f, startU, endV); // tl
				this.vertex(-0.5f, 0.0f, -0.5f, startU, startV); // bl
				this.vertex(0.5f, 0.0f, 0.5f, endU, endV); // tr
				this.vertex(0.5f, 0.0f, -0.5f, endU, startV); // br
				break;
			case 2:
				this.vertex(-0.5f, 0.5f, 0.0f, startU, endV); // tl
				this.vertex(-0.5f, -0.5f, 0.0f, startU, startV); // bl
				this.vertex(0.5f, 0.5f, 0.0f, endU, endV); // tr
				this.vertex(0.5f, -0.5f, 0.0f, endU, startV); // br
				break;
		}
		this.tri(0, 1, 3);
		this.tri(0, 2, 3);

		this.generateBuffers();
	}

	static {
		try {
			TILE_ATLAS = TextureLoader.textureARGB(ImageIO.read(Resources.loadURL("assets/texture/tile_atlas.png")));
		} catch (IOException e) {
			throw new UncheckedIOException("Error loading Tile Atlas", e);
		}
	}
}
