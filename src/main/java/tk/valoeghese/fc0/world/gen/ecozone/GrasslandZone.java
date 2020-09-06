package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.ScatteredOreGenerator;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

public class GrasslandZone extends EcoZone {
	GrasslandZone() {
		super("grassland");

		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(14, 20, Tile.DAISY, Tile.DANDELION, Tile.TALLGRASS, Tile.TALLGRASS, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(0, 1.5f));
	}
}
