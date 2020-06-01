package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.system.gui.GUICollection;
import tk.valoeghese.fc0.client.render.system.gui.PseudoGUI;
import tk.valoeghese.fc0.world.player.CraftingManager;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;

import javax.annotation.Nullable;
import java.util.List;

public class CraftingMenu extends GUICollection<CraftingMenu.Craftable> implements Hotbar.HotbarUpdateSubscriber {
	public CraftingMenu(Inventory parent) {
		this.parent = parent;
	}

	private final Inventory parent;
	private boolean focused = false;

	public boolean isFocused() {
		return this.focused;
	}

	@Nullable
	public Item getItemToCraft(float x, float y) {
		for (Craftable craftable : this.guis) {
			if (craftable.isCursorSelecting(x, y)) {
				return craftable.output;
			}
		}

		return null;
	}

	public void setFocus(boolean focused) {
		this.focused = focused;

		if (focused) {
			this.addCraftables();
		}
	}

	@Override
	public void onUpdate(int slot, float windowAspect) {
		if (this.focused) {
			this.addCraftables();
		}
	}

	private void addCraftables() {
		this.destroy();
		Item ingredient = this.parent.getSelectedItem();

		if (ingredient == null) {
			return;
		}

		List<Item> craftables = CraftingManager.CRAFTING.getCraftables(ingredient.id());

		if (craftables != null) {
			for (int i = 0; i < craftables.size(); ++i) {
				Item craftable = craftables.get(i);

				Craftable rendered = new Craftable(craftable, -0.8f, 0.87f - (0.14f * i));
				String[] rawTranslationKeys = craftable.asStringArray();
				rendered.nameGUI.changeText(Client2fc.getInstance().language.translate(rawTranslationKeys[0]) + " x" + rawTranslationKeys[1]);

				if (craftable.isTile()) {
					rendered.itemGUI.setTile(craftable.tileValue(), craftable.getMeta(), 1f / Client2fc.getInstance().getWindowAspect());
				}

				this.guis.add(rendered);
			}
		}
	}

	static class Craftable implements PseudoGUI {
		public Craftable(Item output, float xOffset, float yOffset) {
			final float buttonXOff = 0.56f;

			this.itemGUI = new ItemGUI(xOffset, yOffset, 0.06f);
			this.nameGUI = new Text("tile.missingno", xOffset + 0.14f, yOffset - 0.02f, 0.8f);
			this.button = new MoveableSquare(Textures.CRAFT, 0.06f);
			this.button.setPosition(xOffset + buttonXOff, yOffset);

			float aspect = 1f / Client2fc.getInstance().getWindowAspect();
			this.x0 = xOffset + buttonXOff - aspect * 0.06f;
			this.y0 = yOffset - 0.06f;
			this.x1 = xOffset + buttonXOff + aspect * 0.06f;
			this.y1 = yOffset + 0.06f;
			this.output = output;
		}

		final ItemGUI itemGUI;
		final Text nameGUI;
		final MoveableSquare button;
		final float x0;
		final float x1;
		final float y0;
		final float y1;
		final Item output;

		public boolean isCursorSelecting(float x, float y) {
			if (y > this.y0 && y < this.y1) {
				if (x > this.x0 && x < this.x1) {
					return true;
				}
			}

			return false;
		}

		@Override
		public void render() {
			this.itemGUI.render();
			this.nameGUI.render();
			this.button.render();
		}

		@Override
		public void destroy() {
			this.itemGUI.destroy();
			this.nameGUI.destroy();
			this.button.destroy();
		}
	}
}
