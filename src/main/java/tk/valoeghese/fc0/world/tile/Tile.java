package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.player.Item;

import javax.annotation.Nullable;
import java.util.Random;

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
	private boolean translucent = false;
	private boolean solid = true;

	public int getU(int faceAxis, byte meta) {
		return this.u;
	}

	public int getV(int faceAxis, byte meta) {
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

	protected Tile translucent() {
		this.translucent = true;
		this.cutout();
		return this;
	}

	protected Tile noCollision() {
		this.solid = false;
		return this;
	}

	@Nullable
	public Item getDrop(Random rand) {
		return new Item(this);
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

	public boolean canPlaceAt(GenWorld world, int x, int y, int z) {
		return true;
	}

	public boolean isTranslucent() {
		return this.translucent;
	}

	public boolean isSolid() {
		return this.solid;
	}

	public float getFrictionConstant() {
		return 1.0f;
	}

	public static final Tile[] BY_ID = new Tile[256];
	public static final Tile AIR = new Tile(0, 0, 0, 0.0f, 0.02f).dontRender().noCollision();
	public static final Tile STONE = new Tile(1, 0, 1, 0.01f, 0.01f);
	public static final Tile GRASS = new GrassTile(2, 0.01f, 0.03f);
	public static final Tile LEAVES = new Tile(3, 2, 1, 0.02f, 0.1f).cutout().noCollision();
	public static final Tile LOG = new ColumnTile(4, 2, 0, 0.04f, 0.04f);
	public static final Tile WATER = new WaterTile(5, 3, 1, 0.05f, 0.14f).dontRender().noCollision();
	public static final Tile SAND = new Tile(6, 0, 2, 0.025f, 0.01f);
	public static final Tile DAISY = new PlantTile(7, 1, 2, 0.001f, 0.12f, GRASS).cross().noCollision();
	public static final Tile CACTUS = new PlantTile(8, 2, 2, 0.002f, 0.06f, SAND).cross().noCollision();
	public static final Tile TALLGRASS = new PlantTile(9, 3, 2, 0.002f, 0.14f, GRASS).cross().noCollision();
	public static final Tile BRICKS = new Tile(10, 4, 2, 0.02f, 0.01f);
	public static final Tile STONE_BRICKS = new Tile(11, 5, 2, 0.015f, 0.011f);
	public static final Tile ICE = new IceTile(12, 5, 0, 0.05f, 0.14f).translucent();
	public static final Tile GALENA = new Tile(13, 5, 2, 0.01f, 0.01f);
}
