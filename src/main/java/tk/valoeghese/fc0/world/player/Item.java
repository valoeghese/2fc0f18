package tk.valoeghese.fc0.world.player;

import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;

public class Item {
	public Item(Tile tile) {
		this(tile, 0);
	}

	public Item(IngredientItem item) {
		this.tileValue = null;
		this.ingredientItemValue = item;
		this.tile = false;
		this.meta = (byte) 0;
	}

	public Item(Tile tile, int meta) {
		this.tileValue = tile;
		this.ingredientItemValue = null;
		this.tile = true;
		this.meta = (byte) meta;
	}

	public Item(Item other) {
		this(other.id(), other.meta, other.count);
	}

	public Item(int id, byte meta, int count) {
		this.tileValue = id < 256 ? Tile.BY_ID[id] : null;
		this.ingredientItemValue = id > 255 ? IngredientItem.BY_ID[id] : null;
		this.tile = id < 256;
		this.meta = meta;
		this.count = count;
	}

	private final boolean tile;
	@Nullable
	private final Tile tileValue;
	@Nullable
	private final IngredientItem ingredientItemValue;
	private final byte meta;
	private int count = 1;
	@Nullable
	public Inventory inventory;
	public int invSlot = 0;

	public boolean isTile() {
		return this.tile;
	}

	@Nullable
	public Tile tileValue() {
		return this.tileValue;
	}

	@Nullable
	public IngredientItem ingredientItemValue() {
		return this.ingredientItemValue;
	}

	public byte getMeta() {
		return this.meta;
	}

	public void setCount(int count) {
		this.count = count;

		if (this.inventory != null) {
			this.inventory.refresh(this.invSlot, this);
		}
	}

	public int getCount() {
		return this.count;
	}

	public void decrement() {
		this.count--;

		if (this.inventory != null) {
			this.inventory.refresh(this.invSlot, this);
		}
	}

	public void increment() {
		this.increment(1);
	}

	public void increment(int increase) {
		this.count += increase;

		if (this.inventory != null) {
			this.inventory.refresh(this.invSlot, this);
		}
	}

	public void setInventory(@Nullable Inventory inventory, int slot) {
		this.inventory = inventory;
		this.invSlot = slot;
	}

	public int id() {
		if (this.tile) {
			return this.tileValue.id;
		} else {
			return this.ingredientItemValue.id + 256;
		}
	}

	public String[] translationStringArray() {
		if (this.tile) {
			return new String[] {this.tileValue.toString(), String.valueOf(this.count)};
		} else {
			return new String[] {this.ingredientItemValue.toString(), String.valueOf(this.count)};
		}
	}
}
