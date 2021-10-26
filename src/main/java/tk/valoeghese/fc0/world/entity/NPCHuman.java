package tk.valoeghese.fc0.world.entity;

import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.LoadableWorld;
import tk.valoeghese.fc0.world.player.Inventory;

public class NPCHuman extends Lifeform {
	public NPCHuman(GameplayWorld world, float height, Inventory inventory) {
		super(world, height, inventory);
	}
}
