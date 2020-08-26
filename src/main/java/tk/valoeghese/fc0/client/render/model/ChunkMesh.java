package tk.valoeghese.fc0.client.render.model;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import tk.valoeghese.fc0.client.render.Shaders;
import tk.valoeghese.fc0.client.render.system.Camera;
import tk.valoeghese.fc0.client.world.ClientChunk;
import tk.valoeghese.fc0.client.world.RenderedChunk;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class ChunkMesh {
	public ChunkMesh(ClientChunk chunk, byte[] tiles, byte[] meta, int x, int z) {
		this.x = x << 4;
		this.z = z << 4;
		this.transform = new Matrix4f().translate(this.x, 0, this.z);
		this.tiles = tiles;
		this.meta = meta;
		this.chunk = chunk;
		this.buildMesh();
	}

	private final int x;
	private final int z;
	private final Matrix4f transform;
	private final byte[] tiles;
	private final byte[] meta;
	private final RenderedChunk chunk;
	private ChunkMeshModel solid;
	private ChunkMeshModel translucent;
	private ChunkMeshModel water;

	public void updateTile(int index, byte tile) {
		this.tiles[index] = tile;
		this.buildMesh();
	}

	public void buildMesh() {
		List<RenderedTileFace> faces = new ArrayList<>();
		List<RenderedTileFace> waterFaces = new ArrayList<>();
		List<RenderedTileFace> translucentFaces = new ArrayList<>();

		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 128; ++y) {
					if (this.chunk.renderHeight(y)) {
						int tile = this.tiles[index(x, y, z)];
						byte meta = this.meta[index(x, y, z)];
						Tile instance = Tile.BY_ID[tile];
						boolean waterLayer = instance == Tile.WATER;
						List<RenderedTileFace> layer = waterLayer ? waterFaces : (instance.isTranslucent() ? translucentFaces : faces);

						if (instance.shouldRender() && instance.isCross()) {
							layer.add(
									new RenderedCrossTileFace(new Vector3f(x, y, z),
											instance,
											0.95f,
											meta));
						} else if (instance.shouldRender() || waterLayer) {
							Tile tileUp = y == 127 ? Tile.AIR : Tile.BY_ID[this.tiles[index(x, y + 1, z)]];
							Tile tileDown = y == 0 ? Tile.AIR : Tile.BY_ID[this.tiles[index(x, y - 1, z)]];
							Tile tileWest = x == 0 ? this.chunk.west(z, y) : Tile.BY_ID[this.tiles[index(x - 1, y, z)]];
							Tile tileEast = x == 15 ? this.chunk.east(z, y) : Tile.BY_ID[this.tiles[index(x + 1, y, z)]];
							Tile tileSouth = z == 0 ? this.chunk.south(x, y) : Tile.BY_ID[this.tiles[index(x, y, z - 1)]];
							Tile tileNorth = z == 15 ? this.chunk.north(x, y) : Tile.BY_ID[this.tiles[index(x, y, z + 1)]];

							if (!tileUp.isOpaque(waterLayer)) {
								layer.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y + 1f, z + 0.5f),
										1,
										instance,
										0.95f * this.chunk.getRenderLightingFactor(x, y + 1, z),
										meta));
							}

							if (!tileDown.isOpaque(waterLayer)) {
								layer.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y, z + 0.5f),
										4,
										instance,
										0.85f * this.chunk.getRenderLightingFactor(x, y - 1, z),
										meta));
							}

							if (!tileNorth.isOpaque(waterLayer)) {
								layer.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y + 0.5f, z + 1f),
										2,
										instance,
										1.05f * this.chunk.getRenderLightingFactor(x, y, z + 1),
										meta));
							}

							if (!tileSouth.isOpaque(waterLayer)) {
								layer.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y + 0.5f, z),
										5,
										instance,
										0.75f * this.chunk.getRenderLightingFactor(x, y, z - 1),
										meta));
							}

							if (!tileEast.isOpaque(waterLayer)) {
								layer.add(new RenderedTileFace(
										new Vector3f(x + 1f, y + 0.5f, z + 0.5f),
										0,
										instance,
										0.9f * this.chunk.getRenderLightingFactor(x + 1, y, z),
										meta));
							}

							if (!tileWest.isOpaque(waterLayer)) {
								layer.add(new RenderedTileFace(
										new Vector3f(x, y + 0.5f, z + 0.5f),
										3,
										instance,
										0.9f * this.chunk.getRenderLightingFactor(x - 1, y, z),
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

	public void renderSolidTerrain(Camera camera){
		camera.render(this.solid, this.transform);
	}

	public void renderTranslucentTerrain(Camera camera) {
		camera.render(this.translucent, this.transform);
	}

	public void renderWater(Camera camera) {
		Shaders.terrain.uniformInt("waveMode", 1);
		camera.render(this.water, this.transform);
		Shaders.terrain.uniformInt("waveMode", 0);
	}

	private static int index(int x, int y, int z) { // @see Chunk.index
		return (x << 11) | (z << 7) | y;
	}

	static class RenderedCrossTileFace extends RenderedTileFace {
		RenderedCrossTileFace(Vector3f offset, Tile tile, float light, byte meta) {
			super(offset.sub(-0.5f, -0.5f, -0.5f), 1, tile, light, meta);
		}

		@Override
		public void addTo(ExternallyEditableModel model) {
			final float startU = (this.u / 16.0f);
			final float startV = (this.v / 16.0f);
			final float endU = startU + 0.0625f;
			final float endV = startV + 0.0625f;

			// square 1
			int i = model.addVertex(this.pos.x - SIZE, this.pos.y - SIZE, this.pos.z - SIZE, startU, startV, this.l);
			model.addVertex(this.pos.x - SIZE, this.pos.y + SIZE, this.pos.z - SIZE, startU, endV, this.l);
			model.addVertex(this.pos.x + SIZE, this.pos.y - SIZE, this.pos.z + SIZE, endU, startV, this.l);
			model.addVertex(this.pos.x + SIZE, this.pos.y + SIZE, this.pos.z + SIZE, endU, endV, this.l);

			model.addTriangle(i, i + 1, i + 3);
			model.addTriangle(i, i + 2, i + 3);

			i = model.addVertex(this.pos.x + SIZE, this.pos.y - SIZE, this.pos.z - SIZE, startU, startV, this.l);
			model.addVertex(this.pos.x + SIZE, this.pos.y + SIZE, this.pos.z - SIZE, startU, endV, this.l);
			model.addVertex(this.pos.x - SIZE, this.pos.y - SIZE, this.pos.z + SIZE, endU, startV, this.l);
			model.addVertex(this.pos.x - SIZE, this.pos.y + SIZE, this.pos.z + SIZE, endU, endV, this.l);

			model.addTriangle(i, i + 1, i + 3);
			model.addTriangle(i, i + 2, i + 3);
		}
	}

	static class RenderedTileFace {
		RenderedTileFace(Vector3f offset, int faceAxis, Tile tile, float light, byte meta) {
			this.pos = offset;
			this.u = tile.getU(faceAxis, meta);
			this.v = tile.getV(faceAxis, meta);
			this.f = faceAxis;
			this.l = light;
		}

		final Vector3f pos;
		int u;
		int v;
		private int f;
		float l;

		static final float SIZE = 0.5f;

		public void addTo(ExternallyEditableModel model) {
			final float startU = (this.u / 16.0f);
			final float startV = (this.v / 16.0f);
			final float endU = startU + 0.0625f;
			final float endV = startV + 0.0625f;

			int i;

			switch (this.f % 3) {
			case 0:
			default:
				i = model.addVertex(this.pos.x, SIZE + this.pos.y, -SIZE + this.pos.z, startU, endV, this.l); // tl
				model.addVertex(this.pos.x, -SIZE + this.pos.y, -SIZE + this.pos.z, startU, startV, this.l); // bl
				model.addVertex(this.pos.x, SIZE + this.pos.y, SIZE + this.pos.z, endU, endV, this.l); // tr
				model.addVertex(this.pos.x, -SIZE + this.pos.y, SIZE + this.pos.z, endU, startV, this.l); // br
				break;
			case 1:
				i = model.addVertex(-SIZE + this.pos.x, this.pos.y, SIZE + this.pos.z, startU, endV , this.l); // tl
				model.addVertex(-SIZE + this.pos.x, this.pos.y, -SIZE + this.pos.z, startU, startV, this.l); // bl
				model.addVertex(SIZE + this.pos.x, this.pos.y, SIZE + this.pos.z, endU, endV, this.l); // tr
				model.addVertex(SIZE + this.pos.x, this.pos.y, -SIZE + this.pos.z, endU, startV, this.l); // br
				break;
			case 2:
				i = model.addVertex(-SIZE + this.pos.x, SIZE + this.pos.y, this.pos.z, startU, endV, this.l); // tl
				model.addVertex(-SIZE + this.pos.x, -SIZE + this.pos.y, this.pos.z, startU, startV, this.l); // bl
				model.addVertex(SIZE + this.pos.x, SIZE + this.pos.y, this.pos.z, endU, endV, this.l); // tr
				model.addVertex(SIZE + this.pos.x, -SIZE + this.pos.y, this.pos.z, endU, startV, this.l); // br
				break;
			}

			model.addTriangle(i, i + 1, i + 3);
			model.addTriangle(i, i + 2, i + 3);
		}
	}

	public void destroy() {
		this.solid.destroy();
		this.water.destroy();
	}
}
