package tk.valoeghese.fc0.util.raycasting;

import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.tile.Tile;

public class WorldSolidBlockDistanceFunction implements DistanceFunction {

	private final World world;

	public WorldSolidBlockDistanceFunction(World world) {
		this.world = world;
	}

	@Override
	public double length(Vec3d point) {
		if (Tile.BY_ID[world.readTile(new TilePos((float) point.x, (float) point.y, (float) point.z))].shouldRender()) {
			return 0;
		}

		return 1D / (2 << 6);
	}
}
