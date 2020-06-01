package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.model.Textures;
import tk.valoeghese.fc0.client.render.system.gui.GUICollection;
import tk.valoeghese.fc0.client.render.system.gui.PseudoGUI;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;

public class Hotbar extends GUICollection<Hotbar.HotbarEntry> {
	public Hotbar(Inventory parent) {
		this.parent = parent;

		for (int i = 0; i < 10; ++i) {
			this.guis.add(new HotbarEntry(0.8f, 0.87f - (0.14f * i)));
			this.update(i, Client2fc.getInstance().getWindowAspect());
		}

		this.selected = new MoveableSquare(Textures.SELECTED, Client2fc.getInstance().getWindowAspect(), 0.08f);
		this.selected.setPosition(0.8f, 0.87f);
	}

	private final Inventory parent;
	public boolean updating = false;
	public final MoveableSquare selected;

	@Override
	public void render() {
		super.render();
		this.selected.render();
	}

	@Override
	public void destroy() {
		super.destroy();
		this.selected.destroy();
	}

	public void setSelectedSlot(int slot) {
		this.selected.setPosition(0.8f, 0.87f - (0.14f * slot));
	}

	public void update(int slot, float windowAspect) {
		this.updating = true;
		Item item = this.parent.getItemAt(slot);

		if (item == null) {
			this.disable(slot);
		} else {
			this.enable(slot);
			HotbarEntry entry = this.guis.get(slot);
			entry.countGUI.changeText(String.valueOf(item.getCount()));

			if (item.isTile()) {
				entry.itemGUI.setTile(item.tileValue(), item.getMeta(), 1f / windowAspect);
			}
		}

		this.updating = false;
	}

	static class HotbarEntry implements PseudoGUI {
		public HotbarEntry(float xOffset, float yOffset) {
			this.itemGUI = new ItemGUI(xOffset, yOffset, 0.06f);
			this.countGUI = new Text("0", xOffset - 0.14f, yOffset - 0.02f, 0.8f);
		}

		final ItemGUI itemGUI;
		final Text countGUI;

		@Override
		public void render() {
			this.itemGUI.render();
			this.countGUI.render();
		}

		@Override
		public void destroy() {
			this.itemGUI.destroy();
			this.countGUI.destroy();
		}
	}
}
