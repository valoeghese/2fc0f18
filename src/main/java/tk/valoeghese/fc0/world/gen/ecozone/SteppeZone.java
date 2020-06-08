package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.ScatteredOreGenerator;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

public class SteppeZone extends EcoZone {
	SteppeZone() {
		super("steppe");

		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.GALENA);
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(20, 32, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(0, 1.75f, 1, 1));

	}
}
