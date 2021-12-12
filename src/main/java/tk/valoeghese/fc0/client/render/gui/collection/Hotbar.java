package tk.valoeghese.fc0.client.render.gui.collection;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.ItemGUI;
import tk.valoeghese.fc0.client.render.gui.MoveableRect;
import tk.valoeghese.fc0.client.render.gui.Text;
import valoeghese.scalpel.gui.GUICollection;
import valoeghese.scalpel.gui.PseudoGUI;
import tk.valoeghese.fc0.world.player.Inventory;
import tk.valoeghese.fc0.world.player.Item;

import java.util.ArrayList;
import java.util.List;

public class Hotbar extends GUICollection<Hotbar.HotbarEntry> {
	public Hotbar(Inventory parent) {
		this.parent = parent;

		for (int i = 0; i < 10; ++i) {
			this.guis.add(new HotbarEntry(0.8f, 0.75f - (0.14f * i)));
			this.update(i, Client2fc.getInstance().getWindowAspect());
		}

		this.selected = new MoveableRect(Textures.SELECTED, 0.08f);
		this.selected.setPosition(0.8f, 0.87f);
	}

	private final Inventory parent;
	public boolean updating = false;
	public final MoveableRect selected;

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
		float windowAspect = Client2fc.getInstance().getWindowAspect();

		for (HotbarUpdateSubscriber subscriber : UPDATE_SUBSCRIBERS) {
			subscriber.onUpdate(slot, windowAspect);
		}
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
			} else {
				entry.itemGUI.setIngredientItem(item.ingredientItemValue(), 1f / windowAspect);
			}
		}

		for (HotbarUpdateSubscriber subscriber : UPDATE_SUBSCRIBERS) {
			subscriber.onUpdate(slot, windowAspect);
		}

		this.updating = false;
	}

	public static void addUpdateSubscriber(HotbarUpdateSubscriber subscriber) {
		UPDATE_SUBSCRIBERS.add(subscriber);
	}

	private static final List<HotbarUpdateSubscriber> UPDATE_SUBSCRIBERS = new ArrayList<>();

	public interface HotbarUpdateSubscriber {
		void onUpdate(int slot, float windowAspect);
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
