package tk.valoeghese.fc0.util;

public class RaycastResult {
	public RaycastResult(TilePos pos, Face face) {
		this.pos = pos;
		this.face = face;
	}

	public final TilePos pos;
	public final Face face;
}
