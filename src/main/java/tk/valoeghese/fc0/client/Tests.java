package tk.valoeghese.fc0.client;

import tk.valoeghese.fc0.client.system.Model;
import tk.valoeghese.fc0.client.system.Window;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static tk.valoeghese.fc0.client.system.GraphicsSystem.NULL;

public class Tests {
	public static void runLegacyTest(Window window) {
		while (window.isOpen()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glBegin(GL_QUADS);
			glVertex2f(0.0f, 1.0f);
			glVertex2f(0.0f, 0.0f);
			glVertex2f(1.0f, 0.0f);
			glVertex2f(1.0f, 1.0f);
			glEnd();
			glFlush();
			window.swapBuffers();
			glfwPollEvents();
		}

		window.destroy();
	}

	public static void runModernTest(Window window) {
		float[] verts = new float[] {
				0.0f, 1.0f, // tl
				0.0f, 0.0f, // bl
				1.0f, 0.0f, // br
				1.0f, 1.0f}; // tr
		int[] indices = new int[] {0, 1, 2, 0, 2, 3};

		int vbo = glGenBuffers();
		int ibo = glGenBuffers();
		int vao = glGenVertexArrays();

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER,verts, GL_STATIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,ibo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		glVertexAttribPointer(0,2,GL_FLOAT,false,0,NULL);
		glEnableVertexAttribArray(0);
		glBindVertexArray(0);

		while (window.isOpen()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glBindVertexArray(vao);
			glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_INT,NULL);
			glBindVertexArray(0);
			window.swapBuffers();
			glfwPollEvents();
		}

		window.destroy();
	}

	public static void testModernSystem(Window window) {
		Model test = new TestModel();

		while (window.isOpen()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			test.render(null); // no shader so null is fine
			window.swapBuffers();
			glfwPollEvents();
		}

		window.destroy();
	}

	private static class TestModel extends Model {
		private TestModel() {
			super(GL_STATIC_DRAW, null);

			this.vertex(0.0f, 1.0f, 0.0f, 0.0f, 0.0f);
			this.vertex(0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
			this.vertex(1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
			this.vertex(1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
			this.tri(0, 1, 2);
			this.tri(0, 2, 3);
			this.generateBuffers();
		}
	}
}
