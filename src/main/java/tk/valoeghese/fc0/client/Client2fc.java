package tk.valoeghese.fc0.client;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.client.keybind.KeybindManager;
import tk.valoeghese.fc0.client.keybind.MousebindManager;
import tk.valoeghese.fc0.client.render.Shaders;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.entity.EntityRenderer;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import tk.valoeghese.fc0.client.render.gui.collection.Hotbar;
import tk.valoeghese.fc0.client.render.screen.CraftingScreen;
import tk.valoeghese.fc0.client.render.screen.GameScreen;
import tk.valoeghese.fc0.client.render.screen.Screen;
import tk.valoeghese.fc0.client.render.screen.TitleScreen;
import tk.valoeghese.fc0.client.render.system.Audio;
import tk.valoeghese.fc0.client.render.system.Camera;
import tk.valoeghese.fc0.client.render.system.Shader;
import tk.valoeghese.fc0.client.render.system.Window;
import tk.valoeghese.fc0.client.render.system.gui.GUI;
import tk.valoeghese.fc0.client.render.system.util.GLUtils;
import tk.valoeghese.fc0.client.world.ClientChunk;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import tk.valoeghese.fc0.client.world.ClientWorld;
import tk.valoeghese.fc0.language.Language;
import tk.valoeghese.fc0.util.TimerSwitch;
import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.entity.Entity;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.player.CraftingManager;
import tk.valoeghese.fc0.world.player.ItemType;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.function.Function;

import static org.joml.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static tk.valoeghese.fc0.client.render.Textures.ENTITY_ATLAS;
import static tk.valoeghese.fc0.client.render.Textures.TILE_ATLAS;

public class Client2fc extends Game2fc<ClientWorld, ClientPlayer> implements Runnable, GLFWCursorPosCallbackI {
	public Client2fc() {
		long time = System.currentTimeMillis();
		instance = this;
		// initialise Graphics and Audio systems
		GLUtils.initGLFW();
		this.window = new Window(640 * 2, 360 * 2);
		GLUtils.initGL(this.window);
		Audio.initAL();
		System.out.println("Setup GL/AL in " + (System.currentTimeMillis() - time) + "ms");

		// setup shaders, world, projections, etc
		this.guiProjection = new Matrix4f().ortho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
	}

	private Matrix4f projection;
	private final Matrix4f guiProjection;
	private long nextUpdate = 0;
	private GUI waterOverlay;
	private int fov;
	public Pos spawnLoc = Pos.ZERO;
	public Language language = Language.EN_GB;
	public GameScreen gameScreen;
	public Screen titleScreen;
	public Screen craftingScreen;
	private Screen currentScreen;
	@Nullable
	public Save save = null;
	private final Window window;
	private double prevYPos = 0;
	private double prevXPos = 0;
	private final Queue<Runnable> later = new LinkedList<>();
	private boolean showDebug = false;
	private final TimerSwitch timerSwitch = new TimerSwitch();
	private GUI setupScreen;

	public static Client2fc getInstance() {
		return instance;
	}

	public void runLater(Runnable callback) {
		synchronized (this.later) {
			this.later.add(callback);
		}
	}

	@Override
	public void run() {
		this.setupScreen = new Overlay(Textures.STARTUP);
		Shaders.loadShaders();

		Thread t = new Thread(this::init);
		t.start();

		while (t.isAlive()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			// render loading screen
			Shaders.gui.bind();
			Shaders.gui.uniformMat4f("projection", this.guiProjection);
			Shaders.gui.uniformFloat("lighting", 1.0f);
			this.setupScreen.render();
			Shader.unbind();

			this.window.swapBuffers();
			glfwPollEvents();
		}

		this.initGameRendering();
		this.initGameAudio();


		while (this.window.isOpen()) {
			long timeMillis = System.currentTimeMillis();

			if (timeMillis >= this.nextUpdate) {
				this.nextUpdate = timeMillis + TICK_DELTA;

				Runnable task = null;

				synchronized (this.later) {
					if (!this.later.isEmpty()) {
						task = this.later.remove();
					}
				}

				if (task != null) {
					task.run();
				}

				this.tick();
			}

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			this.render();
			Audio.tickAudio();
			this.window.swapBuffers();
			glfwPollEvents();

			if (Keybinds.ESCAPE.hasBeenPressed()) {
				this.currentScreen.handleEscape(this.window);
			}
		}

		this.world.destroy();
		this.window.destroy();
		Audio.shutdown();
		Chunk.shutdown();
	}

