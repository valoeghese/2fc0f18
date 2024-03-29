package tk.valoeghese.fc0.world.player;

import tk.valoeghese.fc0.util.maths.Vec2i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemType {
	public ItemType(String textureName, int id) {
		this.id = id;

		if (id != -1) {
			BY_ID[id] = this;
		}

		ITEMS.add(this);

		this.textureName = textureName;
	}

	public final int id;
	private String translationKey = "item.missingno";
	public final String textureName;
	private int u = 0;
	private int v = 0;

	protected void onItemUse(Item item, Player player) {
	}

	public final void requestUV(Function<String, Vec2i> uvSupplier) {
		Vec2i uv = uvSupplier.apply(this.textureName);
		this.u = uv.getX();
		this.v = uv.getY();
	}

	public int getU() {
		return this.u;
	}

	public int getV() {
		return this.v;
	}

	protected ItemType setName(String name) {
		this.translationKey = "item." + name;
		return this;
	}

	@Override
	public String toString() {
		return this.translationKey;
	}

	public static final List<ItemType> ITEMS = new ArrayList<>();
	public static final ItemType[] BY_ID = new ItemType[128];
	// An ID of negative one symbolises a delegate item.
	public static final ItemType TORCH = new ItemType("torch", -1).setName("torch");
	public static final ItemType POMELO = new Food("pomelo", 0).heals(5).setName("pomelo");
}
