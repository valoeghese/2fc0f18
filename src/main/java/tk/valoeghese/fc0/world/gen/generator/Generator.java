package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.gen.GenWorld;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.Random;

public abstract class Generator<T extends GeneratorSettings> {
	protected Generator(String name) {
		this.name = name;
	}

	private final String name;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}

		Generator<?> generator = (Generator<?>) o;
		return this.name.equals(generator.name);
	}

	@Override
	public int hashCode() {
		return 3 * this.name.hashCode();
	}

	@Override
	public String toString() {
		return "generator." + this.name;
	}

	public abstract void generate(GenWorld world, T generatorSettings, int startX, int startZ, Random rand);

	protected static int getHeightForGeneration(GenWorld world, int x, int z) {
		return world.getHeight(x, z, tile -> tile.canSustainGeneration()) + 1;
	}

	protected boolean writeTile(GenWorld world, int x, int y, int z, byte tile) {
		byte current = world.readTile(x, y, z);

		if (current != Tile.STONE.id && current != Tile.LOG.id) {
			world.wgWriteTile(x, y, z, tile);
			return true;
		} else {
			return false;
		}
	}

	public static final Generator<TreeGeneratorSettings> TREE = new TreeGenerator();
	public static final Generator<GroundFoliageGeneratorSettings> GROUND_FOLIAGE = new GroundFoliageGenerator();
	public static final Generator<OreGeneratorSettings> SCATTERED_ORE = new ScatteredOreGenerator();
	public static final Generator<TreeGeneratorSettings> POMELO_PLANT = new PomeloPlantGenerator();
	public static final Generator<NoneGeneratorSettings> CITY = new CityGenerator(85);
}
