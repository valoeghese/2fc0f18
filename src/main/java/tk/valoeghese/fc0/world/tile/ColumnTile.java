package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.util.maths.Vec2i;

import java.util.function.Function;

public class ColumnTile extends Tile {
	public ColumnTile(String textureName, int id, float iota, float kappa) {
		super(textureName, id, iota, kappa);
		this.endTextureName = textureName + "_end";
	}

	private final String endTextureName;
	private int endU;
	private int endV;

	@Override
	public void requestUV(Function<String, Vec2i> uvs) {
		super.requestUV(uvs);
		Vec2i endUV = uvs.apply(this.endTextureName);
		this.endU = endUV.getX();
		this.endV = endUV.getY();
	}

	@Override
	public int getU(int faceAxis, byte meta) {
		return faceAxis == 1 || faceAxis == 4 ? this.endU : super.getU(faceAxis, meta);
	}

	@Override
	public int getV(int faceAxis, byte meta) {
		return faceAxis == 1 || faceAxis == 4 ? this.endV : super.getV(faceAxis, meta);
	}
}
