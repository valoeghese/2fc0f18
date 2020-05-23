package tk.valoeghese.fc0.util;

import tk.valoeghese.fc0.util.maths.TilePos;

public class RaycastResult {
	public RaycastResult(TilePos pos, Face face) {
		this.pos = pos;
		this.face = face;
	}

	public final TilePos pos;
	public final Face face;
}
