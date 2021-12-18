package tk.valoeghese.fc0.client.screen;

import org.joml.Math;
import tk.valoeghese.fc0.BrandAndVersion;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Shaders;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.*;
import tk.valoeghese.fc0.client.render.gui.collection.Hotbar;
import tk.valoeghese.fc0.client.sound.MusicPiece;
import tk.valoeghese.fc0.client.sound.MusicSettings;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import tk.valoeghese.fc0.util.RaycastResult;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.gen.generator.CityGenerator;
import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.tile.Tile;
import valoeghese.scalpel.Camera;
import valoeghese.scalpel.Window;
import valoeghese.scalpel.util.GLUtils;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Optional;

import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static tk.valoeghese.fc0.client.Client2fc.*;

public class GameScreen extends Screen {
	public GameScreen(Client2fc game) {
		super(game);

		this.crosshair = new MoveableRect(Textures.CROSSHAIR, 0.04f);
		this.crosshair.setPosition(0, 0);

		this.version = this.debugWidget(
				"2fc0f18-" + BrandAndVersion.getVersion().orElse(getInstance().language.translate("gui.version")) + BrandAndVersion.getBrand().orElse(""),
				1.7f);

		this.biomeWidget = this.debugWidget("ecozone.missingno", 1.0f);
		this.coordsWidget = this.debugWidget("missingno", 1.0f);
		this.lightingWidget = this.debugWidget("missingno", 1.0f);
		this.cityWidget = this.debugWidget("missingno", 1.0f);
		this.heightmapWidget = this.debugWidget("missingno", 1.0f);

		this.profilerWidget = new Text("missingno", 0.69420f, 0.92f, FONT_SCALE);

		this.modesWidget = new Text.Moveable("", -0.96f, 0.9f - 0.1f * FONT_SCALE * 1.7f, FONT_SCALE);
		this.kingdomWidget = new Text.Moveable("missingno", 0, 0, 2.0f);
		this.hotbarRenderer = new Hotbar(game.getPlayer().getInventory());
		this.healthBar = new ResizableRect(Textures.HEALTH);
		this.unhealthBar = new ResizableRect(0);
	}

	private final MoveableRect crosshair;
	private final GUI version;
	public final Text biomeWidget;
	public final Text coordsWidget;
	public final Text lightingWidget;
	private final Text cityWidget;
	public final Text heightmapWidget;
	public final Text profilerWidget;
	private float offset;

	private PlaceOfInterest lastPOI;

	private final Text.Moveable kingdomWidget;
	private final ResizableRect healthBar;
	private final ResizableRect unhealthBar;
	private Kingdom currentKingdom;
	private final Text.Moveable modesWidget;
	public Hotbar hotbarRenderer;
	private float kingdomShowTime = 0.0f;
	private boolean[] abilityCaches = new boolean[2];
	private float currentHealthProportions = 0;

	private Text debugWidget(String startText, float size) {
		Text result = new Text(startText, -0.96f, 0.9f + this.offset, FONT_SCALE * size);
		this.offset -= 0.1 * FONT_SCALE;
		return result;
	}

