package tk.valoeghese.fc0.client.render.model;

import valoeghese.scalpel.Shader;
import valoeghese.scalpel.scene.Model;
import valoeghese.scalpel.scene.VertexBufferBuilder;

import javax.annotation.Nullable;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class SquareModel extends Model {
	public SquareModel() {
		this(0.0f, 0.0f, 1.0f, 1.0f);
	}

	protected SquareModel(float startU, float startV, float endU, float endV) {
		super(GL_STATIC_DRAW);

		this.setVertexFormat(ChunkMeshModel.TERRAIN_FORMAT);

		VertexBufferBuilder vertices = new VertexBufferBuilder();
		int tl = vertices.pos(-1.0f, 1.0f, 0.0f).uv(startU, endV).add(0).next();
		int bl = vertices.pos(-1.0f, -1.0f, 0.0f).uv(startU, startV).add(0).next();
		int tr = vertices.pos(1.0f, 1.0f, 0.0f).uv(endU, endV).add(0).next();
		int br = vertices.pos(1.0f, -1.0f, 0.0f).uv(endU, startV).add(0).next();

		this.tri(tl, bl, br);
		this.tri(tl, tr, br);

		this.genVertexArrays(vertices.getBuffer().flip());
	}
}
