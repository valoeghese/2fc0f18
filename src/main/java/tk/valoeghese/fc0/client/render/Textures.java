package tk.valoeghese.fc0.client.render;

import org.lwjgl.BufferUtils;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.player.ItemType;
import tk.valoeghese.fc0.world.tile.Tile;
import valoeghese.scalpel.GeneratedAtlas;
import valoeghese.scalpel.util.ResourceLoader;
import valoeghese.scalpel.util.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL33.*;

public class Textures {
	private static int load(String name, boolean fallbackToNull) {
		try {
			return TextureLoader.textureARGB(ImageIO.read(ResourceLoader.loadURL("assets/texture/" + name + ".png")));
		} catch (IOException e) {
			e.printStackTrace();

			if (fallbackToNull) {
				return 0;
			} else {
				throw new UncheckedIOException("Error loading Texture " + name, e);
			}
		}
	}

	private static BufferedImage scaledAtlas(String name, GeneratedAtlas.ImageEntry[] images, int scale) {
		final int atlasSize = 256 >> scale;
		final int componentSize = 16 >> scale;

		name = name + "_" + atlasSize + "x";
		int x = -1;
		int y = 0;
		BufferedImage result = new BufferedImage(atlasSize, atlasSize, BufferedImage.TYPE_INT_ARGB);

		for (GeneratedAtlas.ImageEntry iEntry : images) {
			BufferedImage image = iEntry.image;
			++x;

			if (x > 15) {
				x = 0;
				++y;
			}

			if (y > 15) {
				throw new RuntimeException("Scaled Atlas \"" + name + "\" is too large!");
			}

			if (image.getWidth() != 16 || image.getHeight() != 16) {
				throw new RuntimeException("Invalidly sized image encountered while generating atlas \"" + name + "\"!");
			}

			int startX = x << (4 - scale);
			int startY = (15 - y) << (4 - scale);

			for (int xo = 0; xo < componentSize; ++xo) {
				int totalX = startX + xo;

				for (int yo = 0; yo < componentSize; ++yo) {
					int totalY = startY + yo;

					result.setRGB(totalX, totalY, image.getRGB(xo << scale, yo << scale));
				}
			}
		}

		System.out.println("Successfully Scaled Atlas \"" + name + "\"");

//		File temp = new File("./temp_" + name + ".png");
//		try {
//			ImageIO.write(result, "png", temp);
//		} catch (Exception e) {
//			System.exit(-3);
//		}
//
		return result;
	}

	// TODO move a bunch of this mipmap stuff to scalpel
	private static ByteBuffer imageBuffer(BufferedImage image) {
		final int width = image.getWidth();
		final int height = image.getHeight();
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); // 4 bytes for ARGB
		int[] pixels = new int[width *  height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int v = 0; v < height; ++v) {
			for (int u = 0; u < width; ++u) {
				int pixel = pixels[v * width + u];
				// convert ARGB to RGBA
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // r
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // g
				buffer.put((byte) (pixel & 0xFF)); // b
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // a
			}
		}

