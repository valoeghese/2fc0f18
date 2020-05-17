package tk.valoeghese.fc0.world.generator;

import tk.valoeghese.fc0.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Generator {
	public static final List<Generator> GENERATORS = new ArrayList<>();

	protected Generator() {
		GENERATORS.add(this);
	}

	public abstract void generate(int startX, int startZ, Random rand, World world);

	public static final Generator TREE = new TreeGenerator();
}
