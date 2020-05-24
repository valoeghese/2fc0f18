package tk.valoeghese.fc0.world.gen;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.GroundFoliageGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class EcoZone {
	public EcoZone(String name) {
		this(name, Tile.GRASS, Tile.SAND);
	}

	public EcoZone(String name, Tile surface, Tile beach) {
		this.name = name;
		this.surface = surface.id;
		this.beach = beach.id;
	}

	private final String name;
	public final byte surface;
	public final byte beach;
	private Map<Generator, GeneratorSettings> generators = new LinkedHashMap<>();

	public <T extends GeneratorSettings> EcoZone addGenerator(Generator<T> generator, T settings) {
		this.generators.put(generator, settings);
		return this;
	}

	public Set<Map.Entry<Generator, GeneratorSettings>> getGenerators() {
		return this.generators.entrySet();
	}

	@Override
	public String toString() {
		return "ecozone." + this.name;
	}

	public static final EcoZone TEMPERATE_GRASSLAND = new EcoZone("temperate_grassland")
			.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(3, 7, Tile.DAISY))
			.addGenerator(Generator.TREE, new TreeGeneratorSettings(0, 1.5f));

	public static final EcoZone TROPICAL_GRASSLAND = new EcoZone("tropical_grassland")
			.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(5, 8, Tile.DAISY))
			.addGenerator(Generator.TREE, new TreeGeneratorSettings(0, 1.75f, 1, 1));

	public static final EcoZone TROPICAL_RAINFOREST = new EcoZone("tropical_rainforest")
			.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(1, 3, Tile.DAISY))
			.addGenerator(Generator.TREE, new TreeGeneratorSettings(8, 3.5f, 6, 4));

	public static final EcoZone TEMPERATE_RAINFOREST = new EcoZone("temperate_rainforest")
			.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(0, 2, Tile.DAISY))
			.addGenerator(Generator.TREE, new TreeGeneratorSettings(5, 2.0f, 4, 3));

	public static final EcoZone TEMPERATE_WOODLAND = new EcoZone("temperate_woodland")
			.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(1, 2, Tile.DAISY))
			.addGenerator(Generator.TREE, new TreeGeneratorSettings(2, 2.0f));

	public static final EcoZone TUNDRA = new EcoZone("tundra")
			.addGenerator(Generator.TREE, new TreeGeneratorSettings(0, 0));

	public static final EcoZone DESERT = new EcoZone("desert", Tile.SAND, Tile.SAND)
			.addGenerator(Generator.GROUND_FOLIAGE, new GroundFoliageGeneratorSettings(3, 5, Tile.CACTUS));
}
