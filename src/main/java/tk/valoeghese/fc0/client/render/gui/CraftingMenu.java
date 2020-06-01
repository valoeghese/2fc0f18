package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.system.gui.GUICollection;
import tk.valoeghese.fc0.client.render.system.gui.PseudoGUI;
import tk.valoeghese.fc0.world.player.CraftingManager;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;

import java.util.List;

public class CraftingMenu extends GUICollection<CraftingMenu.Craftable> {
	public CraftingMenu(Inventory parent) {
		this.parent = parent;
	}

	private final Inventory parent;

	public void addCraftables() {
		this.destroy();

		Item ingredient = this.parent.getSelectedItem();
		List<Item> craftables = CraftingManager.CRAFTING.getCraftables(ingredient.id());

		if (craftables != null) {
			for (int i = 0; i < craftables.size(); ++i) {
				Item craftable = craftables.get(i);

				Craftable rendered = new Craftable(0.8f, 0.87f - (0.14f * i));
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
		public Craftable(float xOffset, float yOffset) {
			this.itemGUI = new ItemGUI(xOffset, yOffset, 0.06f);
			this.nameGUI = new Text("tile.missingno", xOffset + 0.14f, yOffset - 0.02f, 0.8f);
		}

		final ItemGUI itemGUI;
		final Text nameGUI;

		@Override
		public void render() {
			this.itemGUI.render();
			this.nameGUI.render();
		}

		@Override
		public void destroy() {
			this.itemGUI.destroy();
			this.nameGUI.destroy();
		}
	}
}
