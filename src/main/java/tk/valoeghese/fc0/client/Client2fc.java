package tk.valoeghese.fc0.client;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import tk.valoeghese.fc0.client.gui.Crosshair;
import tk.valoeghese.fc0.client.gui.GUI;
import tk.valoeghese.fc0.client.keybind.KeybindManager;
import tk.valoeghese.fc0.client.keybind.MousebindManager;
import tk.valoeghese.fc0.client.model.Shaders;
import tk.valoeghese.fc0.client.system.Camera;
import tk.valoeghese.fc0.client.system.GraphicsSystem;
import tk.valoeghese.fc0.client.system.Shader;
import tk.valoeghese.fc0.client.system.Window;
import tk.valoeghese.fc0.util.Pos;
import tk.valoeghese.fc0.util.RaycastResult;
import tk.valoeghese.fc0.util.TilePos;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkSelection;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static tk.valoeghese.fc0.client.model.Textures.TILE_ATLAS;

public class Client2fc implements Runnable, GLFWCursorPosCallbackI {
	public Client2fc() {
		GraphicsSystem.initGLFW();
		this.window = new Window(640 * 2, 360 * 2);
		GraphicsSystem.initGL(this.window);
		this.projection = new Matrix4f().perspective((float) Math.toRadians(45), this.window.aspect, 0.01f, 250.0f);
		this.guiProjection = new Matrix4f().ortho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
		glfwSetKeyCallback(this.window.glWindow, KeybindManager.INSTANCE);
		glfwSetMouseButtonCallback(this.window.glWindow, MousebindManager.INSTANCE);
		Shaders.loadShaders();
		this.world = new ChunkSelection(new Random().nextLong());
	}

	private ChunkSelection world;
	private final Matrix4f projection;
	private final Matrix4f guiProjection;
	private ClientPlayer player;
	private long nextUpdate = 0;
	private GUI crosshair;

	@Override
	public void run() {
		init();

		while (this.window.isOpen()) {
			long timeMillis = System.currentTimeMillis();

			if (timeMillis >= this.nextUpdate) {
				this.nextUpdate = timeMillis + TICK_DELTA;
				this.tick();
			}

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

	private void tick() {
		this.updateMovement();
		this.player.tick();
	}

	private void init() {
		glEnable(GL_DEPTH_TEST);
		glClearColor(0.3f, 0.5f, 0.9f, 1.0f);
		glfwSetInputMode(this.window.glWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		glfwSetCursorPosCallback(this.window.glWindow, this);

		this.player = new ClientPlayer(new Camera(), this.world);
		this.prevYPos = this.window.height / 2;
		this.prevXPos = this.window.width / 2;
		this.crosshair = new Crosshair();
		this.world.populateChunks();
	}

	private void updateMovement() {
		final float yaw = this.player.getCamera().getYaw();
		float slowness = this.player.getHorizontalSlowness();

		if (Keybinds.RUN.isPressed()) {
			slowness /= 1.67;
		}

		if (Keybinds.MOVE_BACKWARDS.isPressed()) {
			this.player.addVelocity(-sin(yaw) / slowness, 0.0f, cos(yaw) / slowness);
		} else if (Keybinds.MOVE_FOWARDS.isPressed()) {
			this.player.addVelocity(-sin(yaw - PI) / slowness, 0.0f, cos(yaw - PI) / slowness);
		}

		if (Keybinds.MOVE_LEFT.isPressed()) {
			this.player.addVelocity(-sin(yaw + HALF_PI) / slowness, 0.0f, cos(yaw + HALF_PI) / slowness);
		} else if (Keybinds.MOVE_RIGHT.isPressed()) {
			this.player.addVelocity(-sin(yaw - HALF_PI) / slowness, 0.0f, cos(yaw - HALF_PI) / slowness);
		}

		if (Keybinds.JUMP.isPressed()) {
			if (this.player.isOnGround()) {
				this.player.addVelocity(0.0f, this.player.getJumpStrength(), 0.0f);
			}
		}

		if (Keybinds.DESTROY.hasBeenPressed()) {
			TilePos pos = this.player.rayCast(10.0).pos;

			if (this.world.isInWorld(pos)) {
				this.world.writeTile(pos, Tile.AIR.id);
			}
		}

		if (Keybinds.INTERACT.hasBeenPressed()) {
			RaycastResult result = this.player.rayCast(10.0);
			TilePos pos = result.face.apply(result.pos);

			if (this.world.isInWorld(pos)) {
				this.world.writeTile(pos, Tile.STONE.id);
			}
		}

		if (Keybinds.RESPAWN.hasBeenPressed() || this.player.getTilePos().y < -20) {
			player.setPos(new Pos(0, 60, 0));
		}
	}

	private void render() {
		long time = System.nanoTime();

		// bind shader
		Shaders.gui.bind();
		// projection
		Shaders.gui.uniformMat4f("projection", this.guiProjection);
		// render gui
		this.crosshair.render();
		// bind shader
		Shaders.terrain.bind();
		// projection
		Shaders.terrain.uniformMat4f("projection", this.projection);
		// render world
		GraphicsSystem.bindTexture(TILE_ATLAS);

		for (Chunk chunk : this.world.getChunksForRendering()) {
			chunk.getOrCreateMesh().render(this.player.getCamera());
		}
		// unbind shader
		GraphicsSystem.bindTexture(0);
		Shader.unbind();
		long elapsed = (System.nanoTime() - time) / 1000000;

		if (elapsed > 180) {
			System.out.println("[Render/Warn] Unusually long time! Rendering took: " + elapsed + "ms. Note that this is normal if it merely happens on startup.");
		}
	}

	private final Window window;

	private double prevYPos = 0;
	private double prevXPos = 0;

	@Override
	public void invoke(long window, double xpos, double ypos) {
		double dx = xpos - this.prevXPos;
		double dy = ypos - this.prevYPos;

		if (Math.abs(dx) > 1.5f) {
			this.player.getCamera().rotateYaw((float) (dx) / 100.0f);
		}

		if (Math.abs(dy) > 1.5f) {
			this.player.getCamera().rotatePitch((float) (dy) / 60.0f);
		}

		this.prevYPos = ypos;
		this.prevXPos = xpos;
	}

	private static final float PI = (float) Math.PI;
	private static final float HALF_PI = PI / 2;
	private static final int TICK_DELTA = 100 / 20;
}
