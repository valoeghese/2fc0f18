package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.world.gen.GenWorld;

public class PlantTile extends Tile {
	public PlantTile(int id, int u, int v, float iota, float kappa, Tile support) {
		super(id, u, v, iota, kappa);
		this.support = support.id;
	}

	private final byte support;

	@Override
	public boolean canPlaceAt(GenWorld world, int x, int y, int z) {
		if (y == 0) {
			return false;
		}

		return world.readTile(x, y - 1, z) == this.support;
	}
}
