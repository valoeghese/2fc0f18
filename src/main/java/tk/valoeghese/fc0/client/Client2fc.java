package tk.valoeghese.fc0.client;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.openal.ALC10;
import tk.valoeghese.fc0.client.gui.Crosshair;
import tk.valoeghese.fc0.client.gui.GUI;
import tk.valoeghese.fc0.client.gui.Overlay;
import tk.valoeghese.fc0.client.gui.Text;
import tk.valoeghese.fc0.client.keybind.KeybindManager;
import tk.valoeghese.fc0.client.keybind.MousebindManager;
import tk.valoeghese.fc0.client.model.Shaders;
import tk.valoeghese.fc0.client.model.Textures;
import tk.valoeghese.fc0.client.system.*;
import tk.valoeghese.fc0.client.world.ClientChunk;
import tk.valoeghese.fc0.client.world.ClientChunkSelection;
import tk.valoeghese.fc0.util.RaycastResult;
import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static tk.valoeghese.fc0.client.model.Textures.TILE_ATLAS;

public class Client2fc implements Runnable, GLFWCursorPosCallbackI {
	public Client2fc() {
		// initialise Graphics and Audio systems
		GraphicsSystem.initGLFW();
		this.window = new Window(640 * 2, 360 * 2);
		GraphicsSystem.initGL(this.window);
		AudioSystem.initAL();;
		// setup shaders, world, projections, etc
		this.setFOV(64);
		this.guiProjection = new Matrix4f().ortho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
		glfwSetKeyCallback(this.window.glWindow, KeybindManager.INSTANCE);
		glfwSetMouseButtonCallback(this.window.glWindow, MousebindManager.INSTANCE);
		Shaders.loadShaders();
		this.world = new ClientChunkSelection(0, 3);
	}

	private ClientChunkSelection world;
	private Matrix4f projection;
	private final Matrix4f guiProjection;
	private ClientPlayer player;
	private long nextUpdate = 0;
	private GUI crosshair;
	private GUI version;
	private GUI waterOverlay;
	private long time = 0;
	private int fov;
	private boolean titleScreen = true;
	private GUI titleText;

	public void createWorld() {
		this.world = new ClientChunkSelection(new Random().nextLong(), 9);
		this.time = 0;
		this.player.changeWorld(this.world);
	}

