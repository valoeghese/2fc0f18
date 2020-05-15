package tk.valoeghese.fc0.client.system;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

public final class GraphicsSystem {
	public static void initGLFW() {
		if (!glfwInit()) {
			throw new RuntimeException("Error initialising GLFW");
		}
	}

	public static void initGL(Window window) {
		createCapabilities();
		glShadeModel(GL_SMOOTH);
		glViewport(0, 0,window.width, window.height);
		glfwSetFramebufferSizeCallback(window.glWindow, (w, width, height) -> glViewport(0, 0, width, height));
	}

	public static void bindTexture(int texture) {
		glBindTexture(GL_TEXTURE_2D, texture);
	}

	public static final int NULL = 0;
}
