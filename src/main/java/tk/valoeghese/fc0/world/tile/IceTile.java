package tk.valoeghese.fc0.world.tile;

import javax.annotation.Nullable;

public class IceTile extends Tile {
	public IceTile(String textureName, int id, float iota) {
		super(textureName, id, iota);
	}

	@Override
	public float getFrictionConstant() {
		return 0.93f;
	}

	@Override
	public boolean isOpaque(boolean waterRenderLayer, @Nullable Tile comparableTo) {
		return comparableTo == this;
	}
}
