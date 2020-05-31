package tk.valoeghese.fc0.client.system.gui;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import tk.valoeghese.fc0.client.model.Shaders;
import tk.valoeghese.fc0.client.system.Shader;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static tk.valoeghese.fc0.client.system.util.GraphicsSystem.NULL;

public abstract class GUI implements PseudoGUI {
	protected GUI(int texture) {
		this.mode = GL_STATIC_DRAW;
		this.shader = Shaders.gui;
		this.texture = texture;
	}

	private FloatList vTemp = new FloatArrayList();
	private int vTempIndex = 0;
	private IntList iTemp = new IntArrayList();
	protected List<VertexArray> vertexArrays = new ArrayList<>();
	private final int mode;
	private final Shader shader;
	private final int texture;

	protected int vertex(float x, float y, float u, float v) {
		return this.vertex(x, y, 0.999f, u, v);
	}

	protected int vertex(float x, float y, float z, float u, float v) {
		this.vTemp.add(x);
		this.vTemp.add(y);
		this.vTemp.add(z);
		this.vTemp.add(u);
		this.vTemp.add(v);
		return this.vTempIndex++;
	}

	protected void tri(int i0, int i1, int i2) {
		this.iTemp.add(i0);
		this.iTemp.add(i1);
		this.iTemp.add(i2);
	}

	@Override
	public void destroy() {
		for (GUI.VertexArray array : this.vertexArrays) {
			glDeleteVertexArrays(array.vao);
		}

		this.vertexArrays.clear();
		this.vTempIndex = 0;
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

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 4 * 5, 4 * 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 5, 4 * 3);
		glEnableVertexAttribArray(1);
		glBindVertexArray(0);

		this.vertexArrays.add(new VertexArray(vao, indices.length));
	}

	@Override
	public final void render() {
		glBindTexture(GL_TEXTURE_2D, texture);

		for (VertexArray array : this.vertexArrays) {
			glBindVertexArray(array.vao);
			glDrawElements(GL_TRIANGLES, array.elementCount, GL_UNSIGNED_INT, NULL);
		}

		unbind();
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public Shader getShader() {
		return this.shader;
	}

	public static final void unbind() {
		glBindVertexArray(0);
	}

	protected static class VertexArray {
		private VertexArray(int vao, int elementCount) {
			this.vao = vao;
			this.elementCount = elementCount;
		}

		private final int vao;
		private final int elementCount;

		@Override
		public String toString() {
			return "{vao: " + this.vao + ", elementCount: " + this.elementCount + "}";
		}
	}
}
