package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.util.OrderedList;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameWorld;
import tk.valoeghese.fc0.world.save.Save;

import javax.annotation.Nullable;
import java.util.*;

public class ClientWorld extends GameWorld<ClientChunk> {
	public ClientWorld(@Nullable Save save, long seed, int size) {
		super(save, seed, size, ClientChunk::new);
	}

	private final Queue<ClientChunk> toAddForRendering = new LinkedList<>();
	private final List<ClientChunk> chunksForRendering = new ArrayList<>();
	private boolean ncTick = false;

	@Nullable
	@Override
	public Chunk loadChunk(int x, int z, ChunkLoadStatus status) {
		ClientChunk result = (ClientChunk) super.loadChunk(x, z, status);

		if (result != null) {
			if (status == ChunkLoadStatus.RENDER) {
				if (!result.render) {
					result.getOrCreateMesh();
					result.render = true;
				}
			}
		}

		return result;
	}

	public void computeRenderChunks(ChunkPos playerChunk) {
		OrderedList<ClientChunk> orderedChunks = new OrderedList<ClientChunk>(c -> (float) c.getPos().distanceTo(playerChunk));

		for (Iterator<ClientChunk> it = this.getChunks(); it.hasNext();) {
			ClientChunk chunk = it.next();
			if (chunk != null) {
				orderedChunks.add(chunk);
			}
		}

		// add to render queue
		int i = 0;
		for (ClientChunk chunk : orderedChunks) {
			if (i++ < 8) {
				this.chunksForRendering.add(chunk);
			} else {
				this.toAddForRendering.add(chunk);
			}
		}
	}

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
