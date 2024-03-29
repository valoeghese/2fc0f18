package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.util.Pair;
import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.NoneGeneratorSettings;
import tk.valoeghese.fc0.world.gen.generator.ScatteredOreGenerator;
import tk.valoeghese.fc0.world.gen.generator.TreeGenerator;
import tk.valoeghese.fc0.world.gen.generator.TreeGeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public abstract class EcoZone {
	protected EcoZone(String name) {
		this(name, Tile.GRASS, Tile.SAND);
	}

	protected EcoZone(String name, Tile surface, Tile beach) {
		this.name = name;
		this.surface = surface.id;
		this.beach = beach.id;

		this.addGenerator(Generator.CITY, NoneGeneratorSettings.INSTANCE);
		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.GALENA);
		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.MAGNETITE);
		this.addGenerator(Generator.SCATTERED_ORE, ScatteredOreGenerator.COAL);
	}

	private final String name;
	public final byte surface;
	public final byte beach;
	private List<Pair<Generator, GeneratorSettings>> generators = new ArrayList<>();
	private boolean cold = false;
	private int treesPerChunk; // trees per chunk, for the debug view.

	public <T extends GeneratorSettings> void addGenerator(Generator<T> generator, T settings) {
		this.generators.add(new Pair<>(generator, settings));

		if (generator instanceof TreeGenerator) {
			this.treesPerChunk += ((TreeGeneratorSettings) settings).getBaseTreeCount();
		}
	}

	protected void cold() {
		this.cold = true;
	}

	public List<Pair<Generator, GeneratorSettings>> getGenerators() {
		return this.generators;
	}

	public boolean isCold() {
		return this.cold;
	}

	/**
	 * @return the average foliage density. Used in the terrain-test debugger.
	 */
	public int getAverageFoliageDensity() {
		return this.treesPerChunk;
	}

	@Override
	public String toString() {
		return "ecozone." + this.name;
	}

	public static final EcoZone TEMPERATE_GRASSLAND = new GrasslandZone();
	public static final EcoZone TROPICAL_STEPPE = new SteppeZone();
	public static final EcoZone TROPICAL_RAINFOREST = new TropicalRainforestZone("tropical_rainforest", 8);
	public static final EcoZone TROPICAL_RAINFOREST_EDGE = new TropicalRainforestZone("tropical_rainforest_edge", 3);
	public static final EcoZone TEMPERATE_RAINFOREST = new TemperateRainforestZone();
	public static final EcoZone TEMPERATE_WOODLAND = new WoodlandZone();
	public static final EcoZone TUNDRA = new TundraZone();
	public static final EcoZone DESERT = new DesertZone();
	public static final EcoZone COLD_WOODLAND = new ColdWoodlandZone();
}
