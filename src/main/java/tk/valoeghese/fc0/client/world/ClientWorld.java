package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.OrderedList;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.save.SaveLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class ClientWorld extends GameplayWorld<ClientChunk> {
	public ClientWorld(SaveLike save, long seed, int size) {
		super(save, seed, size, ClientChunk::new);
	}

	private final OrderedList<ClientChunk> toAddToQueue = new OrderedList<>(c -> (float) c.getPos().distanceTo(
			Client2fc.getInstance().getPlayer().getTilePos().toChunkPos()));
	private final Queue<ClientChunk> toAddForRendering = new LinkedList<>();
	private final List<ClientChunk> chunksForRendering = new ArrayList<>();
	private boolean ncTick = false;

	@Override
	public void addUpgradedChunk(ClientChunk chunk, ChunkLoadStatus status) {
		super.addUpgradedChunk(chunk, status);

		if (status == ChunkLoadStatus.RENDER) {
			if (!chunk.render) {
				chunk.render = true;
				this.toAddToQueue.add(chunk);
			}
		}
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
				ClientChunk c = this.toAddForRendering.remove();

				if (c.mesh != null) { // TODO is this neccesary? (paired with the related check in ClientChunk#refreshLighting)
					c.rebuildMesh();
				}

				this.chunksForRendering.add(c);
			}
		}
	}

	public List<ClientChunk> getChunksForRendering() {
		return this.chunksForRendering;
	}

	@Override
	protected void onChunkRemove(Chunk c) {
		c.destroy();

		if (c.render) {
			if (this.toAddToQueue.contains(c)) {
				this.toAddToQueue.remove(c);
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
