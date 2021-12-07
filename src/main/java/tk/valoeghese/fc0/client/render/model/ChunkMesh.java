package tk.valoeghese.fc0.client.render.model;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import tk.valoeghese.fc0.client.render.Shaders;
import tk.valoeghese.fc0.client.render.tile.TileRenderer;
import tk.valoeghese.fc0.client.world.ClientChunk;
import tk.valoeghese.fc0.world.tile.Tile;
import valoeghese.scalpel.scene.VertexBufferBuilder;

import java.util.ArrayList;
import java.util.List;

public class ChunkMesh {
	public ChunkMesh(ClientChunk chunk, int x, int z) {
		this.x = x << 4;
		this.z = z << 4;
		this.transform = new Matrix4f().translate(this.x, 0, this.z);
		this.chunk = chunk;
	}

	private final int x;
	private final int z;
	private final Matrix4f transform;
	private final ClientChunk chunk;
	private ChunkMeshModel solid;
	private ChunkMeshModel translucent;
	private ChunkMeshModel water;

	public void buildMesh(byte[] tiles, byte[] metas) {
		List<RenderedTileFace> faces = new ArrayList<>();
		List<RenderedTileFace> waterFaces = new ArrayList<>();
		List<RenderedTileFace> translucentFaces = new ArrayList<>();

		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 128; ++y) {
					if (this.chunk.renderHeight(y)) {
						int tile = tiles[index(x, y, z)];
						byte meta = metas[index(x, y, z)];
						Tile instance = Tile.BY_ID[tile];
						boolean waterLayer = instance == Tile.WATER;
						List<RenderedTileFace> layer = waterLayer ? waterFaces : (instance.isTranslucent() ? translucentFaces : faces);

						TileRenderer custom = instance.getCustomTileRenderer();

						if (custom != null && instance.shouldRender()) {
							custom.addFaces(instance, layer, tiles, this.chunk, x, y, z, meta);
						} else if (instance.shouldRender() && instance.isCross()) {
							layer.add(
									new RenderedCrossTileFace(new Vector3f(x, y, z),
											instance,
											this.chunk.getPackedLightLevel(x, y, z),
											meta));
						} else if (instance.shouldRender() || waterLayer) {
							Tile tileUp = y == 127 ? Tile.AIR : Tile.BY_ID[tiles[index(x, y + 1, z)]];
							Tile tileDown = y == 0 ? Tile.AIR : Tile.BY_ID[tiles[index(x, y - 1, z)]];
							Tile tileNorth = x == 0 ? this.chunk.north(z, y) : Tile.BY_ID[tiles[index(x - 1, y, z)]];
							Tile tileSouth = x == 15 ? this.chunk.south(z, y) : Tile.BY_ID[tiles[index(x + 1, y, z)]];
							Tile tileEast = z == 0 ? this.chunk.east(x, y) : Tile.BY_ID[tiles[index(x, y, z - 1)]];
							Tile tileWest = z == 15 ? this.chunk.west(x, y) : Tile.BY_ID[tiles[index(x, y, z + 1)]];

							if (!tileUp.isOpaque(waterLayer, instance)) { // 0.95f
								layer.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y + 1f, z + 0.5f),
										1,
										instance,
										this.chunk.getPackedLightLevel(x, y + 1, z),
										meta));
							}

