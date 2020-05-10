package tk.valoeghese.fc0.client;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import tk.valoeghese.fc0.client.system.*;

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
		this.projection = new Matrix4f().perspective((float) Math.toRadians(45), this.window.aspect, 0.01f, 100.0f);
	}

	private Shader terrain;
	private Model model;
	private final Matrix4f projection;
	private Camera camera;

	@Override
	public void run() {
		init();

		while (this.window.isOpen()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			this.render();
			this.window.swapBuffers();
			glfwPollEvents();
		}

		this.window.destroy();
	}

	public void init() {
		this.terrain = new Shader("assets/shader/terrain_v.glsl", "assets/shader/terrain_f.glsl");
		this.terrain.bind();
		glEnable(GL_DEPTH_TEST);

		this.model = new CubeModel(GL_STATIC_DRAW, this.terrain);
		this.camera = new Camera();
		this.camera.translate(new Vector3f(0.0f, 0.0f, 1.0f));
	}

	public void render() {
		// bind shader
		this.terrain.bind();
		// projection
		this.terrain.uniformMat4f("projection", this.projection);
		// render
		Vector3f colour = new Vector3f(1.0f, 1.0f, 1.0f);
		this.terrain.uniformVec3f("colour", colour);
		GraphicsSystem.render(this.camera, this.model, new Matrix4f());
		// unbind shader
		//Shader.unbind();
	}

	private final Window window;
}
