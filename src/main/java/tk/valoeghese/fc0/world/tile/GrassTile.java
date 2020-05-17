package tk.valoeghese.fc0.world.tile;

public class GrassTile extends Tile {
	public GrassTile(int id) {
		super(id, 1, 0);
	}

	@Override
	public int getV(int faceAxis) {
		return faceAxis == 1 ? 0 : 1;
	}
}
