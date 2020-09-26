package tk.valoeghese.fc0.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.UnaryOperator;

import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.util.raycasting.Vec3d;

public enum Face implements UnaryOperator<TilePos> {
	NORTH(0, 0, 1),
	SOUTH(0, 0, -1),
	EAST(1, 0, 0),
	WEST(-1, 0, 0),
	UP(0, 1, 0),
	DOWN(0, -1, 0);

	Face(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	private final int x;
	private final int y;
	private final int z;

	public static Face findFace(Vec3d direction) {
		Face[] faces = Face.values();
		Arrays.sort(faces, Comparator.comparingDouble(face -> face.x * direction.x + face.y * direction.y + face.z * direction.z));
		return faces[0];
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public TilePos apply(TilePos original) {
		return original.ofAdded(this.x, this.y, this.z);
	}

	public Pos half() {
		return new Pos((double) this.x * 0.5, (double) this.y * 0.5, (double) this.z * 0.5);
	}

	public Face reverse() {
		switch (this) {
		case NORTH:
			return SOUTH;
		case SOUTH:
			return NORTH;
		case EAST:
			return WEST;
		case WEST:
			return EAST;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default: // muri desu
			return null;
		}
	}
}
