package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.world.gen.GenWorld;

import java.util.function.Predicate;

public class PlantTile extends Tile {
	public PlantTile(String textureName, int id, float iota, Predicate<Tile> support) {
		super(textureName, id, iota);
		this.support = support;
	}

	private final Predicate<Tile> support;

	@Override
	public boolean canPlaceAt(GenWorld world, int x, int y, int z) {
		if (y == 0) {
			return false;
		}

		byte below = world.readTile(x, y - 1, z);

		if (this == Tile.CACTUS && below == this.id) {
			return true;
		}

		return this.support.test(Tile.BY_ID[below]);
	}
}
