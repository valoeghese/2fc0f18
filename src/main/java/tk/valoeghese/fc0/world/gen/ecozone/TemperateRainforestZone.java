package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.ScatteredOreGenerator;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

public class TemperateRainforestZone extends EcoZone {
	TemperateRainforestZone() {
		super("temperate_rainforest");

		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.GALENA);
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(6, 16, Tile.DAISY, Tile.TALLGRASS, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(5, 2.0f, 4, 3));
		this.addGenerator(Generator.POMELO_PLANT, new TreeGeneratorSettings(0, 2.2f, 1, 1));
	}
}
