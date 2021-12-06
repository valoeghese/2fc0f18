package tk.valoeghese.fc0.client.render.model;

import valoeghese.scalpel.scene.Model;
import valoeghese.scalpel.scene.VertexBufferBuilder;
import valoeghese.scalpel.scene.VertexFormat;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

public final class ChunkMeshModel extends Model {
	public ChunkMeshModel(List<ChunkMesh.RenderedTileFace> tileFaces) {
		super(GL_DYNAMIC_DRAW);

		this.setVertexFormat(TERRAIN_FORMAT);
		VertexBufferBuilder vertices = new VertexBufferBuilder();

		for (ChunkMesh.RenderedTileFace face : tileFaces) {
			face.addTo(this, vertices);
		}

		this.genVertexArrays(vertices.getBuffer().flip());
	}

	public void addTriangle(int i0, int i1, int i2) {
		this.tri(i0, i1, i2);
	}

	public static VertexFormat TERRAIN_FORMAT = new VertexFormat.Builder()
			.add(GL_FLOAT, 3) // pos
			.add(GL_FLOAT, 2) // UV
			.add(GL_INT, 1) // Lighting
			.build();
}
