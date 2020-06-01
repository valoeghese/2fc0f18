package tk.valoeghese.fc0.world.tile;

public class IceTile extends Tile {
	public IceTile(int id, int u, int v, float iota, float kappa) {
		super(id, u, v, iota, kappa);
	}

	@Override
	public float getFrictionConstant() {
		return 0.93f;
	}
}
