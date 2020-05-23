package tk.valoeghese.fc0.world.gen.generator;

import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.gen.EcoZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Generator {
	public static final List<Generator> GENERATORS = new ArrayList<>();

	protected Generator() {
		GENERATORS.add(this);
	}

	public abstract void generate(World world, EcoZone ecoZone, int startX, int startZ, Random rand);

	public static final Generator TREE = new TreeGenerator();
}
