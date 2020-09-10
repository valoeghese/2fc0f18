package tk.valoeghese.fc0;

import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkLoadStatus;
import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.player.Player;

import java.util.*;

public class Game2fc<W extends World, P extends Player> {
	protected Game2fc() {
		instance = this;
	}

	public long time = 0;
	protected W world;
	protected P player;
	private final Queue<Runnable> later = new LinkedList<>();
	private final Queue<Chunk> toUpdateLighting = new LinkedList<>();

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

	protected void runNextQueued() {
		Runnable task = null;

		synchronized (this.later) {
			if (!this.later.isEmpty()) {
				task = this.later.remove();
			}
		}

		if (task != null) {
			task.run();
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

	public static Game2fc getInstance() {
		return instance;
	}

	private static Game2fc instance;
	public static final Random RANDOM = new Random();
}
