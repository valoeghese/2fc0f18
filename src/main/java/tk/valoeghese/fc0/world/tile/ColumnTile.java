package tk.valoeghese.fc0.world.tile;

public class ColumnTile extends Tile {
	public ColumnTile(int id, int u, int v, float iota, float kappa) {
		super(id, u, v, iota, kappa);
	}

	@Override
	public int getU(int faceAxis) {
		int sResult = super.getU(faceAxis);
		return faceAxis == 1 || faceAxis == 4 ? sResult + 1 : sResult;
	}
}
