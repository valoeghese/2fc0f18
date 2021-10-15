package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.client.render.tile.TileRenderer;
import tk.valoeghese.fc0.client.render.tile.TorchRenderer;
import tk.valoeghese.fc0.world.player.ItemType;

import javax.annotation.Nullable;

public class TorchTile extends Tile {
	public TorchTile(String textureName, int id, float natureness) {
		super(textureName, id, natureness);
	}

	@Nullable
	@Override
	public TileRenderer getCustomTileRenderer() {
		return TorchRenderer.INSTANCE;
	}

	@Nullable
	@Override
	public ItemType delegateItem() {
		return ItemType.TORCH;
	}
}
