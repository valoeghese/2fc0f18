package tk.valoeghese.fc0.client;

import tk.valoeghese.fc0.client.system.GraphicsSystem;
import tk.valoeghese.fc0.client.system.Shader;
import tk.valoeghese.fc0.client.system.Window;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static tk.valoeghese.fc0.client.system.GraphicsSystem.NULL;

public class Client2fc implements Runnable {
	public Client2fc() {
		GraphicsSystem.initGLFW();
		this.window = new Window(600, 400);
		GraphicsSystem.initGL();
	}

	private Shader terrain;

	@Override
	public void run() {
		Tests.testModernSystem(this.window);
		/*init();

		while (this.window.isOpen()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			this.render();
			this.window.swapBuffers();
			glfwPollEvents();
		}

		this.window.destroy();*/
	}

	public void init() {
		this.terrain = new Shader("assets/shader/terrain_v.glsl", "assets/shader/terrain_f.glsl");
	}

	public void render() {

	}

	private final Window window;
}
