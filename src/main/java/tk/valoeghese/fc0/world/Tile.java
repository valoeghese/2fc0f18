package tk.valoeghese.fc0.world;

public class Tile {
	public Tile(int id, int u, int v) {
		BY_ID[id] = this;
		this.id = (byte) id;
		this.u = u;
		this.v = v;
	}

	public final byte id;
	public final int u;
	public final int v;
	private boolean opaque = true;
	private boolean render = true;

	protected Tile dontRender() {
		this.render = false;
		this.cutout();
		return this;
	}

	protected Tile cutout() {
		this.opaque = false;
		return this;
	}

	public boolean isOpaque() {
		return this.opaque;
	}

	public boolean shouldRender() {
		return this.render;
	}

	public static final Tile[] BY_ID = new Tile[256];
	public static final Tile AIR = new Tile(0, 0, 0).dontRender();
	public static final Tile STONE = new Tile(1, 0, 1);
	public static final Tile GRASS = new Tile(2, 1, 0);
}
