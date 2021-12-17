package tk.valoeghese.fc0.world.gen;

import test.Dummy2fc;
import test.PanelTest;
import tk.valoeghese.fc0.util.maths.Vec2f;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.gen.generator.CityGenerator;
import tk.valoeghese.fc0.world.gen.generator.Generator;
import tk.valoeghese.fc0.world.kingdom.Kingdom;
import tk.valoeghese.fc0.world.kingdom.KingdomIDMapper;
import tk.valoeghese.fc0.world.kingdom.Voronoi;
import tk.valoeghese.fc0.world.tile.Tile;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Displays a heightmap of a world where 1 pixel = 4 blocks (not average, picks instead the first in the block selection)
 */
public class TerrainTest extends PanelTest implements KingdomIDMapper {
	public static void main(String[] args) {
		Dummy2fc.use();
		System.out.println(seed);
		new TerrainTest().size(800).start();
	}

	static final long seed = new Random().nextLong();
	static final long SCALE = 3;
	static WorldGen worldGen = new WorldGen.Earth(seed, 0);
	private static final Map<Vec2f, Kingdom> KINGDOMS = new HashMap<>();

	@Override
	public final Kingdom kingdomById(Vec2f voronoi) {
		return KINGDOMS.computeIfAbsent(voronoi, v -> new Kingdom(seed, v.id(), v));
	}

	@Override
	protected int getColour(int x, int z) { // TODO kingdom widget thing when in city centres, 4 towns (also in worldgen), and lime->aquamarine for tree density in visualiser. also fix paths
		x *= SCALE;
		z *= SCALE;

		float height = (float) worldGen.sampleHeight(x, z);

		if (height > 51) {
			// add kingdoms to the map
			Kingdom k = this.kingdomById(Voronoi.sampleVoronoi(x / Kingdom.SCALE, z / Kingdom.SCALE, (int) seed, Kingdom.RELAXATION));

			Vec2i centre = k.getCityCentre(); // get city centre
			int dist = centre.manhattan(x, z);

			if (dist > Generator.OVERWORLD_CITY_SIZE + 5) {
				if (CityGenerator.isOnPath(this, x, z, k, k.getCityCentre(), (int) seed)) {
					return Color.getHSBColor(0.08f, 0.69f, height > 1.0f ? 1.0f : height).getRGB();
				}
			} else if (dist >= Generator.OVERWORLD_CITY_SIZE) {
				return Color.DARK_GRAY.getRGB(); // dark grey to represent the walls
			}

			// base worldgen
			EcoZone zone = worldGen.getEcoZoneByPosition(x, z);
			boolean sand = height <= 52 + worldGen.sampleBeaches(x, z); // beaches first
			sand = sand && zone.beach == Tile.SAND.id || (!sand && zone.surface == Tile.SAND.id); // if beach is sand or surface is sand
			height = (height / 128f);

			if (sand) {
				height = 1.5f * height;
				return Color.getHSBColor(0.14f, 0.69f, height > 1.0f ? 1.0f : height).getRGB();
			} else {
				// cold or not
				if (zone.isCold()) height *= 1.2f;
				return Color.getHSBColor(0.37f, zone.isCold() ? 0.12f : 0.69f, height > 1.0f ? 1.0f : height).getRGB();
			}
		} else {
			height = (height / 128f) + 0.2f;
			return Color.getHSBColor(0.69420f, 0.8f, height > 1.0f ? 1.0f : height).getRGB();
		}
	}
}
