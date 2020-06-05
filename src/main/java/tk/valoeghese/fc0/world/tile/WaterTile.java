package tk.valoeghese.fc0.world.tile;

public class WaterTile extends Tile {
	public WaterTile(String textureName, int id, float iota) {
		super(textureName, id, iota);
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
