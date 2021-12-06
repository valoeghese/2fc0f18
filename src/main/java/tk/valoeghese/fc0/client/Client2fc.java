package tk.valoeghese.fc0.client;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.openal.AL10;
import tk.valoeghese.fc0.BrandAndVersion;
import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.client.keybind.KeybindManager;
import tk.valoeghese.fc0.client.keybind.MousebindManager;
import tk.valoeghese.fc0.client.render.Shaders;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.entity.EntityRenderer;
import tk.valoeghese.fc0.client.render.gui.GUI;
import tk.valoeghese.fc0.client.render.gui.Overlay;
import tk.valoeghese.fc0.client.render.gui.collection.Hotbar;
import tk.valoeghese.fc0.client.render.model.SquareModel;
import tk.valoeghese.fc0.client.screen.CraftingScreen;
import tk.valoeghese.fc0.client.screen.GameScreen;
import tk.valoeghese.fc0.client.screen.OptionsMenuScreen;
import tk.valoeghese.fc0.client.screen.PauseMenuScreen;
import tk.valoeghese.fc0.client.screen.Screen;
import tk.valoeghese.fc0.client.screen.TitleScreen;
import tk.valoeghese.fc0.client.screen.YouDiedScreen;
import tk.valoeghese.fc0.client.sound.ClientSoundEffect;
import tk.valoeghese.fc0.client.sound.MusicSystem;
import tk.valoeghese.fc0.client.sound.SoundEffectDispatcher;
import tk.valoeghese.fc0.client.world.ClientChunk;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import tk.valoeghese.fc0.client.world.ClientWorld;
import tk.valoeghese.fc0.language.Language;
import tk.valoeghese.fc0.util.TimerSwitch;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.entity.Entity;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.player.CraftingManager;
import tk.valoeghese.fc0.world.player.ItemType;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.save.FakeSave;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.sound.SoundEffect;
import tk.valoeghese.fc0.world.tile.Tile;
import valoeghese.scalpel.Camera;
import valoeghese.scalpel.Shader;
import valoeghese.scalpel.Window;
import valoeghese.scalpel.scene.Model;
import valoeghese.scalpel.util.ALUtils;
import valoeghese.scalpel.util.GLUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Properties;
import java.util.Random;
import java.util.function.Function;

import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static tk.valoeghese.fc0.client.render.Textures.*;

public class Client2fc extends Game2fc<ClientWorld, ClientPlayer> implements Runnable, GLFWCursorPosCallbackI {
	public Client2fc(boolean allowsDev, boolean allowsNoClip) {
		long time = System.currentTimeMillis();
		instance = this;
		// initialise Graphics and Audio systems
		GLUtils.initGLFW();
		this.window = new Window2fc(BrandAndVersion.isModded() ? "2fc0f18 (" + BrandAndVersion.getBrand() + ")" : "2fc0f18", 640 * 2, 360 * 2);
		GLUtils.initGL(this.window);
		ALUtils.initAL();
		System.out.println("Setup GL/AL in " + (System.currentTimeMillis() - time) + "ms");

		// setup shaders, world, projections, etc
		this.guiProjection = new Matrix4f().ortho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);

		// load preferences
		File properties = new File("preferences.txt");

