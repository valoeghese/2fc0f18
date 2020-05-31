package tk.valoeghese.fc0.client.system.util;

import tk.valoeghese.fc0.client.system.Window;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

public class GraphicsSystem {
	private GraphicsSystem() {
		// NO-OP
	}

	public static void initGLFW() {
		if (!glfwInit()) {
			throw new RuntimeException("Error initialising GLFW");
		}
	}

	public static void initGL(Window window) {
		createCapabilities();
		glViewport(0, 0, window.width, window.height);
		glfwSetFramebufferSizeCallback(window.glWindow, (w, width, height) -> glViewport(0, 0, width, height));
	}

	public static void bindTexture(int texture) {
		glBindTexture(GL_TEXTURE_2D, texture);
	}

	public static void enableBlend() {
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public static void disableBlend() {
		glDisable(GL_BLEND);
	}

	public static final int NULL = 0;
}
