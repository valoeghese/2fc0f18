package tk.valoeghese.fc0.world.tile;

public class Tile {
	public Tile(int id, int u, int v, float iota, float kappa) {
		BY_ID[id] = this;
		this.id = (byte) id;
		this.u = u;
		this.v = v;
		this.iota = iota;
		this.kappa = kappa;
	}

	public final byte id;
	protected final int u;
	protected final int v;
	// TODO use these in epic stuff
	public final float iota;
	public final float kappa;
	private boolean opaque = true;
	private boolean render = true;

	public int getU(int faceAxis) {
		return this.u;
	}

	public int getV(int faceAxis) {
		return this.v;
	}

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
	public static final Tile AIR = new Tile(0, 0, 0, 0.0f, 0.02f).dontRender();
	public static final Tile STONE = new Tile(1, 0, 1, 0.01f, 0.01f);
	public static final Tile GRASS = new GrassTile(2, 0.01f, 0.03f);
	public static final Tile LEAVES = new Tile(3, 2, 1, 0.02f, 0.1f).cutout();
	public static final Tile LOG = new LogTile(4, 2, 0, 0.04f, 0.04f);
}
