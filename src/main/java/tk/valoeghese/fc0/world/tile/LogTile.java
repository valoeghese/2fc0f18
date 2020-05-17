package tk.valoeghese.fc0.world.tile;

public class LogTile extends Tile {
	public LogTile(int id, int u, int v, float iota, float kappa) {
		super(id, u, v, iota, kappa);
	}

	@Override
	public int getU(int faceAxis) {
		int sResult = super.getU(faceAxis);
		return faceAxis == 1 ? sResult + 1 : sResult;
	}
}
