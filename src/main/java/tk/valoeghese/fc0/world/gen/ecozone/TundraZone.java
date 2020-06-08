package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.HeightCountGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

public class TundraZone extends EcoZone {
	TundraZone() {
		super("tundra", Tile.GRASS, Tile.STONE);

		this.cold();
		this.addGenerator(Generator.SCATTERED_ORE, new HeightCountGeneratorSettings(200, 1, 60));
		this.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(0, 1, Tile.TALLGRASS));
		this.addGenerator(Generator.TREE, new TreeGeneratorSettings(-1, 0));
	}
}