		buffer.flip();
		return buffer;
	}

	private static int load(GeneratedAtlas atlas) {
		int result = TextureLoader.textureARGB(atlas.image);

		if (mipmap_128 != null) {
			// mipmapping
			ByteBuffer buffer_128 = imageBuffer(mipmap_128);
			ByteBuffer buffer_64 = imageBuffer(mipmap_64);
			ByteBuffer buffer_32 = imageBuffer(mipmap_32);
			glBindTexture(GL_TEXTURE_2D, result);
			glTexImage2D(GL_TEXTURE_2D, 1, GL_RGBA8, mipmap_128.getWidth(), mipmap_128.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer_128);
			glTexImage2D(GL_TEXTURE_2D, 2, GL_RGBA8, mipmap_64.getWidth(), mipmap_64.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer_64);
			glTexImage2D(GL_TEXTURE_2D, 3, GL_RGBA8, mipmap_32.getWidth(), mipmap_32.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer_32);
			glBindTexture(GL_TEXTURE_2D, 0);
			mipmap_128 = null;
			mipmap_32 = null;
			mipmap_64 = null;
		}

		return result;
	}

	private static GeneratedAtlas loadAtlas(String name, Consumer<Set<String>> populator) {
		try {
			List<GeneratedAtlas.ImageEntry> images = new ArrayList<>();
			Set<String> entries = new LinkedHashSet<>();
			populator.accept(entries);

			for (String entry : entries) {
				GeneratedAtlas.ImageEntry iEntry = new GeneratedAtlas.ImageEntry();
				iEntry.name = entry.trim();
				iEntry.image = ImageIO.read(
						ResourceLoader.loadURL("assets/texture/" + name + "/" + entry.trim() + ".png")
				);
				images.add(iEntry);
			}

			GeneratedAtlas.ImageEntry[] entries_ = images.toArray(GeneratedAtlas.ImageEntry[]::new);
			mipmap_128 = scaledAtlas(name, entries_, 1);
			mipmap_64 = scaledAtlas(name, entries_, 2);
			mipmap_32 = scaledAtlas(name, entries_, 3);
			return new GeneratedAtlas(name, entries_);
		} catch (IOException e) {
			throw new UncheckedIOException("Error generating Texture Atlas" + name, e);
		}
	}

	private static GeneratedAtlas loadItemAtlas(String name, Consumer<Set<String>> populator) {
		try {
			List<GeneratedAtlas.ImageEntry> images = new ArrayList<>();
			Set<String> entrySet = new LinkedHashSet<>();
			populator.accept(entrySet);
			String[] entries = entrySet.toArray(new String[0]);

			int i = 0;

			for (;;++i) {
				String entry = entries[i];

				if (entry.equals("./$break")) {
					++i;
					break;
				}

				GeneratedAtlas.ImageEntry iEntry = new GeneratedAtlas.ImageEntry();
				iEntry.name = entry.trim();
				iEntry.image = ImageIO.read(
						ResourceLoader.loadURL("assets/texture/tile/" + entry.trim() + ".png")
				);
				images.add(iEntry);
			}

			for (;i < entries.length;++i) {
				String entry = entries[i];

				GeneratedAtlas.ImageEntry iEntry = new GeneratedAtlas.ImageEntry();
				iEntry.name = entry.trim();
				iEntry.image = ImageIO.read(
						ResourceLoader.loadURL("assets/texture/" + name + "/" + entry.trim() + ".png")
				);
				images.add(iEntry);
			}

			GeneratedAtlas.ImageEntry[] entries_ = images.toArray(GeneratedAtlas.ImageEntry[]::new);
			mipmap_128 = scaledAtlas(name, entries_, 1);
			mipmap_64 = scaledAtlas(name, entries_, 2);
			mipmap_32 = scaledAtlas(name, entries_, 3);
			return new GeneratedAtlas(name, entries_);
		} catch (IOException e) {
			throw new UncheckedIOException("Error generating Item Texture Atlas" + name, e);
		}
	}

	public static void loadGeneratedAtlases() {
		// tile atlas
		TILE_ATLAS_OBJ = loadAtlas("tile", entries -> {
			entries.add("missingno");

			for (Tile tile : Tile.BY_ID) {
				if (tile != null) {
					tile.requestUV(str -> { // dummy code to collect textures
						entries.add(str);
						return new Vec2i(0, 0);
					});
				}
			}
		});

		TILE_ATLAS = load(TILE_ATLAS_OBJ);

		// item atlas
		ITEM_ATLAS_OBJ = loadItemAtlas("item", entries -> {
			entries.add("missingno");

			for (Tile tile : Tile.BY_ID) {
				if (tile != null) {
					tile.requestUV(str -> { // dummy code to collect textures again
						entries.add(str);
						return new Vec2i(0, 0);
					});
				}
			}

			entries.add("./$break"); // key for tile end, item begin

			for (ItemType item : ItemType.ITEMS) {
				if (item != null) {
					item.requestUV(str -> { // dummy code to collect textures yet once more
						entries.add(str);
						return new Vec2i(0, 0);
					});
				}
			}
		});

		ITEM_ATLAS = load(ITEM_ATLAS_OBJ);
	}

	private static BufferedImage mipmap_128; // cache
	private static BufferedImage mipmap_64; // cache 2
	private static BufferedImage mipmap_32; // cache 3

	public static GeneratedAtlas TILE_ATLAS_OBJ;
	public static GeneratedAtlas ITEM_ATLAS_OBJ;
	public static int TILE_ATLAS = 0;
	public static int ITEM_ATLAS = 0;
	public static int ENTITY_ATLAS = 0;
	// Overlays
	public static final int WATER_OVERLAY = load("overlay/water_overlay", true);
	public static final int DEATH_OVERLAY = load("overlay/death_overlay", true);
	public static final int DIM = load("overlay/dim", false);
	// GUI
	public static final int FONT_ATLAS = load("gui/font_atlas", false);
	public static final int SELECTED = load("gui/selected", true);
	public static final int CRAFT = load("gui/craft", true);
	public static final int ENTER = load("gui/enter", true);
	public static final int CRAFTING = load("gui/crafting", false);
	public static final int HEALTH = load("gui/stat/health", true);
	// OTHER
	public static final int STARTUP = load("startup", false);
	public static final int THE_SUN = load("the_sun", true);
}
