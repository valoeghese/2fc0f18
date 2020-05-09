package tk.valoeghese.fc0.client.system;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static tk.valoeghese.fc0.client.system.GraphicsSystem.NULL;

public abstract class Model {
	protected Model(int mode) {
		this.mode = mode;
	}

	private FloatList vTemp = new FloatArrayList();
	private int vTempIndex = 0;
	private IntList iTemp = new IntArrayList();
	private float[] vertices;
	private int[] indices;
	private int vbo;
	private int ebo;
	protected int vao;
	private final int mode;
	private int attribIndex;
	private static int currentAttribindex = 0;

	protected int vertex(float x, float y, float z) {
		vTemp.add(x);
		vTemp.add(y);
		vTemp.add(z);
		return vTempIndex++;
	}

	protected void tri(int i0, int i1, int i2) {
		iTemp.add(i0);
		iTemp.add(i1);
		iTemp.add(i2);
	}

	protected void generateBuffers() {
		this.vertices = vTemp.toFloatArray();
		this.indices = iTemp.toIntArray();
		this.attribIndex = currentAttribindex++;

		this.vbo = glGenBuffers();
		this.ebo = glGenBuffers();
		this.vao = glGenVertexArrays();

		glBindVertexArray(this.vao);

		glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
		glBufferData(GL_ARRAY_BUFFER, this.vertices, this.mode);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indices, this.mode);

		glVertexAttribPointer(this.attribIndex, 3,GL_FLOAT,false,0,NULL);
		glEnableVertexAttribArray(this.attribIndex);
		glBindVertexArray(0);
	}

	public final void render() {
		this.bind();
		glDrawElements(GL_TRIANGLES, this.indices.length, GL_UNSIGNED_INT,NULL);
		Model.unbind();
	}

	public final void bind() {
		glBindVertexArray(this.vao);
	}

	public static final void unbind() {
		glBindVertexArray(0);
	}
}
