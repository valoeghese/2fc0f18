package tk.valoeghese.fc0.util;

import java.util.function.UnaryOperator;

public enum Face implements UnaryOperator<TilePos> {
	NORTH(0, 0, 1),
	SOUTH(0, 0, -1),
	EAST(1, 0, 0),
	WEST(-1, 0, 0),
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

	@Override
	public TilePos apply(TilePos original) {
		return original.ofAdded(this.x, this.y, this.z);
	}
}
