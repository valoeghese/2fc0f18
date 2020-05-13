package tk.valoeghese.fc0.client.model;

import javafx.scene.chart.Axis;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import tk.valoeghese.fc0.client.system.Camera;
import tk.valoeghese.fc0.world.Tile;

import java.util.ArrayList;
import java.util.List;

public class ChunkMesh {
	public ChunkMesh(byte[] tiles, int x, int z) {
		this.x = x << 4;
		this.z = z << 4;
		this.tiles = tiles;
		this.buildMesh();
	}

	private final int x;
	private final int z;
	private final byte[] tiles;
	private List<RenderedTileFace> mesh;

	public void updateTile(int index, byte tile) {
		this.tiles[index] = tile;
		this.buildMesh();
	}

	private void buildMesh() {
		this.mesh = new ArrayList<>();

		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 128; ++y) {
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
							AxisAngle4f angle = new AxisAngle4f((float) Math.toRadians(-90.0), 1.0f, 0.0f, 0.0f);
							this.mesh.add(new RenderedTileFace(
									new Vector3f(x, y + 0.5f, z).rotate(new Quaternionf(angle)),
									angle,
									instance,
									this.x,
									this.z));
						}

						if (!tileNorth.isOpaque()) {
							AxisAngle4f angle = new AxisAngle4f((float) Math.toRadians(-90.0), 0.0f, 1.0f, 0.0f);
							this.mesh.add(new RenderedTileFace(
									new Vector3f(x + 0.5f, y, z).rotate(new Quaternionf(angle)),
									angle,
									instance,
									this.x,
									this.z));
						}
					}
				}
			}
		}
	}

	public void render(Camera camera) {
		for (RenderedTileFace face : this.mesh) {
			camera.render(face.model, face.transform);
		}
	}

	private static int index(int x, int y, int z) { // @see Chunk.index
		return (x << 11) | (z << 7) | y;
	}

	private static class RenderedTileFace {
		RenderedTileFace(Vector3f offset, int faceAxis, Tile tile, float cxo, float czo) {
			this.transform = new Matrix4f()
					.translate(offset)
					.translate(new Vector3f(cxo, 0, czo));
			this.model = new TileFaceModel(tile.u, tile.v, faceAxis);
		}

		private final Matrix4f transform;
		private final TileFaceModel model;
	}
}