	@Override
	public void renderGUI(float lighting) {
		this.version.render();
		this.profilerWidget.render();
		this.crosshair.render();
		Player player = Client2fc.getInstance().getPlayer();
		float hpPr = (float) player.getHealth() / (float) player.getMaxHealth();

		if (hpPr != this.currentHealthProportions) {
			this.currentHealthProportions = hpPr;
			this.healthBar.setProportions(0f, 0.04f, 0.6f * Math.max(0f, hpPr), 0.04f);
			this.healthBar.setPosition(0.3f, -0.8f);
			this.unhealthBar.setProportions(0.6f * Math.min(1f, (1f - hpPr)), 0.04f, 0f, 0.04f);
			this.unhealthBar.setPosition(0.3f + 0.6f / Client2fc.getInstance().getWindowAspect(), -0.8f);
		}

		this.unhealthBar.render();
		this.healthBar.render();

		if (Client2fc.getInstance().showDebug()) {
			this.biomeWidget.render();
			this.coordsWidget.render();
			this.lightingWidget.render();
			this.cityWidget.render();
			this.heightmapWidget.render();
		}

		if (player.dev != this.abilityCaches[0] || player.isNoClip() != this.abilityCaches[1]) {
			this.abilityCaches[0] = player.dev;
			this.abilityCaches[1] = player.isNoClip();

			StringBuilder next = new StringBuilder();
			boolean first = true;

			for (int i = 0; i < this.abilityCaches.length; ++i) {
				if (this.abilityCaches[i]) {
					if (first) {
						first = false;
					} else {
						next.append(", ");
					}

					switch (i) {
					case 0:
						next.append("[DevMode]");
						break;
					case 1:
						next.append("[NoClip]");
						break;
					}
				}
			}

			this.modesWidget.changeText(next.toString());
		}

		this.modesWidget.render();

		Shaders.gui.uniformFloat("lighting", (lighting - 1.0f) * 0.5f + 1.0f);
		this.hotbarRenderer.render();
		Shaders.gui.uniformFloat("lighting", 1.0f);

		if (this.kingdomShowTime > 0.0f) {
			this.kingdomShowTime -= 0.01f;
			Shaders.gui.uniformFloat("opacity", this.kingdomShowTime);
			GLUtils.enableBlend();
			this.kingdomWidget.render();
			GLUtils.disableBlend();
			Shaders.gui.uniformFloat("opacity", 1.0f);
		}
	}

	public void onShowDebug(boolean showDebug) {
		if (showDebug) {
			this.modesWidget.setOffsets(this.modesWidget.getXOffset(), 0.9f + this.offset);
		} else {
			this.modesWidget.setOffsets(this.modesWidget.getXOffset(), 0.9f - 0.1f * FONT_SCALE * 1.7f);
		}
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
		Camera camera = this.game.getPlayer().getCamera();

		if (Math.abs(dx) > 0.333f) {
			camera.rotateYaw((float) (dx) / 100.0f);
		}

		if (Math.abs(dy) > 0.333f) {
			camera.rotatePitch((float) (dy) / 60.0f);
		}
	}

	public void updateSelected(Inventory inventory) {
		if (Keybinds.SELECT_1.hasBeenPressed()) {
			inventory.setSelectedSlot(0);
		} else if (Keybinds.SELECT_2.hasBeenPressed()) {
			inventory.setSelectedSlot(1);
		} else if (Keybinds.SELECT_3.hasBeenPressed()) {
			inventory.setSelectedSlot(2);
		} else if (Keybinds.SELECT_4.hasBeenPressed()) {
			inventory.setSelectedSlot(3);
		} else if (Keybinds.SELECT_5.hasBeenPressed()) {
			inventory.setSelectedSlot(4);
		} else if (Keybinds.SELECT_6.hasBeenPressed()) {
			inventory.setSelectedSlot(5);
		} else if (Keybinds.SELECT_7.hasBeenPressed()) {
			inventory.setSelectedSlot(6);
		} else if (Keybinds.SELECT_8.hasBeenPressed()) {
			inventory.setSelectedSlot(7);
		} else if (Keybinds.SELECT_9.hasBeenPressed()) {
			inventory.setSelectedSlot(8);
		} else if (Keybinds.SELECT_0.hasBeenPressed()) {
			inventory.setSelectedSlot(9);
		}
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.INVENTORY.hasBeenPressed()) {
			this.game.switchScreen(this.game.craftingScreen);
			return;
		}

		ClientPlayer player = this.game.getPlayer();

		final float yaw = player.getCamera().getYaw();
		float slowness = player.getHorizontalSlowness();
		boolean lr = Keybinds.MOVE_LEFT.isPressed() || Keybinds.MOVE_RIGHT.isPressed();
		boolean fb = Keybinds.MOVE_BACKWARDS.isPressed() || Keybinds.MOVE_FORWARDS.isPressed();

		if (Keybinds.RUN.isPressed()) {
			slowness /= 1.42;
			this.game.sprintFOV(player.getVelocity().squaredLength() > 0.001f ? 1.08f : 1.0f);
		} else {
			this.game.sprintFOV(1.0f);
		}

		if (player.isSwimming()) {
			slowness *= 2;
		}

