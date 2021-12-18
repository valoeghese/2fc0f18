package tk.valoeghese.fc0.world.kingdom;

import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.Vec2f;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.GameplayWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class Kingdom {
	public Kingdom(long seed, int id, Vec2f voronoi) {
		this.cityLoc = new Vec2i((int) (voronoi.getX() * SCALE), (int) (voronoi.getY() * SCALE));
		this.gridLoc = new Vec2i(MathsUtils.floor(voronoi.getX()), MathsUtils.floor(voronoi.getY()));

		Random rand = new Random(seed ^ id);
		List<String> onsetPre = new ArrayList<>(Arrays.asList("m", "n", "p", "t", "k"));
		List<String> codaPre = new ArrayList<>();

		this.addPhonemes(onsetPre, codaPre, rand);

		this.vowels = rand.nextInt(2) + 5;
		this.onset = onsetPre.toArray(new String[0]);
		this.coda = codaPre.toArray(new String[0]);
		this.name = this.pickName(rand);
		this.id = id;
	}

	private final Vec2i cityLoc;
	private final Vec2i gridLoc;
	private final String name;
	private final String[] onset;
	private final String[] coda;
	private final int vowels;
	public final int id;

	public Vec2i getCityCentre() {
		return this.cityLoc;
	}

	public Vec2f neighbourKingdomVec(int xoff, int yoff, int seed) {
		return Voronoi.sampleVoronoiGrid(xoff + this.gridLoc.getX(), yoff + this.gridLoc.getY(), seed, RELAXATION);
	}

	private void addPhonemes(List<String> onsetPre, List<String> codePre, Random rand) {
		int consonants = rand.nextInt(0b11111111);

		boolean postalveolar = (consonants & 0b1) == 0;
		boolean backCons = !postalveolar && (consonants & 0b10) == 0;
		boolean vp = (consonants & 0b10000) == 0;
		int rhoticity = rand.nextInt(3);

		if (rhoticity != 1) {
			onsetPre.add("l");
		}

		if (rhoticity > 0) {
			onsetPre.add("r");
		}

		if (postalveolar) {
			onsetPre.add("sh");
		}

		if (backCons) {
			onsetPre.add("q");
			onsetPre.add("qh");
		}

		if ((consonants & 0b100) == 0) { // affriciate
			onsetPre.add("ts");

			if (postalveolar) {
				onsetPre.add("ch");

				if (vp) {
					onsetPre.add("j");
				}
			}

			if ((consonants & 0b1000) == 0) {
				onsetPre.add("ps");
				onsetPre.add("ks");
			}

			if (vp) {
				onsetPre.add("zh");
			}
		}

		if (vp) {
			onsetPre.add("b");
			onsetPre.add("d");
			onsetPre.add("g");

			if (backCons) {
				onsetPre.add("'");
			}
		}

		boolean vf = (consonants & 0b100000) == 0;
		boolean vlf = ((consonants & 0b100000) == 0) || !vf;

		if (vf) {
			onsetPre.add("v");
			onsetPre.add("z");

			if (backCons) {
				onsetPre.add("gh");
			}
		}

		if (vlf) {
			onsetPre.add("f");
			onsetPre.add("s");

			if (rand.nextInt(3) > 0) {
				onsetPre.add("h");
			}

			if (backCons) {
				onsetPre.add("kh");
			}
		}

		if ((consonants & 0b1000000) == 0) { // coda nasal
			if ((consonants & 0b10000000) == 0) { // coda other
				codePre.add("m");
				codePre.add("n");

				if (rand.nextBoolean()) {
					codePre.add("ng");
				}

				if (rhoticity != 1) {
					codePre.add("l");
				}

				if (rhoticity > 0) {
					codePre.add("r");
				}
			} else {
				if (rand.nextBoolean()) {
					codePre.add("ng");
				} else {
					codePre.add("n");
				}
			}
		}
	}

	private String pickName(Random rand) {
		int syllables = rand.nextInt(3) + rand.nextInt(3) + 1;
		final boolean lessKingdomOfA = syllables == 1;

		StringBuilder sb = new StringBuilder();

		while (syllables --> 0) {
			boolean onset = rand.nextInt(lessKingdomOfA ? 5 : 3) > 0;
			boolean coda = this.coda.length > 0 && rand.nextInt(3) == 0;

			if (onset) {
				sb.append(this.onset[rand.nextInt(this.onset.length)]);
			}

			sb.append(VOWELS[rand.nextInt(this.vowels)]);

			if (coda) {
				sb.append(this.coda[rand.nextInt(this.coda.length)]);
			}
		}

		return sb.toString();
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return "Kingdom of " + this.name;
	}

	public String debugString() {
		return this.toString() + "\nCity Centre: " + this.getCityCentre().toString();
	}

	public static final float SCALE = 1050.0f;
	public static final float RELAXATION = 0.33f;
	private static final char[] VOWELS = {'i', 'e', 'a', 'o', 'u', 'y'};
}
