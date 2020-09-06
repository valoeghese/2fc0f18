package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.ScatteredOreGenerator;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

public class WoodlandZone extends EcoZone {
	WoodlandZone() {
		super("woodland");

		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(6, 8, Tile.BRUNNERA, Tile.DAISY, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(2, 2.0f));
	}
}
