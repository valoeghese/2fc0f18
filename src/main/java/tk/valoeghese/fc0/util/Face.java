package tk.valoeghese.fc0.util;

import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;

import java.util.function.UnaryOperator;

public enum Face implements UnaryOperator<TilePos> {
	EAST(0, 0, 1),
	WEST(0, 0, -1),
	NORTH(1, 0, 0),
	SOUTH(-1, 0, 0),
	UP(0, 1, 0),
	DOWN(0, -1, 0);

	Face (int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	private final int x;
	private final int y;
	private final int z;

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
		case EAST:
			return WEST;
		case WEST:
			return EAST;
		case NORTH:
			return SOUTH;
		case SOUTH:
			return NORTH;
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		default: // muri desu
			return null;
		}
	}
}
