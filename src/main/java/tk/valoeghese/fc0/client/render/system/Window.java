package tk.valoeghese.fc0.client.render.system;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

import static org.lwjgl.glfw.GLFW.*;
import static tk.valoeghese.fc0.client.render.system.util.GLUtils.NULL;

public class Window implements GLFWWindowSizeCallbackI {
	public Window(int width, int height) {
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

		if(System.getProperty("os.name").toLowerCase().contains("mac")){
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		}

		this.glWindow = glfwCreateWindow(width,height,"2fc",NULL,NULL);

		if (this.glWindow == NULL) {
			throw new RuntimeException("Failed to greate GLFW window!");
		}

		glfwMakeContextCurrent(this.glWindow);
		glfwSwapInterval(1); // vsync
		GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(this.glWindow, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwShowWindow(this.glWindow);
		glfwSetWindowSizeCallback(this.glWindow, this);

		this.width = width;
		this.height = height;
		this.aspect = (float) width / (float) height;
	}

	public final long glWindow;
	public int width;
	public int height;
	public float aspect;

	public boolean isOpen() {
		return !glfwWindowShouldClose(this.glWindow);
	}

	public void swapBuffers() {
		glfwSwapBuffers(this.glWindow);
	}

	public void destroy() {
		glfwDestroyWindow(this.glWindow);
	}

	@Override
	public void invoke(long window, int width, int height) {
		this.width = width;
		this.height = height;
		this.aspect = (float) width / (float) height;
	}
}
