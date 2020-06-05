package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.world.gen.GenWorld;

public class PlantTile extends Tile {
	public PlantTile(String textureName, int id, float iota, Tile support) {
		super(textureName, id, iota);
		this.support = support.id;
	}

	private final byte support;

	@Override
	public boolean canPlaceAt(GenWorld world, int x, int y, int z) {
		if (y == 0) {
			return false;
		}

		byte below = world.readTile(x, y - 1, z);

		if (this == Tile.CACTUS && below == this.id) {
			return true;
		}

		return below == this.support;
	}
}