		// make it so you can't move in two horizontal directions to get extra speed
		if (lr && fb) {
			slowness = org.joml.Math.sqrt(2 * (slowness * slowness));
		}

		if (player.isNoClip()) {
			slowness *= 0.42;
		}

		TilePos below = player.getTilePos().down();

		if (player.getWorld().isInWorld(below)) {
			if (player.getWorld().readTile(below) == Tile.ICE.id) {
				slowness *= 1.5f; // bc more slippery so will get faster anyway
			}
		}

		if (Keybinds.MOVE_BACKWARDS.isPressed()) {
			player.addVelocity(-sin(yaw) / slowness, 0.0f, cos(yaw) / slowness);
		} else if (Keybinds.MOVE_FORWARDS.isPressed()) {
			player.addVelocity(-sin(yaw - PI) / slowness, 0.0f, cos(yaw - PI) / slowness);
		}

		if (Keybinds.MOVE_LEFT.isPressed()) {
			player.addVelocity(-sin(yaw + HALF_PI) / slowness, 0.0f, cos(yaw + HALF_PI) / slowness);
		} else if (Keybinds.MOVE_RIGHT.isPressed()) {
			player.addVelocity(-sin(yaw - HALF_PI) / slowness, 0.0f, cos(yaw - HALF_PI) / slowness);
		}

		if (player.isNoClip()) {
			if (Keybinds.JUMP.isPressed()) {
				player.addVelocity(0.0, 0.1, 0.0);
			} else if (Keybinds.NO_CLIP_DOWN.isPressed()) {
				player.addVelocity(0.0, -0.1, 0.0);
			}
		} else if (Keybinds.JUMP.isPressed()) {
			long time = System.currentTimeMillis();

			if (player.isSwimming() && time > player.lockSwim) {
				player.addVelocity(0.0f, player.getUpwardsSwimStrength() * 0.03f, 0.0f);
			} else {
				player.lockSwim = time + 18;

				if (player.isOnGround()) {
					player.addVelocity(0.0f, player.getJumpStrength(), 0.0f);
				}
			}
		}

		Inventory inventory = player.getInventory();
		this.updateSelected(inventory);
		Item selectedItem = inventory.getSelectedItem();
		GameplayWorld<?> world = this.game.getWorld();

		if (Keybinds.DESTROY.hasBeenPressed()) {
			TilePos pos = player.rayCast(10.0, false).pos;

			if (world.isInWorld(pos)) {
				byte tileId = world.readTile(pos);

				if (tileId != Tile.WATER.id) {
					Tile tile = Tile.BY_ID[tileId];
					this.game.playSound(null, tile.getSounds().getBreakSound(), pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 1.0f);

					if (!player.dev && tile.shouldRender()) {
						inventory.addItem(tile.getDrop(RANDOM, world.readMeta(pos)));
						world.writeTile(pos, Tile.AIR.id);
					} else if (player.dev) {
						world.writeTile(pos, Tile.AIR.id);
					}
				}
			}
		}

		if (Keybinds.INTERACT.hasBeenPressed()) {
			if (selectedItem != null) {
				if (selectedItem.isTile()) {
					RaycastResult result = player.rayCast(10.0, true);

					if (result.face != null) {
						TilePos pos = result.face.apply(result.pos);
						TilePos playerPos = player.getNextTilePos();

						if (!pos.equals(playerPos) && !pos.equals(playerPos.up())) { // stop player from placing blocks on themself
							if (world.isInWorld(pos)) {
								Tile tile = selectedItem.tileValue();

								if (tile.canPlaceAt(world, pos.x, pos.y, pos.z)) {
									this.game.playSound(null, tile.getSounds().getPlaceSound(), pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 1.0f);

									if (!player.dev) {
										selectedItem.decrement();
									}

									if (player.dev && tile.id == Tile.DAISY.id && world.readTile(pos.down()) == Tile.SAND.id) {
										world.writeTile(pos, Tile.CACTUS.id);
									} else {
										world.writeTile(pos, tile.id);
										world.writeMeta(pos.x, pos.y, pos.z, selectedItem.getMeta());
									}

									tile.onPlace(world, pos);
								}
							}
						}
					}
				} else {
					selectedItem.onItemUse(player);
				}
			}
		}