		try {
			if (!properties.createNewFile()) {
				try (FileReader reader = new FileReader(properties)) {
					this.preferences.load(reader);
				} catch (IOException e) {
					System.err.println("Error Reading Preferences File! Proceeding with reset preferences.");
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot create preferences.txt!", e);
		}

		this.allowsDev = allowsDev;
		this.allowsNoClip = allowsNoClip;
	}

	private long clientThreadId;
	private Matrix4f projection;
	private final Matrix4f guiProjection;
	private long nextUpdate = 0;
	private GUI waterOverlay;
	private int fov;
	private float sprintFOV = 1.0f;
	private float nextSprintFOV = 1.0f;
	public Pos spawnLoc = Pos.ZERO;
	public Language language = Language.EN_GB;

	// properties
	private final Properties preferences = new Properties();
	private final boolean allowsDev;
	private final boolean allowsNoClip;

	public GameScreen gameScreen;
	public Screen titleScreen;
	public Screen craftingScreen;
	public Screen pauseScreen;
	public Screen optionsScreen;
	private Screen currentScreen;
	private Screen youDiedScreen;

	private SoundEffectDispatcher soundEffectDispatcher;

	@Nullable
	public Save save = null;
	private final Window window;
	private double prevYPos = 0;
	private double prevXPos = 0;
	private boolean showDebug = false;
	private final TimerSwitch timerSwitch = new TimerSwitch();
	private GUI setupGUI;
	private Model sun;
	public boolean renderWorld = true;
	private float freezeInterpolation; // interpolation at game pause time

	/**
	 * @return whether the player is allowed to enter dev mode.
	 */
	public boolean allowsDev() {
		return this.allowsDev;
	}

	/**
	 * @return whether the player is allowed to enter no-clip mode.
	 */
	public boolean allowsNoClip() {
		return this.allowsNoClip;
	}

	@Override
	public boolean isMainThread() {
		return Thread.currentThread().getId() == this.clientThreadId;
	}

	@Override
	public void invoke(long window, double xpos, double ypos) {
		double dx = xpos - this.prevXPos;
		double dy = ypos - this.prevYPos;

		this.currentScreen.handleMouseInput(dx, dy);
		this.prevYPos = ypos;
		this.prevXPos = xpos;
	}

	@Override
	public SoundEffect createSoundEffect(String name, String... resources) {
		return ClientSoundEffect.create(name, resources);
	}

	@Override
	public void playSound(@Nullable Player toExcept, SoundEffect effect, double x, double y, double z) {
		double dx = this.player.getX() - x;
		double dy = this.player.getY() - y;
		double dz = this.player.getZ() - z;
		this.soundEffectDispatcher.playSound((ClientSoundEffect) effect, (float) (dx), (float)  (dy), (float)  (dz));
	}

	public void playSound(SoundEffect effect) {
		this.soundEffectDispatcher.playSound((ClientSoundEffect) effect, 0, 0, 0);
	}

	@Override
	public void run() {
		this.clientThreadId = Thread.currentThread().getId();

		this.setupGUI = new Overlay(Textures.STARTUP);
		Shaders.loadShaders();
		Shaders.gui.uniformFloat("opacity", 1.0f);

		Thread t = new Thread(this::init);
		t.setDaemon(true);
		t.start();

		while (t.isAlive()) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			// render loading screen
			Shaders.gui.bind();
			Shaders.gui.uniformMat4f("projection", this.guiProjection);
			Shaders.gui.uniformFloat("lighting", 1.0f);
			this.setupGUI.render();
			Shader.unbind();

			this.window.swapBuffers();
			glfwPollEvents();
		}

		this.initGameRendering();
		this.initGameAudio();
		this.activateLoadScreen(); // keep it on while chunkloading is happening to hide our distributed chunk-adding

		while (this.window.isOpen()) {
			long timeMillis = System.currentTimeMillis();

			if (timeMillis >= this.nextUpdate) {
				this.nextUpdate = timeMillis + TICK_DELTA;

				// do 6 queued tasks per tick
				this.runNextQueued(6);
				this.updateNextLighting();

				if (this.timerSwitch.isOn()) {
					this.timerSwitch.update();

					if (!this.timerSwitch.isOn()) { // this is probably causing the bugs with infinite respawn loading times. Maybe a SAVE#ISTHREADALIVE bug. TODO is this fixed with the rewrite?
						if (this.getLightingQueueSize() > 12 || Save.isThreadAlive()) {
							this.activateLoadScreen();
						}
					}
				} else {
					this.handleKeybinds();
				}

				if (!this.currentScreen.isPauseScreen()) {
					this.tick();
				}
			}

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			this.render(this.currentScreen.isPauseScreen() ? this.freezeInterpolation : 1.0f - ((float)(this.nextUpdate - timeMillis) / (float)TICK_DELTA));
			this.window.swapBuffers();
			glfwPollEvents();

			if (Keybinds.ESCAPE.hasBeenPressed()) {
				this.currentScreen.handleEscape(this.window);
			}
		}

		this.world.destroy();
		this.window.destroy();
		this.soundEffectDispatcher.destroy();
		MusicSystem.shutdown();
		ALUtils.shutdown(); // destroys all audio buffers
		Chunk.shutdown(); // may System.exit from here or Save#shutDown so put any further tasks that need to execute either before these or in the force shutdown
		Save.shutdown();
	}

