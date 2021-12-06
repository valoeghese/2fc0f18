package tk.valoeghese.fc0.world.player;

public class Food extends ItemType {
	public Food(String textureName, int id) {
		super(textureName, id);
	}

	private int health = 0;

	public Food heals(int health) {
		this.health = health;
		return this;
	}

	@Override
	protected void onItemUse(Item item, Player player) {
		player.heal(this.health);
		if (!player.dev) item.decrement();
	}
}
