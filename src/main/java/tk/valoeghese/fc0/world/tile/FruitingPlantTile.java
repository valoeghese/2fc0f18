package tk.valoeghese.fc0.world.tile;

import tk.valoeghese.fc0.world.player.Item;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;

public class FruitingPlantTile extends PlantTile {
	public FruitingPlantTile(String textureName, int id, float iota, Predicate<Tile> support, Item drop) {
		super(textureName, id, iota, support);
		this.drop = drop;
	}

	private final Item drop;

	@Nullable
	@Override
	public Item getDrop(Random rand, byte meta) {
		return rand.nextInt(3) == 0 ? this.drop : null;
	}
}