	@Override
	protected void tick() {
		// TODO move screen dependent logic to a Screen::tick method
		// TODO fix the todo by using scalpel client stuff probably

		if (this.currentScreen == this.titleScreen) {
			if (NEW_TITLE) {
				this.player.forceMove(0, 0, 0.025f);
			} else {
				this.player.getCamera().rotateYaw(0.002f);
			}
		}

		super.tick();

		EcoZone zone = this.world.getEcozone(this.player.getTileX(), this.player.getTileZ());

		if (this.currentScreen == this.gameScreen) {
			TilePos tilePos = this.player.getTilePos();

			if (this.player.cachedPos != tilePos) {
				this.gameScreen.coordsWidget.changeText(tilePos.toChunkPos().toString() + "\n" + tilePos);

				if (this.player.chunk != null) { // TODO placeholder for kingdom
					this.gameScreen.heightmapWidget.changeText("Heightmap: " + this.player.chunk.getHeightmap(tilePos.x & 0xF, tilePos.z & 0xF));
					this.gameScreen.lightingWidget.changeText(this.player.chunk.getLightLevelText(tilePos.x & 0xF, tilePos.y, tilePos.z & 0xF));

					Kingdom kingdom = this.player.chunk.getKingdom(tilePos.x & 0xF, tilePos.z & 0xF);

					if (this.gameScreen.getCurrentKingdom() != kingdom) {
						this.gameScreen.setCurrentKingdom(kingdom);
					}
				} else {
					this.gameScreen.heightmapWidget.changeText("Loading");
					this.gameScreen.lightingWidget.changeText("Loading");
				}
			}

			if (zone != this.player.cachedZone) {
				this.player.cachedZone = zone;
				String newValue = this.language.translate(zone.toString());
				this.gameScreen.biomeWidget.changeText(newValue);
			}

			if (!this.player.isAlive()) {
				this.switchScreen(this.youDiedScreen);
			}
		}

		// Smooth Sprint FOV
		if (this.sprintFOV > this.nextSprintFOV + 0.001f) {
			this.sprintFOV -= 0.01f;
			this.projection = new Matrix4f().perspective((float) Math.toRadians(this.fov * this.sprintFOV), this.window.getAspect(), 0.01f, 250.0f);
		} else if (this.sprintFOV < this.nextSprintFOV - 0.001f) {
			this.sprintFOV += 0.01f;
			this.projection = new Matrix4f().perspective((float) Math.toRadians(this.fov * this.sprintFOV), this.window.getAspect(), 0.01f, 250.0f);
		}
		
		if (this.world != null) {
			this.world.updateSkylight(); // this is only used for ingame stuff, not spawning. Do I even need to have the world cache this anymore?
		}

		// Music System
		MusicSystem.tick(this.currentScreen);
	}

	private void initGameRendering() {
		long start = System.currentTimeMillis();
		glfwSetKeyCallback(this.window.getHandle(), KeybindManager.INSTANCE);
		glfwSetMouseButtonCallback(this.window.getHandle(), MousebindManager.INSTANCE);
		glEnable(GL_DEPTH_TEST);
		glClearColor(0.3f, 0.5f, 0.9f, 1.0f);
		glfwSetCursorPosCallback(this.window.getHandle(), this);

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

		this.prevYPos = this.window.getHeight() / 2.0f;
		this.prevXPos = this.window.getWidth() / 2.0f;

		this.gameScreen = new GameScreen(this);
		this.titleScreen = new TitleScreen(this);
		this.craftingScreen = new CraftingScreen(this);
		this.pauseScreen = new PauseMenuScreen(this, this.gameScreen);
		this.optionsScreen = new OptionsMenuScreen(this, this.pauseScreen, this.preferences);
		this.youDiedScreen = new YouDiedScreen(this);
		this.switchScreen(this.titleScreen);

		this.waterOverlay = new Overlay(Textures.WATER_OVERLAY);
		this.sun = new SquareModel();

		System.out.println("Initialised Game Rendering in " + (System.currentTimeMillis() - start) + "ms.");
	}

	private void init() {
		long time = System.currentTimeMillis();
		this.setFOV(64);

		this.world = new ClientWorld(new FakeSave(0), 0, TITLE_WORLD_SIZE);
		this.player = new ClientPlayer(new Camera2fc(), this, false);
		this.player.changeWorld(this.world, this.save);

		if (NEW_TITLE) {
			this.player.setNoClip(true);
			this.player.forceMove(0, 15, 0);
		}

		this.player.getCamera().rotateYaw((float) Math.PI);

		CraftingManager.addCraftingRecipes();

		System.out.println("Initialised 2fc0f18 in " + (System.currentTimeMillis() - time) + "ms.");
	}

	private void initGameAudio() {
		long start = System.currentTimeMillis();
		MusicSystem.init();

		for (SoundEffect effect : SoundEffect.getSoundEffects()) {
			effect.initialise();
		}

		this.soundEffectDispatcher = new SoundEffectDispatcher(16);

		System.out.println("Initialised Game Audio in " + (System.currentTimeMillis() - start) + "ms.");
	}

