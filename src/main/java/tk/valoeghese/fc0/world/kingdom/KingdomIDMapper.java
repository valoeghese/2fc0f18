package tk.valoeghese.fc0.world.kingdom;

import tk.valoeghese.fc0.util.maths.Vec2f;

/**
 * Only exists for the purpose of sharing code between tests and dev with slightly different implementations.
 */
public interface KingdomIDMapper {
	/**
	 * @param sample the vector sample from the kingdom voronoi grid.
	 * @return the cached (or newly created) kingdom belonging to the sample.
	 */
	Kingdom kingdomById(Vec2f sample);
}
