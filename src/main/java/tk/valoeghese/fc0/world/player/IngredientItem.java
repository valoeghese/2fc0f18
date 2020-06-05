package tk.valoeghese.fc0.world.player;

import tk.valoeghese.fc0.util.maths.Vec2i;

import java.util.function.Function;

public class IngredientItem {
	public IngredientItem(String textureName, int id) {
		this.id = id;
		BY_ID[id] = this;
		this.textureName = textureName;
	}

	public final int id;
	private String translationKey = "item.missingno";
	public final String textureName;
	private int u = 0;
	private int v = 0;

	public final void requestUV(Function<String, Vec2i> uvSupplier) {
		Vec2i uv = uvSupplier.apply(this.translationKey);
		this.u = uv.getX();
		this.v = uv.getY();
	}

	public int getU() {
		return this.u;
	}

	public int getV() {
		return this.v;
	}

	protected IngredientItem setName(String name) {
		this.translationKey = "item." + name;
		return this;
	}

	@Override
	public String toString() {
		return this.translationKey;
	}

	public static final IngredientItem[] BY_ID = new IngredientItem[128];
	public static final IngredientItem POMELO = new IngredientItem("pomelo", 0).setName("pomelo");
}
