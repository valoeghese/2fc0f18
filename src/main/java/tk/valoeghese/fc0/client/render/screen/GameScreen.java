package tk.valoeghese.fc0.client.render.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.Shaders;
import tk.valoeghese.fc0.client.render.gui.Crosshair;
import tk.valoeghese.fc0.client.render.gui.collection.Hotbar;
import tk.valoeghese.fc0.client.render.gui.Text;
import tk.valoeghese.fc0.client.render.system.Camera;
import tk.valoeghese.fc0.client.render.system.Window;
import tk.valoeghese.fc0.client.render.system.gui.GUI;
import tk.valoeghese.fc0.client.render.system.util.GLUtils;
import tk.valoeghese.fc0.client.world.ClientPlayer;
import tk.valoeghese.fc0.client.world.ClientWorld;
import tk.valoeghese.fc0.util.RaycastResult;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;
import tk.valoeghese.fc0.world.tile.Tile;

import static org.joml.Math.cos;
import static org.joml.Math.sin;
import static tk.valoeghese.fc0.client.Client2fc.*;

public class GameScreen extends Screen {
	public GameScreen(Client2fc game) {
		super(game);

		this.version = new Text("2fc0f18-v0.2.2", -0.92f, 0.9f, 1.7f);
		this.crosshair = new Crosshair();
		this.biomeWidget = new Text("ecozone.missingno", -0.92f, 0.78f, 1.0f);
		this.hotbarRenderer = new Hotbar(game.getPlayer().getInventory());
	}

	private final GUI crosshair;
	private final GUI version;
	public final Text biomeWidget;
	public Hotbar hotbarRenderer;

	@Override
	public void renderGUI(float lighting) {
		this.version.render();
		this.crosshair.render();
		this.biomeWidget.render();

		Shaders.gui.uniformFloat("lighting", (lighting - 1.0f) * 0.5f + 1.0f);
		this.hotbarRenderer.render();
		Shaders.gui.uniformFloat("lighting", 1.0f);
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
			slowness /= 1.67;
		}

		if (player.isSwimming()) {
			slowness *= 2;
		}

		// make it so you can't move in two horizontal directions to get extra speed
		if (lr && fb) {
			slowness = org.joml.Math.sqrt(2 * (slowness * slowness));
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
	}

	@Override
	public void handleEscape(Window window) {
		this.game.saveWorld();
		this.game.getWorld().destroy();
		this.game.save = null;

		ClientWorld world = new ClientWorld(null, 0, 4);
		this.game.setWorld(world);
		this.game.getPlayer().changeWorld(world, this.game.save);
		this.game.switchScreen(this.game.titleScreen);
	}

	@Override
	public void onFocus() {
		GLUtils.disableMouse(Client2fc.getInstance().getWindowId());
	}
}
