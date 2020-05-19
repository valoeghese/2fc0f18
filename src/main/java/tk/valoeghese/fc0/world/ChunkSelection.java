package tk.valoeghese.fc0.world;

import tk.valoeghese.fc0.client.ClientPlayer;
import tk.valoeghese.fc0.util.ChunkPos;
import tk.valoeghese.fc0.util.OrderedList;
import tk.valoeghese.fc0.util.TilePos;

import java.util.*;

public class ChunkSelection implements World {
	public ChunkSelection(long seed) {
		this.offset = SIZE - 1;
		this.diameter = 1 + this.offset * 2;
		long time = System.currentTimeMillis();
		System.out.println("Generating World.");
		this.chunks = new Chunk[this.diameter * this.diameter];
		this.genRand = new Random(seed);

		OrderedList<Chunk> orderedChunks = new OrderedList<>(c -> (float) (Math.abs(c.x) + Math.abs(c.z)));

		for (int x = -SIZE + 1; x < SIZE; ++x) {
			for (int z = -SIZE + 1; z < SIZE; ++z) {
				Chunk chunk = WorldGen.generateChunk(x, z, seed, this.genRand);
				this.chunks[(x + this.offset) * this.diameter + z + this.offset] = chunk;
				orderedChunks.add(chunk);
			}
		}

		System.out.println("Generated World in " + (System.currentTimeMillis() - time) + "ms.");

		// add to render queue
		int i = 0;
		for (Chunk chunk : orderedChunks) {
			if (i++ < 8) {
				this.chunksForRendering.add(chunk);
			} else {
				this.toAddForRendering.add(chunk);
			}
		}

		this.minBound = (-SIZE + 1) << 4;
		this.maxBound = SIZE << 4;
	}

	private final int offset;
	private final int diameter;
	private final int minBound;
	private final int maxBound;
	private final Chunk[] chunks;
	private final Queue<Chunk> toAddForRendering = new LinkedList<>();
	private final List<Chunk> chunksForRendering = new ArrayList<>();
	private final Random genRand;
	private boolean ncTick = false;

	public void populateChunks() {
		long time = System.currentTimeMillis();
		System.out.println("Populating World.");

		for (Chunk chunk : this.chunks) {
			WorldGen.populateChunk(this, chunk, this.genRand);
		}

		System.out.println("Populated World in " + (System.currentTimeMillis() - time) + "ms.");
	}

	public Chunk getChunk(int x, int z) {
		return this.chunks[(x + this.offset) * this.diameter + z + this.offset];
	}

	public List<Chunk> getChunksForRendering() {
		if (!this.toAddForRendering.isEmpty()) {
			ncTick = !ncTick;

			if (ncTick) {
				this.chunksForRendering.add(this.toAddForRendering.remove());
			}
		}

		return this.chunksForRendering;
	}

	@Override
	public byte readTile(int x, int y, int z) {
		return this.getChunk(x >> 4, z >> 4).readTile(x & 0xF, y, z & 0xF);
	}

	@Override
	public void writeTile(int x, int y, int z, byte tile) {
		this.getChunk(x >> 4, z >> 4).writeTile(x & 0xF, y, z & 0xF, tile);
	}

	@Override
	public int getHeight(int x, int z) {
		return this.getChunk(x >> 4, z >> 4).getHeight(x & 0xF, z & 0xF);
	}

	@Override
	public boolean isInWorld(int x, int y, int z) {
		return x >= this.minBound && x < this.maxBound && z >= this.minBound && z < this.maxBound && y >= 0 && y < 128;
	}

	@Override
	public void updateChunkOf(ClientPlayer clientPlayer) {
		TilePos pos = clientPlayer.getTilePos();
		ChunkPos cPos = pos.toChunkPos();

		if (this.isInWorld(pos.x, 50, pos.z)) {
			this.getChunk(cPos.x, cPos.z).updateChunkOf(clientPlayer);
		} else if (clientPlayer.chunk != null) {
			clientPlayer.chunk.removePlayer(clientPlayer);
			clientPlayer.chunk = null;
		}
	}

	private static final int SIZE = 9;
}
