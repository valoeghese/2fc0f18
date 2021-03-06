package tk.valoeghese.fc0.world.player;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CraftingManager {
	private Int2ObjectMap<List<Item>> crafting = new Int2ObjectArrayMap<>();

	public void addCraftingRecipe(int id, Item out) {
		this.crafting.computeIfAbsent(id, i -> new ArrayList<>()).add(out);
	}

	@Nullable
	public List<Item> getCraftables(int id) {
		return this.crafting.get(id);
	}

	public static void addCraftingRecipes() {
		CRAFTING.addCraftingRecipe(Tile.STONE.id, new Item(Tile.STONE_BRICKS));
		CRAFTING.addCraftingRecipe(Tile.STONE.id, new Item(Tile.BRICKS));
		CRAFTING.addCraftingRecipe(Tile.LOG.id, new Item(Tile.PLANKS));
		CRAFTING.addCraftingRecipe(Tile.PLANKS.id, new Item(Tile.TORCH));
	}

	public static final CraftingManager CRAFTING = new CraftingManager();
	// todo smelting
	// todo magic - perhaps this should be separate, since magic will be the main focus of the game
	// todo magic is done via a corruption/purification method. Iota is the level of purification, negative is corruption.
	// players also specialise in one of the 6 elements: Fire, Air, Earth, Water, Nature, Electricity, which each have a corruption and purification side.
}
