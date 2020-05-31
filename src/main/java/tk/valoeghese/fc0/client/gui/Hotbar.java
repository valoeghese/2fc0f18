package tk.valoeghese.fc0.client.gui;

import tk.valoeghese.fc0.client.system.gui.GUICollection;
import tk.valoeghese.fc0.client.system.gui.PseudoGUI;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;

public class Hotbar extends GUICollection<Hotbar.HotbarEntry> {
	public Hotbar(Inventory parent) {
		for (int i = 0; i < 9; ++i) {
			this.guis.add(new HotbarEntry(0.8f, 0.8f - (0.1f * i)));
			this.disable(i);
		}

		this.parent = parent;
	}

	private final Inventory parent;

	public void update(int slot, float windowAspect) {
		Item item = this.parent.getItemAt(slot);

		if (item == null) {
			this.disable(slot);
			return;
		}

		this.enable(slot);
		HotbarEntry entry = this.guis.get(slot);
		entry.countGUI.changeText(String.valueOf(item.getCount()));

		if (item.isTile()) {
			entry.tileGUI.setTile(item.tileValue(), item.getMeta(), 1f / windowAspect);
		}
	}

	static class HotbarEntry implements PseudoGUI {
		public HotbarEntry(float xOffset, float yOffset) {
			this.tileGUI = new TileGUI(xOffset, yOffset, 0.1f);
			this.countGUI = new Text("0", xOffset - 0.14f, yOffset, 0.3f);
		}

		final TileGUI tileGUI;
		final Text countGUI;

		@Override
		public void render() {
			this.tileGUI.render();
			this.countGUI.render();
		}

		@Override
		public void destroy() {
			this.tileGUI.destroy();
			this.countGUI.destroy();
		}
	}
}
