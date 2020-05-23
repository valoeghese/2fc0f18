package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.client.ClientPlayer;
import tk.valoeghese.fc0.util.OrderedList;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class ChunkSelection implements World, ChunkAccess {
	public ChunkSelection(long seed, int size) {
		this.seed = seed;
		this.offset = size - 1;
		this.diameter = 1 + this.offset * 2;
		long time = System.currentTimeMillis();

		if (this.seed != 0) {
			System.out.println("Generating World.");
		}

		this.chunks = new Chunk[this.diameter * this.diameter];
		this.genRand = new Random(seed);

		OrderedList<Chunk> orderedChunks = new OrderedList<>(c -> (float) (Math.abs(c.x) + Math.abs(c.z)));

		for (int x = -size + 1; x < size; ++x) {
			for (int z = -size + 1; z < size; ++z) {
				Chunk chunk = WorldGen.generateChunk(this, x, z, seed, this.genRand);
				this.chunks[(x + this.offset) * this.diameter + z + this.offset] = chunk;
				orderedChunks.add(chunk);
			}
		}

		if (this.seed != 0) {
			System.out.println("Generated World in " + (System.currentTimeMillis() - time) + "ms.");
		}

		// add to render queue
		int i = 0;
		for (Chunk chunk : orderedChunks) {
			if (i++ < 8) {
				this.chunksForRendering.add(chunk);
			} else {
				this.toAddForRendering.add(chunk);
			}
		}

		this.minBound = (-size + 1) << 4;
		this.maxBound = size << 4;
	}

	private final int offset;
	private final int diameter;
	private final int minBound;
	private final int maxBound;
	private final Chunk[] chunks;
	private final Queue<Chunk> toAddForRendering = new LinkedList<>();
	private final List<Chunk> chunksForRendering = new ArrayList<>();
	private final Random genRand;
	private long seed;
	private boolean ncTick = false;

	public void populateChunks() {
		long time = System.currentTimeMillis();

		if (this.seed != 0) {
			System.out.println("Populating World.");
		}

		for (Chunk chunk : this.chunks) {
			WorldGen.populateChunk(this, chunk, this.genRand);
		}

		if (this.seed != 0) {
			System.out.println("Populated World in " + (System.currentTimeMillis() - time) + "ms.");
		}
	}

	@Override
	@Nullable
	public Chunk getChunk(int x, int z) {
		if (!this.isInWorld(x << 4, 50, z << 4)) {
			return null;
		}

		return this.getChunkDirect(x, z);
	}

	public Chunk getChunkDirect(int x, int z) {
		return this.chunks[(x + this.offset) * this.diameter + z + this.offset];
	}

	public void updateChunksForRendering() {
		if (!this.toAddForRendering.isEmpty()) {
			ncTick = !ncTick;

			if (ncTick) {
				this.chunksForRendering.add(this.toAddForRendering.remove());
			}
		}
	}

	public List<Chunk> getChunksForRendering() {
		return this.chunksForRendering;
	}

	@Override
	public byte readTile(int x, int y, int z) {
		return this.getChunkDirect(x >> 4, z >> 4).readTile(x & 0xF, y, z & 0xF);
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		this.getChunkDirect(x >> 4, z >> 4).writeTile(x & 0xF, y, z & 0xF, tile);
	}

	@Override
	public int getHeight(int x, int z, Predicate<Tile> solid) {
		return this.getChunkDirect(x >> 4, z >> 4).getHeight(x & 0xF, z & 0xF, solid);
	}

	@Override
	public boolean isInWorld(int x, int y, int z) {
		return x >= this.minBound && x < this.maxBound && z >= this.minBound && z < this.maxBound && y >= 0 && y < WORLD_HEIGHT;
	}

	@Override
	public void updateChunkOf(ClientPlayer clientPlayer) {
		TilePos pos = clientPlayer.getTilePos();
		ChunkPos cPos = pos.toChunkPos();

		if (this.isInWorld(pos.x, 50, pos.z)) {
			this.getChunkDirect(cPos.x, cPos.z).updateChunkOf(clientPlayer);
		} else if (clientPlayer.chunk != null) {
			clientPlayer.chunk.removePlayer(clientPlayer);
			clientPlayer.chunk = null;
		}
	}

	@Override
	public void destroy() {
		for (Chunk c : this.chunksForRendering) {
			c.destroy();
		}
	}
}
