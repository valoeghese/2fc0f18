package tk.valoeghese.fc0.client.system;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static tk.valoeghese.fc0.client.system.GraphicsSystem.NULL;

public abstract class Model {
	protected Model(int mode, @Nullable Shader shader) {
		this.mode = mode;
		this.shader = shader;
	}

	private FloatList vTemp = new FloatArrayList();
	private int vTempIndex = 0;
	private IntList iTemp = new IntArrayList();
	private List<VertexArray> vaos = new ArrayList<>();
	private final int mode;
	@Nullable
	private final Shader shader;

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

	protected void generateBuffers(int texture) {
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

		glVertexAttribPointer(0, 3, GL_FLOAT, false,4 * 5, 4 * 0);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 5, 4 * 3);
		glEnableVertexAttribArray(1);
		glBindVertexArray(0);

		this.vaos.add(new VertexArray(vao, indices.length, texture));
	}

	public final void render(Matrix4f transform) {
		if (this.shader != null) {
			this.shader.uniformMat4f("transform", transform);
		}

		for (VertexArray array : this.vaos) {
			glBindVertexArray(array.vao);
			glBindTexture(GL_TEXTURE_2D, array.texture);
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
		private VertexArray(int vao, int elementCount, int texture) {
			this.vao = vao;
			this.elementCount = elementCount;
			this.texture = texture;
		}

		private final int vao;
		private final int elementCount;
		private final int texture;
	}
}
