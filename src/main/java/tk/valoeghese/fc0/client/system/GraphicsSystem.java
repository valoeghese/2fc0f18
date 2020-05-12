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

	public static final int NULL = 0;
}