		// respawn if fall out of world
		if (player.getTilePos().y < -20) {
			player.setPos(this.game.spawnLoc);
		}

		if (Keybinds.NO_CLIP.hasBeenPressed() && Client2fc.getInstance().allowsNoClip()) {
			player.setNoClip(!player.isNoClip());
		}

		if (player.dev) {
			if (Keybinds.DEV_ITEMS.hasBeenPressed()) {
				player.addDevItems();
			}

			if (Keybinds.ADD_TIME.isPressed()) {
				this.game.time += 200;
			}

			if (Keybinds.REMOVE_TIME.isPressed()) {
				this.game.time -= 200;
			}

			if (Keybinds.HIDE_WORLD.hasBeenPressed()) {
				this.game.renderWorld = !this.game.renderWorld;
			}
 		}

		if (Keybinds.DEV_MODE.hasBeenPressed() && Client2fc.getInstance().allowsDev()) {
			player.toggleDev();
		}

		if (Keybinds.HIDE_DEBUG.hasBeenPressed()) {
			Client2fc i = Client2fc.getInstance();
			i.setShowDebug(!i.showDebug());
		}
	}

	@Override
	public Optional<MusicSettings> getMusic() {
		ChunkPos playerChunkPos = this.game.getPlayer().getTilePos().toChunkPos();
		return this.game.getWorld().getChunk(playerChunkPos.x, playerChunkPos.z) == null ? Optional.empty() : GAME_MUSIC;
	}

	@Override
	public void handleEscape(Window window) {
		this.game.switchScreen(this.game.pauseScreen);
	}

	@Override
	public void onFocus() {
		GLUtils.disableMouse(Client2fc.getInstance().getWindowId());
	}

	@Nullable
	public Kingdom getCurrentKingdom() {
		return this.currentKingdom;
	}

	/**
	 * Recalculates the stored POI.
	 * @return if the POI changed.
	 */
	public boolean updatePOI() {
		TilePos position = this.game.getPlayer().getTilePos();
		PlaceOfInterest location = CityGenerator.isInCity(this.game.getWorld(), position.x, position.z, Generator.OVERWORLD_CITY_SIZE) ? PlaceOfInterest.CITY : PlaceOfInterest.KINGDOM;
		boolean result = this.lastPOI != location;
		this.lastPOI = location;
		return result;
	}

	public void setCurrentKingdom(@Nullable Kingdom kingdom) {
		this.currentKingdom = kingdom;

		if (kingdom != null) {
			this.cityWidget.changeText(kingdom.debugString());
		}
	}

	public void showPOIWidget() {
		if (this.currentKingdom != null) {
			String text = switch (this.lastPOI) {
				case KINGDOM -> this.currentKingdom.toString();
				case CITY -> this.currentKingdom.getName() + " City";
				default ->  "Missingno";
			};

			this.kingdomWidget.changeText(text, -Text.widthOf(text.toCharArray()), 0.7f);
			this.kingdomShowTime = 1.0f;
		}
	}

	private static List<MusicPiece> pickMusic() {
		Client2fc game = Client2fc.getInstance();
		TilePos position = game.getPlayer().getTilePos();

		if (CityGenerator.isInCity(game.getWorld(), position.x, position.z, Generator.OVERWORLD_CITY_SIZE)) {
			return TOWN_MUSIC;
		} else {
			return GRASSLAND_MUSIC;
		}
	}

	private static final List<MusicPiece> TOWN_MUSIC = List.of(MusicPiece.TOWN_CLAV, MusicPiece.TOWN_HARPSICHORD);
	private static final List<MusicPiece> GRASSLAND_MUSIC = List.of(MusicPiece.FOREST_RILL);
	public static final Optional<MusicSettings> GAME_MUSIC = Optional.of(new MusicSettings(GameScreen::pickMusic, 600 + 300, 5 * 600 + 300, 0.4f));

	private static final float FONT_SCALE = 0.67f;

	private enum PlaceOfInterest {
		KINGDOM,
		CITY
	}
}