	@Override
	protected void tick() {
		boolean isTitleScreen = this.currentScreen == this.titleScreen;

		if (isTitleScreen) {
			this.player.getCamera().rotateYaw(0.002f);
		}

		this.handleKeybinds();

		super.tick();

		EcoZone zone = this.world.getEcozone(this.player.getX(), this.player.getZ());

		if (!isTitleScreen) {
			TilePos tilePos = this.player.getTilePos();

			if (this.player.cachedPos != tilePos) {
				this.gameScreen.coordsWidget.changeText(tilePos.toChunkPos().toString() + "\n" + tilePos.toString());
				this.gameScreen.lightingWidget.changeText(this.player.chunk.getLightLevelText(tilePos.x & 0xF, tilePos.y, tilePos.z & 0xF));
				this.gameScreen.kingdomWidget.changeText(this.player.chunk.getKingdom(tilePos.x & 0xF,tilePos.z & 0xF).toString());
			}

			if (zone != this.player.cachedZone) {
				this.player.cachedZone = zone;
				String newValue = this.language.translate(zone.toString());
				this.gameScreen.biomeWidget.changeText(newValue);
			}
		}

		if (this.timerSwitch.isOn()) {
			this.timerSwitch.update();

			if (!this.timerSwitch.isOn()) {
				boolean b;

				synchronized (this.later) {
					b = this.later.size() > 10;
				}

				if (b) {
					this.timerSwitch.switchOn(2000);
				}
			}
		}
	}

	private void initGameRendering() {
		long start = System.currentTimeMillis();
		glfwSetKeyCallback(this.window.id, KeybindManager.INSTANCE);
		glfwSetMouseButtonCallback(this.window.id, MousebindManager.INSTANCE);
		glEnable(GL_DEPTH_TEST);
		glClearColor(0.3f, 0.5f, 0.9f, 1.0f);
		glfwSetCursorPosCallback(this.window.id, this);

		// load in the atlases!
		Textures.loadGeneratedAtlases();
		Function<String, Vec2i> uvRequests = name -> new Vec2i(Textures.TILE_ATLAS_OBJ.imageLocationMap.get(name));

		for (Tile tile : Tile.BY_ID) {
			if (tile != null) {
				tile.requestUV(uvRequests);
			}
		}

		uvRequests = name -> new Vec2i(Textures.ITEM_ATLAS_OBJ.imageLocationMap.get(name));

		for (ItemType item : ItemType.ITEMS) {
			if (item != null) {
				item.requestUV(uvRequests);
			}
		}

		this.prevYPos = this.window.height / 2.0f;
		this.prevXPos = this.window.width / 2.0f;

		this.gameScreen = new GameScreen(this);
		this.titleScreen = new TitleScreen(this);
		this.craftingScreen = new CraftingScreen(this);
		this.switchScreen(this.titleScreen);

		this.waterOverlay = new Overlay(Textures.WATER_OVERLAY);

		System.out.println("Initialised Game Rendering in " + (System.currentTimeMillis() - start) + "ms.");
	}

	private void init() {
		long time = System.currentTimeMillis();
		this.setFOV(64);

		this.world = new ClientWorld(null, 0, 4);
		this.player = new ClientPlayer(new Camera(), this, false);
		this.player.changeWorld(this.world, this.save);
//		this.world.generateSpawnChunks(this.player.getTilePos().toChunkPos());
		this.player.getCamera().rotateYaw((float) Math.PI);

		CraftingManager.addCraftingRecipes();

		System.out.println("Initialised 2fc0f18 in " + (System.currentTimeMillis() - time) + "ms.");
	}

	private void initGameAudio() {
		long start = System.currentTimeMillis();
		//TODO everything
		System.out.println("Initialised Game Audio in " + (System.currentTimeMillis() - start) + "ms.");
	}

	private static final float SKY_CHANGE_RATE = 17.0f;

