package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.ScatteredOreGenerator;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

public class ColdWoodlandZone extends EcoZone {
	ColdWoodlandZone() {
		super("cold_woodland", Tile.GRASS, Tile.STONE);

		this.cold();
		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.EXTRA_COAL);
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(0, 2, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(2, 1.0f));
	}
}
