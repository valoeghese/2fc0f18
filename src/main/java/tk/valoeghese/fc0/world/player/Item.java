package tk.valoeghese.fc0.world.player;

import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;

public class Item {
	public Item(Tile tile) {
		this(tile, 0);
	}

	public Item(Tile tile, int meta) {
		this.tileValue = tile;
		this.tile = true;
		this.meta = (byte) meta;
	}

	private final boolean tile;
	@Nullable
	private final Tile tileValue;
	private final byte meta;

	public boolean isTile() {
		return this.tile;
	}

	@Nullable
	public Tile tileValue() {
		return this.tileValue;
	}

	public byte getMeta() {
		return this.meta;
	}
}
