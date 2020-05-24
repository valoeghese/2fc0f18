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
	private boolean cross = false;

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

	protected Tile cross() {
		this.cross = true;
		this.cutout();
		return this;
	}

	public final boolean isOpaque() {
		return this.isOpaque(false);
	}

	public boolean isOpaque(boolean waterRenderLayer) {
		return this.opaque;
	}

	public boolean dontOptimiseOut() {
		return this.shouldRender();
	}

	public boolean isCross() {
		return this.cross;
	}

	public boolean shouldRender() {
		return this.render;
	}

	public static final Tile[] BY_ID = new Tile[256];
	public static final Tile AIR = new Tile(0, 0, 0, 0.0f, 0.02f).dontRender();
	public static final Tile STONE = new Tile(1, 0, 1, 0.01f, 0.01f);
	public static final Tile GRASS = new GrassTile(2, 0.01f, 0.03f);
	public static final Tile LEAVES = new Tile(3, 2, 1, 0.02f, 0.1f).cutout();
	public static final Tile LOG = new ColumnTile(4, 2, 0, 0.04f, 0.04f);
	public static final Tile WATER = new WaterTile(5, 3, 1, 0.05f, 0.14f).dontRender();
	public static final Tile SAND = new Tile(6, 0, 2, 0.025f, 0.01f);
	public static final Tile DAISY = new Tile(7, 1, 2, 0.001f, 0.12f).cross();
	public static final Tile CACTUS = new Tile(8, 2, 2, 0.002f, 0.06f).cross();
	public static final Tile TALLGRASS = new Tile(9, 3, 2, 0.002f, 0.14f).cross();
}
