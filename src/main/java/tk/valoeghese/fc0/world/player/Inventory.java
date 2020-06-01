package tk.valoeghese.fc0.world.player;

import javax.annotation.Nullable;
import java.util.Iterator;

public class Inventory implements Iterable<Item> {
	public Inventory(int size) {
		this.items = new Item[size];
		this.size = size;
	}

	private final Item[] items;
	private final int size;
	private int freeSlot = 0;
	private int selectedSlot;

	@Nullable
	public Item getSelectedItem() throws ArrayIndexOutOfBoundsException {
		return this.getItemAt(this.selectedSlot);
	}

	@Nullable
	public Item getItemAt(int slot) throws ArrayIndexOutOfBoundsException {
		Item result = this.items[slot];

		if (result == null) {
			return null;
		}

		if (result.getCount() <= 0) {
			this.putItemAt(slot, null);
			return null;
		}

		return result;
	}

	public void putItemAt(int slot, @Nullable Item item) throws ArrayIndexOutOfBoundsException {
		Item currentItem = this.items[slot];

		if (currentItem != null) {
			currentItem.setInventory(null, 0);
		}

		this.items[slot] = item;

		if (item == null) {
			if (slot < this.freeSlot) {
				this.freeSlot = slot;
			}
		} else {
			item.setInventory(this, slot);

			if (slot == this.freeSlot) {
				while (this.freeSlot < this.size && this.items[this.freeSlot] != null) {
					++this.freeSlot;
				}
			}
		}
	}

	protected void refresh(int slot, Item item) {
	}

	public boolean addItem(@Nullable Item item) {
		if (item == null) {
			return true;
		}

		for (int i = 0; i < this.size; ++i) {
			Item currentItem = this.items[i];

			if (currentItem != null) {
				if (currentItem.id() == item.id()) {
					currentItem.increment(item.getCount());
					return true;
				}
			}
		}

		int slot = this.getNextFreeSlot();

		if (slot != -1) {
			this.putItemAt(slot, item);
			return true;
		}

		return false;
	}

	public void setSelectedSlot(int slot) {
		this.selectedSlot = slot;
	}

	public int getSelectedSlot() {
		return this.selectedSlot;
	}

	/**
	 * @return -1 if the inventory is full, otherwise the first free slot.
	 */
	public int getNextFreeSlot() {
		return this.freeSlot < this.size ? this.freeSlot : -1;
	}

	public int getSize() {
		return this.size;
	}

	@Override
	public Iterator<Item> iterator() {
		return new InventoryIterator(this.items);
	}

	public void reset() {
		for (int i = 0; i < this.size; ++i) {
			this.putItemAt(i, null);
		}
	}

	private static class InventoryIterator implements Iterator<Item> {
		public InventoryIterator(Item[] items) {
			this.items = new Item[items.length];
			System.arraycopy(items, 0, this.items, 0, items.length);
		}

		private Item[] items;
		private int index = 0;

		@Override
		public boolean hasNext() {
			return this.index < this.items.length;
		}

		@Override
		public Item next() {
			return this.items[this.index++];
		}
	}
}
