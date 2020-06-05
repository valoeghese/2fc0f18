package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.util.maths.Vec2i;

import java.util.function.Function;

public class GrassTile extends Tile {
	public GrassTile(String textureName, int id, float iota, float kappa) {
		super(textureName, id, iota, kappa);
	}

	private Vec2i sideUV;
	private Vec2i snowTopUV;
	private Vec2i snowSideUV;

	@Override
	public void requestUV(Function<String, Vec2i> uvs) {
		super.requestUV(uvs);
		this.sideUV = uvs.apply("grass_side");
		this.snowTopUV = uvs.apply("snow_grass");
		this.snowSideUV = uvs.apply("snow_grass_side");
	}

	@Override
	public int getV(int faceAxis, byte meta) {
		if (faceAxis == 4) {
			return Tile.STONE.getV(faceAxis, meta);
		} else if (meta == 1) {
			if (faceAxis == 1) {
				return this.snowTopUV.getY();
			} else {
				return this.snowSideUV.getY();
			}
		} else if (faceAxis == 1) {
			return super.getV(faceAxis, meta);
		} else {
			return this.sideUV.getY();
		}
	}

	@Override
	public int getU(int faceAxis, byte meta) {
		if (faceAxis == 4) {
			return Tile.STONE.getU(faceAxis, meta);
		} else if (meta == 1) {
			if (faceAxis == 1) {
				return this.snowTopUV.getX();
			} else {
				return this.snowSideUV.getX();
			}
		} else if (faceAxis == 1) {
			return super.getU(faceAxis, meta);
		} else {
			return this.sideUV.getX();
		}
	}
}