	public void setFOV(int newFOV) {
		this.fov = newFOV;
		this.projection = new Matrix4f().perspective((float) Math.toRadians(this.fov), this.window.aspect, 0.01f, 250.0f);
	}

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
		ALC10.alcCloseDevice(AudioSystem.getDevice());
	}

	private void tick() {
		if (this.titleScreen) {
			this.player.getCamera().rotateYaw(0.002f);
		}

		this.handleKeybinds();
		this.player.tick();
		++this.time;
	}

	private void init() {
		glEnable(GL_DEPTH_TEST);
		glClearColor(0.3f, 0.5f, 0.9f, 1.0f);
		glfwSetInputMode(this.window.glWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		glfwSetCursorPosCallback(this.window.glWindow, this);

		this.player = new ClientPlayer(new Camera(), this.world);
		this.prevYPos = this.window.height / 2.0f;
		this.prevXPos = this.window.width / 2.0f;
		this.crosshair = new Crosshair();
		this.version = new Overlay(Textures.VERSION);
		this.waterOverlay = new Overlay(Textures.WATER_OVERLAY);
		this.titleText = new Text("Click to start.", -0.85f, 0.5f, 2.2f);
		this.world.populateChunks();
		this.player.getCamera().rotateYaw((float) Math.PI);
	}

	private void handleKeybinds() {
		if (this.titleScreen) {
			if (Keybinds.DESTROY.hasBeenPressed()) {
				this.titleScreen = false;
				this.world.destroy();
				this.createWorld();
				this.world.populateChunks();
			}
		} else {
			final float yaw = this.player.getCamera().getYaw();
			float slowness = this.player.getHorizontalSlowness();
			boolean lr = Keybinds.MOVE_LEFT.isPressed() || Keybinds.MOVE_RIGHT.isPressed();
			boolean fb = Keybinds.MOVE_BACKWARDS.isPressed() || Keybinds.MOVE_FORWARDS.isPressed();

			if (Keybinds.RUN.isPressed()) {
				slowness /= 1.67;
			}

			if (this.player.isSwimming()) {
				slowness *= 2;
			}

			// make it so you can't move in two horizontal directions to get extra speed
			if (lr && fb) {
				slowness = org.joml.Math.sqrt(2 * (slowness * slowness));
			}

			if (Keybinds.MOVE_BACKWARDS.isPressed()) {
				this.player.addVelocity(-sin(yaw) / slowness, 0.0f, cos(yaw) / slowness);
			} else if (Keybinds.MOVE_FORWARDS.isPressed()) {
				this.player.addVelocity(-sin(yaw - PI) / slowness, 0.0f, cos(yaw - PI) / slowness);
			}

			if (Keybinds.MOVE_LEFT.isPressed()) {
				this.player.addVelocity(-sin(yaw + HALF_PI) / slowness, 0.0f, cos(yaw + HALF_PI) / slowness);
			} else if (Keybinds.MOVE_RIGHT.isPressed()) {
				this.player.addVelocity(-sin(yaw - HALF_PI) / slowness, 0.0f, cos(yaw - HALF_PI) / slowness);
			}

			if (Keybinds.JUMP.isPressed()) {
				long time = System.currentTimeMillis();

				if (this.player.isSwimming() && time > this.player.lockSwim) {
					this.player.addVelocity(0.0f, this.player.getJumpStrength() * 0.03f, 0.0f);
				} else {
					this.player.lockSwim = time + 18;

					if (this.player.isOnGround()) {
						this.player.addVelocity(0.0f, this.player.getJumpStrength(), 0.0f);
					}
				}
			}

			if (Keybinds.SELECT_1.hasBeenPressed()) {
				this.player.selectedTile = Tile.STONE;
			} else if (Keybinds.SELECT_2.hasBeenPressed()) {
				this.player.selectedTile = Tile.GRASS;
			} else if (Keybinds.SELECT_3.hasBeenPressed()) {
				this.player.selectedTile = Tile.LOG;
			} else if (Keybinds.SELECT_4.hasBeenPressed()) {
				this.player.selectedTile = Tile.LEAVES;
			} else if (Keybinds.SELECT_5.hasBeenPressed()) {
				this.player.selectedTile = Tile.SAND;
			} else if (Keybinds.SELECT_6.hasBeenPressed()) {
				this.player.selectedTile = Tile.DAISY;
			} else if (Keybinds.SELECT_7.hasBeenPressed()) {
				this.player.selectedTile = Tile.TALLGRASS;
			} else if (Keybinds.SELECT_8.hasBeenPressed()) {
				this.player.selectedTile = Tile.BRICKS;
			} else if (Keybinds.SELECT_9.hasBeenPressed()) {
				this.player.selectedTile = Tile.STONE_BRICKS;
			}

			if (Keybinds.DESTROY.hasBeenPressed()) {
				TilePos pos = this.player.rayCast(10.0).pos;

				if (this.world.isInWorld(pos)) {
					if (this.world.readTile(pos) != Tile.WATER.id) {
						this.world.writeTile(pos, Tile.AIR.id);
					}
				}
			}

			if (Keybinds.INTERACT.hasBeenPressed()) {
				RaycastResult result = this.player.rayCast(10.0);

				if (result.face != null) {
					TilePos pos = result.face.apply(result.pos);

					if (this.world.isInWorld(pos)) {
						if (this.player.selectedTile.id == Tile.DAISY.id && this.world.readTile(pos.down()) == Tile.SAND.id) {
							this.world.writeTile(pos, Tile.CACTUS.id);
						} else {
							this.world.writeTile(pos, this.player.selectedTile.id);
						}
					}
				}
			}

			if (Keybinds.RESPAWN.hasBeenPressed() || this.player.getTilePos().y < -20) {
				player.setPos(new Pos(0, this.world.getHeight(0, 0) + 1, 0));
			}
		}

		if (Keybinds.FOV_DOWN.hasBeenPressed()) {
			if (this.fov > 30) {
				this.setFOV(this.fov - 5);
			}
		}

		if (Keybinds.FOV_UP.hasBeenPressed()) {
			if (this.fov < 90) {
				this.setFOV(this.fov + 5);
			}
		}
	}

	private void render() {
		long time = System.nanoTime();
		float lighting = MathsUtils.clampMap(sin((float) this.time / 3072.0f), -1, 1, 0.125f, 1.15f);
		glClearColor(0.35f * lighting, 0.55f * lighting, 0.95f * lighting, 1.0f);

		// bind shader
		Shaders.terrain.bind();
		// time and stuff
		Shaders.terrain.uniformInt("time", (int) System.currentTimeMillis());
		Shaders.terrain.uniformFloat("lighting", lighting);
		// projection
		Shaders.terrain.uniformMat4f("projection", this.projection);
		// defaults
		Shaders.terrain.uniformInt("waveMode", 0);
		// render world
		GraphicsSystem.bindTexture(TILE_ATLAS);

		this.world.updateChunksForRendering();

		for(ClientChunk chunk : this.world.getChunksForRendering()){
			chunk.getOrCreateMesh().renderTerrain(this.player.getCamera());
		}

		for(ClientChunk chunk : this.world.getChunksForRendering()){
			chunk.getOrCreateMesh().renderWater(this.player.getCamera());
		}

		// bind shader
		Shaders.gui.bind();
		// projection
		Shaders.gui.uniformMat4f("projection", this.guiProjection);
		// render gui
		if (this.titleScreen) {
			this.titleText.render();
		} else {
			this.version.render();
			this.crosshair.render();
		}

		if (this.player.isUnderwater()) {
			GraphicsSystem.enableBlend();
			this.waterOverlay.render();
			GraphicsSystem.disableBlend();
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
		if (!this.titleScreen) {
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
	}

	private static final float PI = (float) Math.PI;
	private static final float HALF_PI = PI / 2;
	private static final int TICK_DELTA = 100 / 20;
}
