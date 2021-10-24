package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.ScatteredOreGenerator;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

public class TropicalRainforestZone extends EcoZone {
	TropicalRainforestZone(int density) {
		super("tropical_rainforest");

		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.EXTRA_COAL);
		int grassDensity = (int)((double)density * 1.5);
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(grassDensity, grassDensity + 3, Tile.DAISY, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(density, 3.5f, 6, 4));
		this.addGenerator(Generator.POMELO_PLANT, new TreeGeneratorSettings(0, 5.0f, 1, 1));
	}
}
