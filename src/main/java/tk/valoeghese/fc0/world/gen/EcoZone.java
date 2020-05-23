package tk.valoeghese.fc0.world.gen;

import tk.valoeghese.fc0.world.tile.Tile;

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
	public int baseTreeCount = 0;
	public float treeCountVariation = 0;
	public int baseTreeHeight = 3;
	public int potentialHeightIncrease = 3;

	protected EcoZone treePlacement(int baseCount, float countVariation) {
		this.baseTreeCount = baseCount;
		this.treeCountVariation = countVariation;
		return this;
	}

	protected EcoZone treeStyle(int baseHeight, int potentialHeightIncrease) {
		this.baseTreeHeight = baseHeight;
		this.potentialHeightIncrease = potentialHeightIncrease;
		return this;
	}

	@Override
	public String toString() {
		return "ecozone." + this.name;
	}

	public static final EcoZone TEMPERATE_GRASSLAND = new EcoZone("temperate_grassland")
			.treePlacement(0, 1.5f);
	public static final EcoZone TROPICAL_GRASSLAND = new EcoZone("tropical_grassland")
			.treePlacement(0, 1.75f)
			.treeStyle(1, 1);
	public static final EcoZone TROPICAL_RAINFOREST = new EcoZone("tropical_rainforest")
			.treePlacement(6, 3.0f)
			.treeStyle(6, 4);
	public static final EcoZone TEMPERATE_RAINFOREST = new EcoZone("temperate_rainforest")
			.treePlacement(4, 2.0f)
			.treeStyle(4, 3);
	public static final EcoZone TEMPERATE_WOODLAND = new EcoZone("temperate_woodland")
			.treePlacement(2, 2.0f);
	public static final EcoZone TUNDRA = new EcoZone("tundra");
	public static final EcoZone DESERT = new EcoZone("desert", Tile.SAND, Tile.SAND);
}
