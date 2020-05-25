package tk.valoeghese.fc0.client.system;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static tk.valoeghese.fc0.client.system.GraphicsSystem.NULL;

public abstract class Model {
	protected Model(int mode, @Nullable Shader shader) {
		this.mode = mode;
		this.shader = shader;
	}

	private FloatList vTemp = new FloatArrayList();
	private int vTempIndex = 0;
	private IntList iTemp = new IntArrayList();
	private List<VertexArray> vertexArrays = new ArrayList<>();
	private final int mode;
	@Nullable
	private final Shader shader;

	protected int vertex(float x, float y, float z, float u, float v) {
		return this.vertex(x, y, z, u, v, 1.0f);
	}

	protected int vertex(float x, float y, float z, float u, float v, float light) {
		this.vTemp.add(x);
		this.vTemp.add(y);
		this.vTemp.add(z);
		this.vTemp.add(u);
		this.vTemp.add(v);
		this.vTemp.add(light);
		return this.vTempIndex++;
	}

	protected void tri(int i0, int i1, int i2) {
		this.iTemp.add(i0);
		this.iTemp.add(i1);
		this.iTemp.add(i2);
	}

	protected void generateBuffers() {
		float[] vertices = this.vTemp.toFloatArray();
		int[] indices = this.iTemp.toIntArray();
		this.vTemp = new FloatArrayList();
		this.iTemp = new IntArrayList();

		int vbo = glGenBuffers();
		int ebo = glGenBuffers();
		int vao = glGenVertexArrays();

		glBindVertexArray(vao);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertices, this.mode);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, this.mode);

		glVertexAttribPointer(0, 3, GL_FLOAT, false,4 * 6, 4 * 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 6, 4 * 3);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(2, 1, GL_FLOAT, false, 4 * 6, 4 * 5);
		glEnableVertexAttribArray(2);
		glBindVertexArray(0);

		this.vertexArrays.add(new VertexArray(vao, indices.length));
	}

	public void destroy() {
		for (VertexArray array : this.vertexArrays) {
			glDeleteVertexArrays(array.vao);
		}

		this.vertexArrays = new ArrayList<>();
		this.vTempIndex = 0;
	}

	public final void render(Matrix4f transform) {
		if (this.shader != null) {
			this.shader.uniformMat4f("transform", transform);
		}

		for (VertexArray array : this.vertexArrays) {
			glBindVertexArray(array.vao);
			glDrawElements(GL_TRIANGLES, array.elementCount, GL_UNSIGNED_INT, NULL);
		}

		unbind();
	}

	@Nullable
	public Shader getShader() {
		return this.shader;
	}

	public static final void unbind() {
		glBindVertexArray(0);
	}

	private static class VertexArray {
		private VertexArray(int vao, int elementCount) {
			this.vao = vao;
			this.elementCount = elementCount;
		}

		private final int vao;
		private final int elementCount;
	}
}
