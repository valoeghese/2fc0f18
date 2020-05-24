package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkSelection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ClientChunkSelection extends ChunkSelection<ClientChunk> {
	public ClientChunkSelection(long seed, int size) {
		super(seed, size, ClientChunk::new, ClientChunk[]::new);

		// add to render queue
		int i = 0;
		for (ClientChunk chunk : this.orderedChunks) {
			if (i++ < 8) {
				this.chunksForRendering.add(chunk);
			} else {
				this.toAddForRendering.add(chunk);
			}
		}
	}

	private final Queue<ClientChunk> toAddForRendering = new LinkedList<>();
	private final List<ClientChunk> chunksForRendering = new ArrayList<>();
	private boolean ncTick = false;

	public void updateChunksForRendering() {
		if (!this.toAddForRendering.isEmpty()) {
			ncTick = !ncTick;

			if (ncTick) {
				this.chunksForRendering.add(this.toAddForRendering.remove());
			}
		}
	}

	public List<ClientChunk> getChunksForRendering() {
		return this.chunksForRendering;
	}

	@Override
	public void destroy() {
		for (Chunk c : this.chunksForRendering) {
			c.destroy();
		}
	}
}
