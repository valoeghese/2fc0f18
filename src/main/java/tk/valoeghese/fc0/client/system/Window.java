package tk.valoeghese.fc0.client.system;

import static org.lwjgl.glfw.GLFW.*;
import static tk.valoeghese.fc0.client.system.GraphicsSystem.NULL;

public class Window {
	public Window(int width, int height) {
		this.glWindow = glfwCreateWindow(width,height,"2fc",NULL,NULL);

		if (this.glWindow == NULL) {
			throw new RuntimeException("Failed to greate GLFW window!");
		}

		glfwMakeContextCurrent(this.glWindow);
		glfwSwapInterval(1); // vsync
		glfwShowWindow(this.glWindow);

		this.width = width;
		this.height = height;
	}

	private final long glWindow;
	public final int width;
	public final int height;

	public boolean isOpen() {
		return !glfwWindowShouldClose(this.glWindow);
	}

	public void swapBuffers() {
		glfwSwapBuffers(this.glWindow);
	}

	public void destroy() {
		glfwDestroyWindow(this.glWindow);
	}
}
