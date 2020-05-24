package tk.valoeghese.fc0.world.tile;

public class GrassTile extends Tile {
	public GrassTile(int id, float iota, float kappa) {
		super(id, 1, 0, iota, kappa);
	}

	@Override
	public int getV(int faceAxis, byte meta) {
		return faceAxis == 1 ? 0 : 1;
	}

	@Override
	public int getU(int faceAxis, byte meta) {
		if (faceAxis == 4) {
			return 0;
		} else {
			return meta == 1 ? 4 : 1;
		}
	}
}
