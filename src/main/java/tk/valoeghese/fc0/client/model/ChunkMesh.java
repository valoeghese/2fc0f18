package tk.valoeghese.fc0.client.model;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import tk.valoeghese.fc0.client.system.Camera;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.Tile;

import java.util.ArrayList;
import java.util.List;

public class ChunkMesh {
	public ChunkMesh(Chunk chunk, byte[] tiles, int x, int z) {
		this.x = x << 4;
		this.z = z << 4;
		this.transform = new Matrix4f().translate(this.x, 0, this.z);
		this.tiles = tiles;
		this.chunk = chunk;
		this.buildMesh();
	}

	private final int x;
	private final int z;
	private final Matrix4f transform;
	private final byte[] tiles;
	private final Chunk chunk;
	private ChunkMeshModel mesh;

	public void updateTile(int index, byte tile) {
		this.tiles[index] = tile;
		this.buildMesh();
	}

	private void buildMesh() {
		List<RenderedTileFace> faces = new ArrayList<>();

		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 128; ++y) {
					if (this.chunk.renderHeight(y)) {
						int tile = this.tiles[index(x, y, z)];
						Tile instance = Tile.BY_ID[tile];

						if (instance.shouldRender()) {
							Tile tileUp = y == 127 ? Tile.AIR : Tile.BY_ID[this.tiles[index(x, y + 1, z)]];
							Tile tileDown = y == 0 ? Tile.AIR : Tile.BY_ID[this.tiles[index(x, y - 1, z)]];
							Tile tileWest = x == 0 ? Tile.AIR : Tile.BY_ID[this.tiles[index(x - 1, y, z)]];
							Tile tileEast = x == 15 ? Tile.AIR : Tile.BY_ID[this.tiles[index(x + 1, y, z)]];
							Tile tileSouth = z == 0 ? Tile.AIR : Tile.BY_ID[this.tiles[index(x, y, z - 1)]];
							Tile tileNorth = z == 15 ? Tile.AIR : Tile.BY_ID[this.tiles[index(x, y, z + 1)]];

							if (!tileUp.isOpaque()) {
								faces.add(new RenderedTileFace(
										new Vector3f(x, y + 0.5f, z),
										1,
										instance,
										0.95f));
							}

							if (!tileDown.isOpaque()) {
								faces.add(new RenderedTileFace(
										new Vector3f(x, y - 0.5f, z),
										1,
										instance,
										0.85f));
							}

							if (!tileNorth.isOpaque()) {
								faces.add(new RenderedTileFace(
										new Vector3f(x, y, z + 0.5f),
										2,
										instance,
										1.05f));
							}

							if (!tileSouth.isOpaque()) {
								faces.add(new RenderedTileFace(
										new Vector3f(x, y, z - 0.5f),
										2,
										instance,
										0.75f));
							}

							if (!tileEast.isOpaque()) {
								faces.add(new RenderedTileFace(
										new Vector3f(x + 0.5f, y, z),
										0,
										instance,
										0.9f));
							}

							if (!tileWest.isOpaque()) {
								faces.add(new RenderedTileFace(
										new Vector3f(x - 0.5f, y, z),
										0,
										instance,
										0.9f));
							}
						}
					}
				}
			}
		}

		this.mesh = new ChunkMeshModel(faces);
	}

	public void render(Camera camera) {
		camera.render(this.mesh, this.transform);
	}

	private static int index(int x, int y, int z) { // @see Chunk.index
		return (x << 11) | (z << 7) | y;
	}

	static class RenderedTileFace {
		RenderedTileFace(Vector3f offset, int faceAxis, Tile tile, float light) {
			this.pos = offset;
			this.u = tile.u;
			this.v = tile.v;
			this.f = faceAxis;
			this.l = light;
		}

		private final Vector3f pos;
		private int u;
		private int v;
		private int f;
		private float l;

		static final float SIZE = 0.5f;

		public void addTo(ExternallyEditableModel model) {
			final float startU = (this.u / 16.0f);
			final float startV = (this.v / 16.0f);
			final float endU = startU + 0.0625f;
			final float endV = startV + 0.0625f;

			int i;

			switch (this.f) {
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
					i = model.addVertex(-SIZE + this.pos.x, SIZE + this.pos.y, this.pos.z, startU, endV, l); // tl
					model.addVertex(-SIZE + this.pos.x, -SIZE + this.pos.y, this.pos.z, startU, startV, l); // bl
					model.addVertex(SIZE + this.pos.x, SIZE + this.pos.y, this.pos.z, endU, endV, l); // tr
					model.addVertex(SIZE + this.pos.x, -SIZE + this.pos.y, this.pos.z, endU, startV, l); // br
					break;
			}

			model.addTriangle(i, i + 1, i + 3);
			model.addTriangle(i, i + 2, i + 3);
		}
	}
}
