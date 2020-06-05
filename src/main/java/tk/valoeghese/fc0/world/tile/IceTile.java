package tk.valoeghese.fc0.world.tile;

public class IceTile extends Tile {
	public IceTile(String textureName, int id, float iota, float kappa) {
		super(textureName, id, iota, kappa);
	}

	@Override
	public float getFrictionConstant() {
		return 0.93f;
	}
}
