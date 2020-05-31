package tk.valoeghese.fc0.world.player;

import javax.annotation.Nullable;

public class Inventory {
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
		return this.items[slot];
	}

	public void putItemAt(int slot, @Nullable Item item) throws ArrayIndexOutOfBoundsException {
		this.items[slot] = item;

		if (item == null) {
			if (slot < this.freeSlot) {
				this.freeSlot = slot;
			}
		} else {
			if (slot == this.freeSlot) {
				while (this.freeSlot < this.size && this.items[this.freeSlot] != null) {
					++this.freeSlot;
				}
			}
		}
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
}
