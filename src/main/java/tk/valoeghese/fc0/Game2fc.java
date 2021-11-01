package tk.valoeghese.fc0;

import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.TileAccess;
import tk.valoeghese.fc0.world.player.Player;

import java.util.*;

import static org.joml.Math.PI;
import static org.joml.Math.sin;

public abstract class Game2fc<W extends TileAccess, P extends Player> {
	protected Game2fc() {
		instance = this;
	}

	public long time = 4800;
	protected W world;
	protected P player;
	private final Queue<Runnable> later = new LinkedList<>();
	private final Queue<Chunk> toUpdateLighting = new LinkedList<>();

	private static final float SKY_LIGHTING_CHANGE_RATE = 13.5f;
	private static final float SKY_ROTATION_RATE = (float) (9216 * PI * 4); // 4pi n

	/**
	 * @return the sky angle between 0.0f and (float)2pi.
	 */
	public float calculateSkyAngle() {
		return ((float)this.time % SKY_ROTATION_RATE) / 9216.0f;
	}

	/**
	 * @return the lighting coefficient, between 0.125f and 1.15f
	 */
	public float calculateLighting() {
		float zeitGrellheit = 0.1f + SKY_LIGHTING_CHANGE_RATE * sin((float) this.time / 9216.0f);
		return MathsUtils.clampMap(zeitGrellheit, -1, 1, 0.125f, 1.15f);
	}

	/**
	 * @return the game's current world.
	 */
	public W getWorld() {
		return this.world;
	}

	protected int getLightingQueueSize() {
		synchronized (this.toUpdateLighting) {
			return this.toUpdateLighting.size();
		}
	}

	protected void updateNextLighting() {
		List<Chunk> c = new ArrayList<>();

		synchronized (this.toUpdateLighting) {
			int count = Math.min(9, toUpdateLighting.size());

			for (int i = 0; i < count; ++i) {
				c.add(this.toUpdateLighting.remove());
			}
		}

		for (Chunk chunk : c) {
			if (chunk.status != ChunkLoadStatus.UNLOADED) {
				chunk.refreshLighting();
			}
		}
	}

	protected void runNextQueued(int count) {
		List<Runnable> tasks = new ArrayList<>();

		synchronized (this.later) {
			for (int i = 0; i < count; ++i) {
				if (!this.later.isEmpty()) {
					tasks.add(this.later.remove());
				}
			}
		}

		for (Runnable task : tasks) {
			task.run();;
		}
	}

	public void runLater(Runnable task) {
		synchronized (this.later) {
			this.later.add(task);
		}
	}

	public void needsLightingUpdate(Chunk c) {
		synchronized (this.toUpdateLighting) {
			if (!this.toUpdateLighting.contains(c)) {
				this.toUpdateLighting.add(c);
			}
		}
	}

	protected void tick() {
		this.player.tick();
		++this.time;
	}

	public abstract boolean isMainThread();

	public static Game2fc getInstance() {
		return instance;
	}

	private static Game2fc instance;
	public static final Random RANDOM = new Random();
}
