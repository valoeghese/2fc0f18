package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public class GroundFoliageGeneratorSettings extends RandomCountGeneratorSettings {
	public GroundFoliageGeneratorSettings(int min, int max, Tile... tiles) {
		super(min, max);
		this.tiles = tiles;
	}

	private final Tile[] tiles;

	public Tile pickTile(Random rand) {
		return this.tiles[rand.nextInt(tiles.length)];
	}
}
