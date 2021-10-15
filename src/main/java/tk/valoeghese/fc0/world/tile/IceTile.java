package tk.valoeghese.fc0.world.tile;

import javax.annotation.Nullable;

public class IceTile extends Tile {
	public IceTile(String textureName, int id, float natureness) {
		super(textureName, id, natureness);
	}

	@Override
	public float getFrictionConstant() {
		return 0.88f;
	}

	@Override
	public boolean isOpaque(boolean waterRenderLayer, @Nullable Tile comparableTo) {
		return comparableTo == this;
	}
}