	private void render() {
		long time = System.nanoTime();
		float zeitGrellheit = sin((float) this.time / 9216.0f);
		float lighting = MathsUtils.clampMap(zeitGrellheit, -1, 1, 0.125f, 1.15f);
		this.world.assertSkylight((byte) MathsUtils.clamp(MathsUtils.floor(SKY_CHANGE_RATE * zeitGrellheit + 7.5f), 0, 10));

		if (this.timerSwitch.isOn()) {
			Shaders.gui.bind();
			Shaders.gui.uniformMat4f("projection", this.guiProjection);
			Shaders.gui.uniformFloat("lighting", 1.0f);
			this.setupScreen.render();
			Shader.unbind();
		} else {
			glClearColor(0.35f * lighting, 0.55f * lighting, 0.95f * lighting, 1.0f);

			// bind shader
			Shaders.terrain.bind();
			// time and stuff
			Shaders.terrain.uniformInt("time", (int) System.currentTimeMillis());
			Shaders.terrain.uniformFloat("lighting", 1.0f); // We Update chunk lighting to change lighting now.
			// projection
			Shaders.terrain.uniformMat4f("projection", this.projection);
			// defaults
			Shaders.terrain.uniformInt("waveMode", 0);
			// render world
			GLUtils.bindTexture(TILE_ATLAS);

			this.world.updateChunksForRendering();

			for (ClientChunk chunk : this.world.getChunksForRendering()) {
				chunk.getOrCreateMesh().renderSolidTerrain(this.player.getCamera());
			}

			GLUtils.enableBlend();

			for (ClientChunk chunk : this.world.getChunksForRendering()) {
				chunk.getOrCreateMesh().renderTranslucentTerrain(this.player.getCamera());
			}

			Shaders.terrain.uniformInt("waveMode", 1);

			for (ClientChunk chunk : this.world.getChunksForRendering()) {
				chunk.getOrCreateMesh().renderWater(this.player.getCamera());
			}

			Shaders.terrain.uniformInt("waveMode", 0);
			GLUtils.disableBlend();

			// render entities
			GLUtils.bindTexture(ENTITY_ATLAS);

			for (Entity entity : this.world.getEntities(this.player.getX(), this.player.getZ(), 20)) {
				EntityRenderer renderer = entity.getRenderer();

				if (renderer != null) {
					//renderer.getOrCreateModel();
				}
			}

			// bind shader
			Shaders.gui.bind();
			// projection
			Shaders.gui.uniformMat4f("projection", this.guiProjection);
			// defaults
			Shaders.gui.uniformFloat("lighting", 1.0f);
			// render gui
			this.renderGUI(lighting);
			// unbind shader
			GLUtils.bindTexture(0);

			Shader.unbind();
		}

		long elapsed = (System.nanoTime() - time) / 1000000;

		if (elapsed > 180) {
			System.out.println("[Render/Warn] Unusually long time! Rendering took: " + elapsed + "ms. Note that this is normal if it merely happens on startup.");
		}
	}

	private void renderGUI(float lighting) {
		this.currentScreen.renderGUI(lighting);

		if (this.player.isUnderwater()) {
			GLUtils.enableBlend();
			Shaders.gui.uniformFloat("lighting", lighting);
			this.waterOverlay.render();
			Shaders.gui.uniformFloat("lighting", 1.0f);
			GLUtils.disableBlend();
		}
	}

	private void handleKeybinds() {
		if (!this.timerSwitch.isOn()) {
			this.currentScreen.handleKeybinds();

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
	}

	public ClientPlayer getPlayer() {
		return this.player;
	}

	public ClientWorld getWorld() {
		return this.world;
	}

	public void setWorld(ClientWorld world) {
		this.world = world;
	}

	public void saveWorld() {
		if (this.save != null) {
			this.save.writeForClient(this.player, this.world.getChunks(), this.player.getInventory().iterator(), this.player.getInventory().getSize(), this.player.getPos(), this.spawnLoc, this.time);
		}
	}

	public void createWorld(String saveName) {
		this.timerSwitch.switchOn(7500);
		this.setShowDebug(false);
		this.world.destroy();
		this.saveWorld();
		this.time = 0;
		this.save = new Save(saveName, new Random().nextLong());
		// 240000 * 240000 world.
		this.world = new ClientWorld(this.save, this.save.getSeed(), 750);

		if (this.save.spawnLocPos != null) {
			this.spawnLoc = this.save.spawnLocPos;
		} else {
			this.spawnLoc = new Pos(0, this.world.getHeight(0, 0) + 1, 0);
		}

		if (this.save.lastSavePos != null) {
			this.player.changeWorld(this.world, this.save.lastSavePos, this.save);
		} else {
			this.player.changeWorld(this.world, this.save);
		}

		this.player.dev = this.save.loadedDevMode;
	}

	/*public void generateSpawnChunks() {
		this.world.generateSpawnChunks(this.player.getTilePos().toChunkPos());
	}*/

	public void setFOV(int newFOV) {
		this.fov = newFOV;
		this.projection = new Matrix4f().perspective((float) Math.toRadians(this.fov), this.window.aspect, 0.01f, 250.0f);
	}

	public float getWindowAspect() {
		return this.window.aspect;
	}

	public long getWindowId() {
		return this.window.id;
	}

	public Window getWindow() {
		return this.window;
	}

	@Nullable
	public Hotbar getHotbarRenderer() {
		return this.gameScreen == null ? null : this.gameScreen.hotbarRenderer;
	}

	@Override
	public void invoke(long window, double xpos, double ypos) {
		double dx = xpos - this.prevXPos;
		double dy = ypos - this.prevYPos;

		this.currentScreen.handleMouseInput(dx, dy);
		this.prevYPos = ypos;
		this.prevXPos = xpos;
	}

	public void switchScreen(Screen screen) {
		this.currentScreen = screen;
		this.currentScreen.onFocus();
	}

	public void setShowDebug(boolean showDebug) {
		if (this.showDebug != showDebug) {
			this.showDebug = showDebug;
			this.gameScreen.onShowDebug(showDebug);
		}
	}

	public boolean showDebug() {
		return this.showDebug;
	}

	public static final float PI = (float) Math.PI;
	public static final float HALF_PI = PI / 2;
	private static final int TICK_DELTA = 100 / 20;
	private static Client2fc instance;
}