							if (!tileDown.isOpaque(waterLayer, instance)) { // 0.85f
								layer.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y, z + 0.5f),
										4,
										instance,
										this.chunk.getPackedLightLevel(x, y - 1, z),
										meta));
							}

							if (!tileWest.isOpaque(waterLayer, instance)) { // 1.05f
								layer.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y + 0.5f, z + 1f),
										5,
										instance,
										this.chunk.getPackedLightLevel(x, y, z + 1),
										meta));
							}

							if (!tileEast.isOpaque(waterLayer, instance)) { // 0.75f
								layer.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y + 0.5f, z),
										2,
										instance,
										this.chunk.getPackedLightLevel(x, y, z - 1),
										meta));
							}

							if (!tileSouth.isOpaque(waterLayer, instance)) { // 0.9f
								layer.add(new RenderedTileFace(
										new Vector3f(x + 1f, y + 0.5f, z + 0.5f),
										0,
										instance,
										this.chunk.getPackedLightLevel(x + 1, y, z),
										meta));
							}

							if (!tileNorth.isOpaque(waterLayer, instance)) { // 0.9f
								layer.add(new RenderedTileFace(
										new Vector3f(x, y + 0.5f, z + 0.5f),
										3,
										instance,
										this.chunk.getPackedLightLevel(x - 1, y, z),
										meta));
							}
						}
					}
				}
			}
		}

		if (this.solid != null) {
			this.solid.destroy();
			this.translucent.destroy();
			this.water.destroy();
		}

		this.solid = new ChunkMeshModel(faces);
		this.translucent = new ChunkMeshModel(translucentFaces);
		this.water = new ChunkMeshModel(waterFaces);
	}

	public void renderSolidTerrain() {
		Shaders.terrain.uniformMat4f("transform", this.transform);
		this.solid.render();
	}

	public void renderTranslucentTerrain() {
		Shaders.terrain.uniformMat4f("transform", this.transform);
		this.translucent.render();
	}

	public void renderWater() {
		Shaders.terrain.uniformMat4f("transform", this.transform);
		this.water.render();
	}

	private static int index(int x, int y, int z) { // @see Chunk.index
		return (x << 11) | (z << 7) | y;
	}

	static class RenderedCrossTileFace extends RenderedTileFace {
		RenderedCrossTileFace(Vector3f offset, Tile tile, int light, byte meta) {
			super(offset.sub(-0.5f, -0.5f, -0.5f), 1, tile, light, meta);
		}

		@Override
		public void addTo(ChunkMeshModel model, VertexBufferBuilder vertices) {
			final float startU = (this.u / 16.0f);
			final float startV = (this.v / 16.0f);
			final float endU = startU + 0.0625f;
			final float endV = startV + 0.0625f;

			// square 1
			int i = vertices.pos(this.pos.x - SIZE, this.pos.y - SIZE, this.pos.z - SIZE).uv(startU, startV).add(this.lighting).next();
			vertices.pos(this.pos.x - SIZE, this.pos.y + SIZE, this.pos.z - SIZE).uv(startU, endV).add(this.lighting).next();
			vertices.pos(this.pos.x + SIZE, this.pos.y - SIZE, this.pos.z + SIZE).uv(endU, startV).add(this.lighting).next();
			vertices.pos(this.pos.x + SIZE, this.pos.y + SIZE, this.pos.z + SIZE).uv(endU, endV).add(this.lighting).next();

			model.addTriangle(i, i + 1, i + 3);
			model.addTriangle(i, i + 3, i + 2);
			// double up -- should I just separate cross models and not cull for them instead?
			model.addTriangle(i, i + 3, i + 1);
			model.addTriangle(i, i + 2, i + 3);

			i = vertices.pos(this.pos.x + SIZE, this.pos.y - SIZE, this.pos.z - SIZE).uv(startU, startV).add(this.lighting).next();
			vertices.pos(this.pos.x + SIZE, this.pos.y + SIZE, this.pos.z - SIZE).uv(startU, endV).add(this.lighting).next();
			vertices.pos(this.pos.x - SIZE, this.pos.y - SIZE, this.pos.z + SIZE).uv(endU, startV).add(this.lighting).next();
			vertices.pos(this.pos.x - SIZE, this.pos.y + SIZE, this.pos.z + SIZE).uv(endU, endV).add(this.lighting).next();

			model.addTriangle(i, i + 1, i + 3);
			model.addTriangle(i, i + 3, i + 2);
			// double up
			model.addTriangle(i, i + 3, i + 1);
			model.addTriangle(i, i + 2, i + 3);
		}
	}

	public static class RenderedTileFace {
		public RenderedTileFace(Vector3f offset, int faceAxis, Tile tile, int light, byte meta) {
			this.pos = offset;
			this.u = tile.getU(faceAxis, meta);
			this.v = tile.getV(faceAxis, meta);
			this.f = faceAxis;
			this.lighting = light | faceAxis;
		}

		protected final Vector3f pos;
		protected int u;
		protected int v;
		protected int f;
		protected int lighting;

		public static final float SIZE = 0.5f;

		public void addTo(ChunkMeshModel model, VertexBufferBuilder vertices) {
			final float startU = (this.u / 16.0f);
			final float startV = (this.v / 16.0f);
			final float endU = startU + 0.0625f;
			final float endV = startV + 0.0625f;

			int i;

			switch (this.f % 3) {
			case 0:
			default:
				i = vertices.pos(this.pos.x, SIZE + this.pos.y, -SIZE + this.pos.z).uv(startU, endV).add(this.lighting).next(); // tl
				vertices.pos(this.pos.x, -SIZE + this.pos.y, -SIZE + this.pos.z).uv(startU, startV).add(this.lighting).next(); // bl
				vertices.pos(this.pos.x, SIZE + this.pos.y, SIZE + this.pos.z).uv(endU, endV).add(this.lighting).next(); // tr
				vertices.pos(this.pos.x, -SIZE + this.pos.y, SIZE + this.pos.z).uv(endU, startV).add(this.lighting).next(); // br
				break;
			case 1:
				i = vertices.pos(-SIZE + this.pos.x, this.pos.y, SIZE + this.pos.z).uv(startU, endV).add(this.lighting).next(); // tl
				vertices.pos(-SIZE + this.pos.x, this.pos.y, -SIZE + this.pos.z).uv(startU, startV).add(this.lighting).next(); // bl
				vertices.pos(SIZE + this.pos.x, this.pos.y, SIZE + this.pos.z).uv(endU, endV).add(this.lighting).next(); // tr
				vertices.pos(SIZE + this.pos.x, this.pos.y, -SIZE + this.pos.z).uv(endU, startV).add(this.lighting).next(); // br
				break;
			case 2:
				i = vertices.pos(-SIZE + this.pos.x, SIZE + this.pos.y, this.pos.z).uv(startU, endV).add(this.lighting).next(); // tl
				vertices.pos(-SIZE + this.pos.x, -SIZE + this.pos.y, this.pos.z).uv(startU, startV).add(this.lighting).next(); // bl
				vertices.pos(SIZE + this.pos.x, SIZE + this.pos.y, this.pos.z).uv(endU, endV).add(this.lighting).next(); // tr
				vertices.pos(SIZE + this.pos.x, -SIZE + this.pos.y, this.pos.z).uv(endU, startV).add(this.lighting).next(); // br
				break;
			}

			if (this.f < 3) {
				model.addTriangle(i, i + 3, i + 1);
				model.addTriangle(i, i + 2, i + 3);
			} else {
				model.addTriangle(i, i + 1, i + 3);
				model.addTriangle(i, i + 3, i + 2);
			}
		}
	}

	public void destroy() {
		this.solid.destroy();
		this.water.destroy();
		this.translucent.destroy();
	}
}
