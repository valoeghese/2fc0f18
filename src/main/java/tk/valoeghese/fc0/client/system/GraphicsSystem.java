package tk.valoeghese.fc0.client.system;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

public final class GraphicsSystem {
	public static void initGLFW() {
		if (!glfwInit()) {
			throw new RuntimeException("Error initialising GLFW");
		}
	}

	public static void initGL() {
		createCapabilities();
		glClearColor(0, 0, 0, 255);
		glShadeModel(GL_FLAT);
	}

	private static final float[] cv = {
			-0.5f, -0.5f, -0.5f,
			0.5f, -0.5f, -0.5f,
			0.5f, -0.5f, 0.5f,
			-0.5f, -0.5f, 0.5f,
			-0.5f, 0.5f, -0.5f,
			0.5f, 0.5f, -0.5f,
			0.5f, 0.5f, 0.5f,
			-0.5f, 0.5f, 0.5f,
	};

	private static void cubeVertex(int index) {
		//index *= 3;
		//glVertex3f(cv[index], cv[index + 1], cv[index + 2]);
	}

	public static void renderCube() {
		// bottom
		cubeVertex(0);
		cubeVertex(3);
		cubeVertex(2);
		cubeVertex(1);
		// top
		cubeVertex(4);
		cubeVertex(5);
		cubeVertex(6);
		cubeVertex(7);
		// left
		cubeVertex(7);
		cubeVertex(3);
		cubeVertex(0);
		cubeVertex(4);
		// right
		cubeVertex(5);
		cubeVertex(1);
		cubeVertex(2);
		cubeVertex(6);
		// front
		cubeVertex(4);
		cubeVertex(0);
		cubeVertex(1);
		cubeVertex(5);
		// back
		cubeVertex(6);
		cubeVertex(2);
		cubeVertex(3);
		cubeVertex(7);
	}

	public static final int NULL = 0;
}
