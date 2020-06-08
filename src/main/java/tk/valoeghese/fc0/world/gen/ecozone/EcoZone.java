package tk.valoeghese.fc0.world.gen.ecozone;

import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.gen.generator.GeneratorSettings;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class EcoZone {
	protected EcoZone(String name) {
		this(name, Tile.GRASS, Tile.SAND);
	}

	protected EcoZone(String name, Tile surface, Tile beach) {
		this.name = name;
		this.surface = surface.id;
		this.beach = beach.id;
	}

	private final String name;
	public final byte surface;
	public final byte beach;
	private Map<Generator, GeneratorSettings> generators = new LinkedHashMap<>();
	private boolean cold = false;

	public <T extends GeneratorSettings> void addGenerator(Generator<T> generator, T settings) {
		this.generators.put(generator, settings);
	}

	protected void cold() {
		this.cold = true;
	}

	public Set<Map.Entry<Generator, GeneratorSettings>> getGenerators() {
		return this.generators.entrySet();
	}

	public boolean isCold() {
		return this.cold;
	}

	@Override
	public String toString() {
		return "ecozone." + this.name;
	}

	public static final EcoZone TEMPERATE_GRASSLAND = new GrasslandZone();
	public static final EcoZone TROPICAL_GRASSLAND = new SteppeZone();
	public static final EcoZone TROPICAL_RAINFOREST = new TropicalRainforestZone();
	public static final EcoZone TEMPERATE_RAINFOREST = new TemperateRainforestZone();
	public static final EcoZone TEMPERATE_WOODLAND = new WoodlandZone();
	public static final EcoZone TUNDRA = new TundraZone();
	public static final EcoZone DESERT = new DesertZone();
	public static final EcoZone COLD_WOODLAND = new ColdWoodlandZone();
}
