package tk.valoeghese.fc0.client.render.screen;

import org.joml.Math;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Shaders;
import tk.valoeghese.fc0.client.render.gui.Crosshair;
import tk.valoeghese.fc0.client.render.gui.GUI;
import tk.valoeghese.fc0.client.render.gui.Text;
import tk.valoeghese.fc0.client.render.gui.collection.Hotbar;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import tk.valoeghese.fc0.client.world.ClientWorld;
import tk.valoeghese.fc0.util.RaycastResult;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.tile.Tile;
import valoeghese.scalpel.Camera;
import valoeghese.scalpel.Window;
import valoeghese.scalpel.util.GLUtils;

import javax.annotation.Nullable;

import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static tk.valoeghese.fc0.client.Client2fc.*;

public class GameScreen extends Screen {
	public GameScreen(Client2fc game) {
		super(game);

		this.version = new Text(getInstance().language.translate("gui.version"), -0.92f, 0.9f, 1.7f);
		this.crosshair = new Crosshair();
		this.biomeWidget = new Text("ecozone.missingno", -0.92f, 0.78f, 1.0f);
		this.coordsWidget = new Text("missingno", -0.92f, 0.68f, 1.0f);
		this.lightingWidget = new Text("missingno", -0.92f, 0.58f, 1.0f);
		this.cityWidget = new Text("missingno", -0.92f, 0.48f, 1.0f);
		this.modesWidget = new Text.Moveable("", -0.92f, 0.78f, 1.0f);
		this.kingdomWidget = new Text.Moveable("missingno", 0, 0, 2.0f);
		this.hotbarRenderer = new Hotbar(game.getPlayer().getInventory());
	}

	private final GUI crosshair;
	private final GUI version;
	public final Text biomeWidget;
	public final Text coordsWidget;
	public final Text lightingWidget;
	private final Text cityWidget;
	private final Text.Moveable kingdomWidget;
	private Kingdom currentKingdom;
	private final Text.Moveable modesWidget;
	public Hotbar hotbarRenderer;
	private float kingdomShowTime = 0.0f;
	private boolean[] abilityCaches = new boolean[2];

	@Override
	public void renderGUI(float lighting) {
		this.version.render();
		this.crosshair.render();

		if (Client2fc.getInstance().showDebug()) {
			this.biomeWidget.render();
			this.coordsWidget.render();
			this.lightingWidget.render();
			this.cityWidget.render();
		}

		Player player = Client2fc.getInstance().getPlayer();

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
			this.modesWidget.setOffsets(this.modesWidget.getXOffset(), 0.38f);
		} else {
			this.modesWidget.setOffsets(this.modesWidget.getXOffset(), 0.78f);
		}
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
		Camera camera = this.game.getPlayer().getCamera();

		if (Math.abs(dx) > 1.5f) {
			camera.rotateYaw((float) (dx) / 100.0f);
		}

		if (Math.abs(dy) > 1.5f) {
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
			slowness /= 1.48; // nerfed from previous versions
		}

		if (player.isSwimming()) {
			slowness *= 2;
		}

		// make it so you can't move in two horizontal directions to get extra speed
		if (lr && fb) {
			slowness = org.joml.Math.sqrt(2 * (slowness * slowness));
		}

		if (player.isNoClip()) {
			slowness *= 0.3;
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
				player.addVelocity(0.0, 0.02, 0.0);
			} else if (Keybinds.NO_CLIP_DOWN.isPressed()) {
				player.addVelocity(0.0, -0.02, 0.0);
			}
		} else {
			if (Keybinds.JUMP.isPressed()) {
				long time = System.currentTimeMillis();

				if (player.isSwimming() && time > player.lockSwim) {
					player.addVelocity(0.0f, player.getJumpStrength() * 0.03f, 0.0f);
				} else {
					player.lockSwim = time + 18;

					if (player.isOnGround()) {
						player.addVelocity(0.0f, player.getJumpStrength(), 0.0f);
					}
				}
			}
		}

		Inventory inventory = player.getInventory();
		this.updateSelected(inventory);
		Item selectedItem = inventory.getSelectedItem();
		GameplayWorld<?> world = this.game.getWorld();

		if (Keybinds.DESTROY.hasBeenPressed()) {
			TilePos pos = player.rayCast(10.0).pos;

			if (world.isInWorld(pos)) {
				byte tileId = world.readTile(pos);

				if (tileId != Tile.WATER.id) {
					Tile tile = Tile.BY_ID[tileId];

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
			if (selectedItem != null && selectedItem.isTile()) {
				RaycastResult result = player.rayCast(10.0);

				if (result.face != null) {
					TilePos pos = result.face.apply(result.pos);

					if (world.isInWorld(pos)) {
						Tile tile = selectedItem.tileValue();

						if (tile.canPlaceAt(world, pos.x, pos.y, pos.z)) {
							if (!player.dev) {
								selectedItem.decrement();
							}

							if (player.dev && tile.id == Tile.DAISY.id && world.readTile(pos.down()) == Tile.SAND.id) {
								world.writeTile(pos, Tile.CACTUS.id);
							} else {
								world.writeTile(pos, tile.id);
							}

							tile.onPlace(world, pos);
						}
					}
				}
			}
		}

		if (Keybinds.RESPAWN.hasBeenPressed() || player.getTilePos().y < -20) {
			player.setPos(this.game.spawnLoc);
		}

		if (Keybinds.SET_SPAWN.hasBeenPressed()) {
			this.game.spawnLoc = player.getPos();
		}

		if (Keybinds.NO_CLIP.hasBeenPressed()) {
			player.setNoClip(!player.isNoClip());
		}

		if (player.dev) {
			if (Keybinds.DEV_ITEMS.hasBeenPressed()) {
				player.addDevItems();
			}

			if (Keybinds.ADD_TIME.hasBeenPressed()) {
				Client2fc.getInstance().time += 200;
			}
		}

		if (Keybinds.DEV_MODE.hasBeenPressed()) {
			player.toggleDev();
		}

		if (Keybinds.HIDE_DEBUG.hasBeenPressed()) {
			Client2fc i = Client2fc.getInstance();
			i.setShowDebug(!i.showDebug());
		}
	}

	@Override
	public void handleEscape(Window window) {
		this.game.saveWorld();
		this.game.getWorld().destroy();
		this.game.save = null;

		ClientWorld world = new ClientWorld(null, 0, Client2fc.TITLE_WORLD_SIZE);
		this.game.setWorld(world);
		ClientPlayer player = this.game.getPlayer();
		player.changeWorld(world, this.game.save);
		player.getCamera().setPitch(0);
		player.getCamera().setYaw(PI);

		if (NEW_TITLE) {
			player.setNoClip(true);
			player.move(0, 20, 0);
		}

		this.game.switchScreen(this.game.titleScreen);
	}

	@Override
	public void onFocus() {
		GLUtils.disableMouse(Client2fc.getInstance().getWindowId());
	}

	@Nullable
	public Kingdom getCurrentKingdom() {
		return this.currentKingdom;
	}

	public void setCurrentKingdom(Kingdom kingdom) {
		this.currentKingdom = kingdom;
		this.cityWidget.changeText(kingdom.debugString());

		String text = kingdom.toString();
		this.kingdomWidget.changeText(text, Text.widthOf(text.toCharArray()) * -1f, 0.7f);
		this.kingdomShowTime = 1.0f;
	}
}
