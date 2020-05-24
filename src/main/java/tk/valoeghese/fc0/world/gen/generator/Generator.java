package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.World;
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

	public abstract void generate(World world, T generatorSettings, int startX, int startZ, Random rand);

	protected static int getHeightForGeneration(World world, int x, int z) {
		return world.getHeight(x, z, tile -> tile.isOpaque() && tile != Tile.LOG) + 1;
	}

	public static final Generator<TreeGeneratorSettings> TREE = new TreeGenerator();
	public static final Generator<GroundFoliageGeneratorSettings> GROUND_FOLIAGE = new GroundFoliageGenerator();
}
