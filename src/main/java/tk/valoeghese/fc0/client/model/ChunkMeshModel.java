package tk.valoeghese.fc0.client.model;

import tk.valoeghese.fc0.client.system.Model;

import java.util.List;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

public final class ChunkMeshModel extends Model implements ExternallyEditableModel {
	public ChunkMeshModel(List<ChunkMesh.RenderedTileFace> tileFaces) {
		super(GL_DYNAMIC_DRAW, Shaders.terrain);

		for (ChunkMesh.RenderedTileFace face : tileFaces) {
			face.addTo(this);
		}

		this.generateBuffers();
	}

	@Override
	public void addTriangle(int i0, int i1, int i2) {
		this.tri(i0 , i1, i2);
	}

	@Override
	public int addVertex(float x, float y, float z, float u, float v) {
		return this.vertex(x, y, z, u, v);
	}
}
