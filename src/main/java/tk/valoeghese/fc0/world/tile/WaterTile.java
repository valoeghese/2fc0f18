package tk.valoeghese.fc0.world.tile;

public class WaterTile extends Tile {
	public WaterTile(int id, int u, int v, float iota, float kappa) {
		super(id, u, v, iota, kappa);
	}

	@Override
	public boolean isOpaque(boolean waterRenderLayer) {
		return waterRenderLayer ? true : false;
	}

	@Override
	public boolean dontOptimiseOut() {
		return true;
	}
}
