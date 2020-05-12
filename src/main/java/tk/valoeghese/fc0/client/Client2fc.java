package tk.valoeghese.fc0.client;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import tk.valoeghese.fc0.client.keybind.KeybindManager;
import tk.valoeghese.fc0.client.keybind.MousebindManager;
import tk.valoeghese.fc0.client.system.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public class Client2fc implements Runnable {
	public Client2fc() {
		GraphicsSystem.initGLFW();
		this.window = new Window(640, 360);
		GraphicsSystem.initGL(this.window);
		this.projection = new Matrix4f().perspective((float) Math.toRadians(45), this.window.aspect, 0.01f, 100.0f);
		glfwSetKeyCallback(this.window.glWindow, KeybindManager.INSTANCE);
		glfwSetMouseButtonCallback(this.window.glWindow, MousebindManager.INSTANCE);
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

			if (Keybinds.ESCAPE.hasBeenPressed()) {
				glfwSetWindowShouldClose(this.window.glWindow, true);
			}
		}

		this.window.destroy();//*/
	}

	public void init() {
		this.terrain = new Shader("assets/shader/terrain_v.glsl", "assets/shader/terrain_f.glsl");
		this.terrain.bind();
		glEnable(GL_DEPTH_TEST);

		this.model = new PlaneModel(GL_STATIC_DRAW, this.terrain);
		this.camera = new Camera();
		this.camera.translate(new Vector3f(0.0f, 0.0f, -1.5f));
	}

	public void render() {
		if (Keybinds.MOVE_BACKWARDS.isPressed()) {
			this.camera.translate(new Vector3f(0.0f, 0.0f, -0.1f));
		} else if (Keybinds.MOVE_FOWARDS.isPressed()) {
			this.camera.translate(new Vector3f(0.0f, 0.0f, 0.1f));
		}

		if (Keybinds.MOVE_LEFT.isPressed()) {
			this.camera.translate(new Vector3f(0.1f, 0.0f, 0.0f));
		} else if (Keybinds.MOVE_RIGHT.isPressed()) {
			this.camera.translate(new Vector3f(-0.1f, 0.0f, 0.0f));
		}
		// bind shader
		this.terrain.bind();
		// projection
		this.terrain.uniformMat4f("projection", this.projection);
		// render
		this.camera.render(this.model, new Matrix4f());
		// unbind shader
		Shader.unbind();
	}

	private final Window window;
}