	private void render(float tickDelta) {
		long time = System.nanoTime();
		float skylight = this.calculateSkyLighting();

		if (this.timerSwitch.isOn()) {
			Shaders.gui.bind();
			Shaders.gui.uniformMat4f("projection", this.guiProjection);
			Shaders.gui.uniformFloat("lighting", 1.0f);
			this.setupGUI.render();
			Shader.unbind();
		} else {
			glClearColor(0.35f * skylight, 0.55f * skylight, 0.95f * skylight, 1.0f);

			// update camera
			this.player.updateCameraPos(tickDelta);

			// bind shader
			Shaders.terrain.bind();
			// time and stuff
			Shaders.terrain.uniformInt("time", (int) System.currentTimeMillis());
			Shaders.terrain.uniformFloat("skylight", skylight);
			// projection
			Shaders.terrain.uniformMat4f("projection", this.projection);
			// defaults
			Shaders.terrain.uniformInt("waveMode", 0);
			Shaders.terrain.uniformMat4f("view", this.player.getCamera().getView());
			// render world
			GLUtils.bindTexture(TILE_ATLAS);

			if (renderWorld) {
				for (ClientChunk chunk : this.world.getRenderingChunks()) {
					chunk.getOrCreateMesh().renderSolidTerrain();
				}

				GLUtils.enableBlend();

				for (ClientChunk chunk : this.world.getRenderingChunks()) {
					chunk.getOrCreateMesh().renderTranslucentTerrain();
				}

				Shaders.terrain.uniformInt("waveMode", 1);

				for (ClientChunk chunk : this.world.getRenderingChunks()) {
					chunk.getOrCreateMesh().renderWater();
				}

				Shaders.terrain.uniformInt("waveMode", 0);
				GLUtils.disableBlend();
			}

			// render entities
			GLUtils.bindTexture(ENTITY_ATLAS);

			for (Entity entity : this.world.getEntities(this.player.getTileX(), this.player.getTileZ(), 20)) {
				EntityRenderer renderer = entity.getRenderer();

				if (renderer != null) {
					//renderer.getOrCreateModel();
				}
			}

			// Render The Sun
			GLUtils.bindTexture(THE_SUN);
			float yaw = this.player.getCamera().getYaw();

			Shaders.terrain.uniformMat4f("view", new Matrix4f()
					.rotate(new AxisAngle4f(yaw, 0.0f, 1.0f, 0.0f))
					.rotate(new AxisAngle4f(this.player.getCamera().getPitch(), -sin(yaw - NINETY_DEGREES), 0.0f, cos(yaw - NINETY_DEGREES)))
			);
			Shaders.terrain.uniformFloat("skylight", -1.0f); // this makes it ignore lighting calculations

			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_COLOR, GL_ONE);
			float skyAngle = this.calculateSkyAngle();
			// TODO should Matrix4f calculations be cached since the sky angle only changes every tick
			Shaders.terrain.uniformMat4f("transform", new Matrix4f()
					.scale(16.0f)
					.rotate(new AxisAngle4f(skyAngle - PI + 8.0f * PI,1.0f, 0.0f, 0.0f))
					.translate(new Vector3f(0, 0, 10.0f)));
			this.sun.render();
			GLUtils.disableBlend();

			// bind shader
			Shaders.gui.bind();
			// projection
			Shaders.gui.uniformMat4f("projection", this.guiProjection);
			// defaults
			Shaders.gui.uniformFloat("lighting", 1.0f);
			// render gui
			this.renderGUI(skylight);
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
		this.activateLoadScreen();
		this.world.destroy();
		this.world = world;
		this.renderWorld = true;
	}

	public void saveWorld() {
		if (this.save != null) {
			this.save.writeForClient(this.player, this.world, this.player.getInventory().iterator(), this.player.getInventory().getSize(), this.player.getPos(), this.spawnLoc, this.time);
		}
	}

