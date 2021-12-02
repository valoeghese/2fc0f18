package tk.valoeghese.fc0.client.render.tile;

import org.joml.Vector3f;
import tk.valoeghese.fc0.client.render.model.ChunkMesh;
import tk.valoeghese.fc0.client.render.model.ChunkMeshModel;
import tk.valoeghese.fc0.client.world.ClientChunk;
import tk.valoeghese.fc0.world.tile.Tile;
import valoeghese.scalpel.scene.VertexBufferBuilder;

import java.util.List;

public enum TorchRenderer implements TileRenderer {
	INSTANCE;

	@Override
	public void addFaces(Tile instance, List<ChunkMesh.RenderedTileFace> layer, byte[] tiles, ClientChunk chunk, int x, int y, int z, byte meta) {
		Tile tileUp = y == 127 ? Tile.AIR : Tile.BY_ID[tiles[TileRenderer.index(x, y + 1, z)]];
		Tile tileDown = y == 0 ? Tile.AIR : Tile.BY_ID[tiles[TileRenderer.index(x, y - 1, z)]];
		float lf = chunk.getRenderLightingFactor(x, y, z);

		if (!tileUp.isOpaque()) {
			layer.add(new Face(
					new Vector3f(x, y + 0.5f, z),
					1,
					instance,
					0.95f * chunk.getRenderLightingFactor(x, y + 1, z),
					meta));
		}

		if (!tileDown.isOpaque()) {
			layer.add(new Face(
					new Vector3f(x, y - 0.5f, z),
					4,
					instance,
					0.85f * chunk.getRenderLightingFactor(x, y - 1, z),
					meta));
		}

		layer.add(new Face(
				new Vector3f(x, y, z + 0.5f * 0.25f),
				2,
				instance,
				1.05f * lf,
				meta));

		layer.add(new Face(
				new Vector3f(x, y, z - 0.5f * 0.25f),
				5,
				instance,
				0.75f * lf,
				meta));

		layer.add(new Face(
				new Vector3f(x + 0.5f * 0.25f, y, z),
				0,
				instance,
				0.9f * lf,
				meta));

		layer.add(new Face(
				new Vector3f(x - 0.5f * 0.25f, y, z),
				3,
				instance,
				0.9f * lf,
				meta));
	}

	static class Face extends ChunkMesh.RenderedTileFace {
		Face(Vector3f offset, int faceAxis, Tile tile, float light, byte meta) {
			super(offset.sub(-0.5f, -0.5f, -0.5f), faceAxis, tile, light, meta);
		}

		@Override
		public void addTo(ChunkMeshModel model, VertexBufferBuilder vertices) {
			final float startU = (this.u / 16.0f);
			final float startV = (this.v / 16.0f);
//			final float endU = startU + 0.0625f;
			final float endV = startV + 0.0625f;

			final float horizontalSize = SIZE * 0.25f;
			final float hendU = startU + 0.0625f * 0.125f;
			final float hendV = startV + 0.0625f * 0.125f;

			int i;

			switch (this.f % 3) {
			case 0:
			default:
				i = vertices.pos(this.pos.x, SIZE + this.pos.y, -horizontalSize + this.pos.z).uv(startU, endV).add(this.lighting).next(); // tl
				vertices.pos(this.pos.x, -SIZE + this.pos.y, -horizontalSize + this.pos.z).uv(startU, startV).add(this.lighting).next(); // bl
				vertices.pos(this.pos.x, SIZE + this.pos.y, horizontalSize + this.pos.z).uv(hendU, endV).add(this.lighting).next(); // tr
				vertices.pos(this.pos.x, -SIZE + this.pos.y, horizontalSize + this.pos.z).uv(hendU, startV).add(this.lighting).next(); // br
				break;
			case 1:
				i = vertices.pos(-horizontalSize + this.pos.x, this.pos.y, horizontalSize + this.pos.z).uv(startU, hendV ).add(this.lighting).next(); // tl
				vertices.pos(-horizontalSize + this.pos.x, this.pos.y, -horizontalSize + this.pos.z).uv(startU, startV).add(this.lighting).next(); // bl
				vertices.pos(horizontalSize + this.pos.x, this.pos.y, horizontalSize + this.pos.z).uv(hendU, hendV).add(this.lighting).next(); // tr
				vertices.pos(horizontalSize + this.pos.x, this.pos.y, -horizontalSize + this.pos.z).uv(hendU, startV).add(this.lighting).next(); // br
				break;
			case 2:
				i = vertices.pos(-horizontalSize + this.pos.x, SIZE + this.pos.y, this.pos.z).uv(startU, endV).add(this.lighting).next(); // tl
				vertices.pos(-horizontalSize + this.pos.x, -SIZE + this.pos.y, this.pos.z).uv(startU, startV).add(this.lighting).next(); // bl
				vertices.pos(horizontalSize + this.pos.x, SIZE + this.pos.y, this.pos.z).uv(hendU, endV).add(this.lighting).next(); // tr
				vertices.pos(horizontalSize + this.pos.x, -SIZE + this.pos.y, this.pos.z).uv(hendU, startV).add(this.lighting).next(); // br
				break;
			}

			model.addTriangle(i, i + 1, i + 3);
			model.addTriangle(i, i + 2, i + 3);
		}
	}
}
