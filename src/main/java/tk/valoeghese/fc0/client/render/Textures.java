package tk.valoeghese.fc0.client.render;

import tk.valoeghese.fc0.client.render.system.GeneratedAtlas;
import tk.valoeghese.fc0.client.render.system.util.ResourceLoader;
import tk.valoeghese.fc0.client.render.system.util.TextureLoader;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.player.ItemType;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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

	private static int load(GeneratedAtlas atlas) {
		return TextureLoader.textureARGB(atlas.image);
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

			return new GeneratedAtlas(name, images.toArray(new GeneratedAtlas.ImageEntry[0]));
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

			return new GeneratedAtlas(name, images.toArray(new GeneratedAtlas.ImageEntry[0]));
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

			for (ItemType item : ItemType.BY_ID) {
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

	public static GeneratedAtlas TILE_ATLAS_OBJ;
	public static GeneratedAtlas ITEM_ATLAS_OBJ;
	public static int TILE_ATLAS = 0;
	public static int ITEM_ATLAS = 0;
	public static final int WATER_OVERLAY = load("water_overlay", true);
	public static final int FONT_ATLAS = load("font_atlas", false);
	public static final int STARTUP = load("startup", false);
	public static final int SELECTED = load("selected", true);
	public static final int CRAFT = load("craft", true);
	public static final int ENTER = load("enter", true);
	public static final int CRAFTING = load("crafting", false);
}