	public void createWorld(String saveName) {
		this.setShowDebug(false);
		this.saveWorld();
		this.time = 0;
		this.save = new Save(saveName, new Random().nextLong());
		this.player.setNoClip(false);
		this.player.setMaxHealth(this.save.loadedMaxHP);
		this.player.setHealth(this.save.loadedHP);
		// idek how big this is it's probably more than the game can handle if you go out that far
		// TODO should I clear toUpdateLighting here? Or will that f*k up lighting in saved chunks?
		// I mean in the case of title screen it's fine probably
		// but if from world to world directly or sth
		this.setWorld(new ClientWorld(this.save, this.save.getSeed(), 1500));

		if (this.save.spawnLocPos != null) {
			this.spawnLoc = this.save.spawnLocPos;
		} else {
			this.spawnLoc = null;
		}

		if (this.save.lastSavePos != null && this.spawnLoc != null) {
			this.player.changeWorld(this.world, this.save, this.save.lastSavePos);
		} else {
			ChunkPos spawnChunk = this.world.getSpawnPos();
			this.world.chunkLoad(spawnChunk);

			this.world.scheduleForChunk(GameplayWorld.key(spawnChunk.x, spawnChunk.z), c -> {
				int x = (c.x << 4) + 8;
				int z = (c.z << 4) + 8;
				this.spawnLoc = new Pos(x, c.getHeight(x & 0xF, z & 0xF) + 1, z);
				this.player.changeWorld(this.world, this.save, this.spawnLoc);
			}, "changePlayerWorld");
		}

		this.player.dev = this.save.loadedDevMode;
	}

	public void setFOV(int newFOV) {
		this.fov = newFOV;
		this.projection = new Matrix4f().perspective((float) Math.toRadians(this.fov * this.sprintFOV), this.window.getAspect(), 0.01f, 250.0f);
	}

	public int getFOV() {
		return this.fov;
	}

	public void sprintFOV(float correctSprintFOV) {
		this.nextSprintFOV = correctSprintFOV;
	}

	public float getWindowAspect() {
		return this.window.aspect;
	}

	public long getWindowId() {
		return this.window.getHandle();
	}

	public Window getWindow() {
		return this.window;
	}

	@Nullable
	public Hotbar getHotbarRenderer() {
		return this.gameScreen == null ? null : this.gameScreen.hotbarRenderer;
	}

	public String getProperty(String key, String defaultValue) {
		return this.preferences.getProperty(key, defaultValue);
	}

	public void activateLoadScreen() {
		this.timerSwitch.switchOn(1000);
	}

	public void switchScreen(Screen screen) {
		if (this.currentScreen != null) {
			if (screen.isPauseScreen()) {
				this.freezeInterpolation = 1.0f - ((float) (this.nextUpdate - System.currentTimeMillis()) / (float) TICK_DELTA);
			} else if (this.currentScreen.isPauseScreen()) {
				this.nextUpdate = System.currentTimeMillis() + TICK_DELTA - (int) (this.freezeInterpolation * TICK_DELTA);
			}
		}

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

	public static Client2fc getInstance() {
		return instance;
	}

	private static Client2fc instance;

	public static final float PI = (float) Math.PI;
	public static final float HALF_PI = PI / 2;
	private static final int TICK_DELTA = 1000 / 20;
	public static final int TITLE_WORLD_SIZE = 1000;
	public static final boolean NEW_TITLE = true;
	private static final Matrix4f IDENTITY = new Matrix4f();
	private static final float NINETY_DEGREES = (float) Math.toRadians(90);

	private class Window2fc extends Window {
		public Window2fc(String title, int width, int height) {
			super(title, width, height);
		}

		@Override
		public void invoke(long window, int width, int height) {
			super.invoke(window, width, height);
			Client2fc.this.projection = new Matrix4f().perspective((float) Math.toRadians(Client2fc.this.fov * Client2fc.this.sprintFOV), Client2fc.this.window.getAspect(), 0.01f, 250.0f);
		}
	}

	private static class Camera2fc extends Camera {
		@Override
		public void rotatePitch(float pitch) {
			super.rotatePitch(pitch);
			this.rotateListener();
		}

		@Override
		public void rotateYaw(float yaw) {
			super.rotateYaw(yaw);
			this.rotateListener();
		}

		@Override
		public void setPitch(float f) {
			super.setPitch(f);
			this.rotateListener();
		}

		@Override
		public void setYaw(float f) {
			super.setYaw(f);
			this.rotateListener();
		}

		private void rotateListener() {
			ByteBuffer buffer = ByteBuffer.allocateDirect(6 * 4);
			buffer.order( ByteOrder.nativeOrder() );

			Matrix4f view = this.getView();
			buffer.putFloat(view.m01());
			buffer.putFloat(view.m02());
			buffer.putFloat(view.m03());
			buffer.putFloat(view.m11());
			buffer.putFloat(view.m12());
			buffer.putFloat(view.m13());
			buffer.flip();

			AL10.alListenerfv(AL10.AL_ORIENTATION, buffer.asFloatBuffer());
		}
	}
}
