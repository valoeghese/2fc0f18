package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.ScatteredOreGenerator;
import tk.valoeghese.fc0.world.tile.Tile;

public class DesertZone extends EcoZone {
	DesertZone() {
		super("desert", Tile.SAND, Tile.SAND);

		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(3, 5, Tile.CACTUS));
	}
}
