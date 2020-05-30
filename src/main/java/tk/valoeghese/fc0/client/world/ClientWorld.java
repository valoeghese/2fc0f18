package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.OrderedList;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.save.Save;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ClientWorld extends GameplayWorld<ClientChunk> {
	public ClientWorld(@Nullable Save save, long seed, int size) {
		super(save, seed, size, ClientChunk::new);
	}

	private final OrderedList<ClientChunk> toAddToQueue = new OrderedList<>(c -> (float) c.getPos().distanceTo(
			Client2fc.getInstance().getPlayer().chunk.getPos()));
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
					result.render = true;
					this.toAddToQueue.add(result);
				}
			}
		}

		return result;
	}

	public void updateChunksForRendering() {
		while (!this.toAddToQueue.isEmpty()) {
			if (this.chunksForRendering.size() < 8) {
				this.chunksForRendering.add(this.toAddToQueue.remove(0));
			} else {
				this.toAddForRendering.add(this.toAddToQueue.remove(0));
			}
		}

		if (!this.toAddForRendering.isEmpty()) {
			this.ncTick = !this.ncTick;

			if (this.ncTick) {
				this.chunksForRendering.add(this.toAddForRendering.remove());
			}
		}
	}

	public List<ClientChunk> getChunksForRendering() {
		return this.chunksForRendering;
	}

	@Override
	protected void onChunkRemove(Chunk c) {
		if (c.render) {
			c.destroy();

			if (this.toAddToQueue.contains(c)) {
				this.toAddForRendering.remove(c);
			} else if (this.toAddForRendering.contains(c)) {
				this.toAddForRendering.remove(c);
			} else if (this.chunksForRendering.contains(c)) {
				this.chunksForRendering.remove(c);
			}
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		for (Chunk c : this.chunksForRendering) {
			c.destroy();
		}
	}
}
