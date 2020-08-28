package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.client.render.tile.TileRenderer;
import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.player.Item;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Function;

public class Tile {
	public Tile(String textureName, int id, float iota) {
		BY_ID[id] = this;
		this.id = (byte) id;
		this.iota = iota;
		this.textureName = textureName;
	}

	public final byte id;
	protected int u;
	protected int v;
	// TODO use this (funni unstability magic system)
	public final float iota;
	private boolean opaque = true;
	private boolean render = true;
	private boolean cross = false;
	private boolean translucent = false;
	private boolean solid = true;
	private int light = 0;
	protected String translationKey = "tile.missingno";
	private final String textureName;

	public int getU(int faceAxis, byte meta) {
		return this.u;
	}

	public int getV(int faceAxis, byte meta) {
		return this.v;
	}

	public void requestUV(Function<String, Vec2i> uvs) {
		Vec2i next = uvs.apply(this.textureName);
		this.u = next.getX();
		this.v = next.getY();
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

	protected Tile setName(String name) {
		this.translationKey = "tile." + name;
		return this;
	}

	protected Tile lightLevel(int light) {
		this.light = MathsUtils.clamp(light, 0, 0xF);
		return this;
	}

	@Nullable
	public Item getDrop(Random rand, byte meta) {
		return new Item(this, meta);
	}

	public final boolean isOpaque() {
		return this.isOpaque(false);
	}

	public int getLight() {
		return this.light;
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

	@Override
	public String toString() {
		return this.translationKey;
	}

	@Nullable
	public TileRenderer getCustomTileRenderer() {
		return null;
	}

	public static final Tile[] BY_ID = new Tile[256];
	public static final Tile AIR = new Tile("stone", 0, 0.0f).dontRender().noCollision();
	public static final Tile STONE = new Tile("stone", 1, 0.0f).setName("stone");
	public static final Tile GRASS = new GrassTile("grass", 2, 0.0f).setName("grass");
	public static final Tile LEAVES = new LeavesTile("leaves", 3, 0.0f).cutout().noCollision().setName("leaves");
	public static final Tile LOG = new ColumnTile("log", 4, 0.0f).setName("log");
	public static final Tile WATER = new WaterTile("water", 5, 0.0f).dontRender().noCollision();
	public static final Tile SAND = new Tile("sand", 6, 0.0f).setName("sand");
	public static final Tile DAISY = new PlantTile("daisy", 7, 0.0f, t -> t == GRASS).cross().noCollision().setName("daisy");
	public static final Tile CACTUS = new PlantTile("cactus", 8, 0.0f, t -> t == SAND).cross().noCollision().setName("cactus");
	public static final Tile TALLGRASS = new PlantTile("tallgrass", 9, 0.0f, t -> t == GRASS).cross().noCollision().setName("tallgrass");
	public static final Tile BRICKS = new Tile("bricks", 10, 0.0f).setName("bricks");
	public static final Tile STONE_BRICKS = new Tile("stone_bricks", 11, 0.0f).setName("stone_bricks");
	public static final Tile ICE = new IceTile("ice", 12, 0.0f).translucent().setName("ice");
	public static final Tile GALENA = new Tile("galena", 13, 0.0f).setName("galena");
	// TODO proper torch texture
	public static final Tile TORCH = new TorchTile("bricks", 14, 0.0f).setName("torch").cutout().noCollision().lightLevel(10);
}
