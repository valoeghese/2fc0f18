package tk.valoeghese.fc0.client;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import tk.valoeghese.fc0.client.keybind.KeybindManager;
import tk.valoeghese.fc0.client.keybind.MousebindManager;
import tk.valoeghese.fc0.client.model.Shaders;
import tk.valoeghese.fc0.client.model.TileFaceModel;
import tk.valoeghese.fc0.client.system.*;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.WorldGen;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Client2fc implements Runnable, GLFWCursorPosCallbackI {
	public Client2fc() {
		GraphicsSystem.initGLFW();
		this.window = new Window(640, 360);
		GraphicsSystem.initGL(this.window);
		this.projection = new Matrix4f().perspective((float) Math.toRadians(45), this.window.aspect, 0.01f, 100.0f);
		glfwSetKeyCallback(this.window.glWindow, KeybindManager.INSTANCE);
		glfwSetMouseButtonCallback(this.window.glWindow, MousebindManager.INSTANCE);
		Shaders.loadShaders();
		this.model = new TileFaceModel(0, 1, 0);
		this.chunk = WorldGen.generateChunk(0, 0);
	}

	private Model model;
	private Chunk chunk;
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

		this.window.destroy();
	}

	public void init() {
		glEnable(GL_DEPTH_TEST);
		glfwSetInputMode(this.window.glWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		glfwSetCursorPosCallback(this.window.glWindow, this);

		this.camera = new Camera();
		this.camera.translateScene(new Vector3f(0.0f, -60.0f, -1.5f));
		this.prevYPos = this.window.height / 2;
		this.prevXPos = this.window.width / 2;
	}

	public void render() {
		long time = System.nanoTime();

		if (Keybinds.MOVE_BACKWARDS.isPressed()) {
			this.camera.translateScene(new Vector3f(0.0f, 0.0f, -0.1f));
		} else if (Keybinds.MOVE_FOWARDS.isPressed()) {
			this.camera.translateScene(new Vector3f(0.0f, 0.0f, 0.1f));
		}

		if (Keybinds.MOVE_LEFT.isPressed()) {
			this.camera.translateScene(new Vector3f(0.1f, 0.0f, 0.0f));
		} else if (Keybinds.MOVE_RIGHT.isPressed()) {
			this.camera.translateScene(new Vector3f(-0.1f, 0.0f, 0.0f));
		}

		if (Keybinds.JUMP.isPressed()) {
			this.camera.translateScene(new Vector3f(0.0f, -0.1f, 0.0f));
		} else if (Keybinds.FLY_DOWN.isPressed()) {
			this.camera.translateScene(new Vector3f(0.0f, 0.1f, 0.0f));
		}
		// bind shader
		Shaders.terrain.bind();
		// projection
		Shaders.terrain.uniformMat4f("projection", this.projection);
		// render
		//this.camera.render(this.model, new Matrix4f());
		this.chunk.getOrCreateMesh().render(this.camera);
		this.camera.render(model, new Matrix4f());
		// unbind shader
		Shader.unbind();
		long elapsed = (System.nanoTime() - time) / 1000000;

		if (elapsed > 80) {
			System.out.println("[Render/Warn] Unusually long time! Rendering took: " + elapsed + "ms");
		}
	}

	private final Window window;

	private double prevYPos = 0;
	private double prevXPos = 0;

	@Override
	public void invoke(long window, double xpos, double ypos) {
		this.camera.rotateYaw((float) (xpos - this.prevXPos) / 80.0f);
		this.camera.rotatePitch((float) (ypos - this.prevYPos) / 40.0f);
		this.prevYPos = ypos;
		this.prevXPos = xpos;
	}
}
