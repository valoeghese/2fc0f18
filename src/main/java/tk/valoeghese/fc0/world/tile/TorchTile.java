package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.client.render.tile.TileRenderer;
import tk.valoeghese.fc0.client.render.tile.TorchRenderer;

import javax.annotation.Nullable;

public class TorchTile extends Tile {
	public TorchTile(String textureName, int id, float iota) {
		super(textureName, id, iota);
	}

	@Nullable
	@Override
	public TileRenderer getCustomTileRenderer() {
		return TorchRenderer.INSTANCE;
	}
}
