package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.OrderedList;
import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.save.Save;
import tk.valoeghese.fc0.world.save.SaveLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class ClientWorld extends GameplayWorld<ClientChunk> {
	public ClientWorld(SaveLike save, long seed, int size) {
		super(save, seed, size, ClientChunk::new);
	}

	private final List<ClientChunk> toAddToQueue = new ArrayList<>(); // changed to regular array list from ordered list as ordering is now done on the chunkload end.
	private final Set<ChunkPos> toAddToQueuePositions = new HashSet<>();

	private void addToToAddToQueue(ClientChunk chunk) {
		if (chunk != null && this.toAddToQueuePositions.add(chunk.getPos())) { // if did not already contain, add to queue
			this.toAddToQueue.add(chunk);
		}
	}

	private final Queue<ClientChunk> toAddForRendering = new LinkedList<>();
	private final List<ClientChunk> chunksForRendering = new ArrayList<>();
	private boolean ncTick = false; // I think this variable just exists to make rendering not lag the game by batching half as often

	@Override
	public void addUpgradedChunk(final ClientChunk chunk, ChunkLoadStatus status) {
		super.addUpgradedChunk(chunk, status);

		if (status == ChunkLoadStatus.RENDER) {
			if (!chunk.render) {
				chunk.render = true;
				this.addToToAddToQueue(chunk);

				int cx = chunk.x;
				int cz = chunk.z;

				// recalculate because I broke shit in optimising chunkloading
				// FIXME this does absolutely nothing that it's supposed to do
				Game2fc.getInstance().runLater(() -> {
					ClientChunk chunk_ = (ClientChunk) getRenderChunk(cx, cz + 1);
					if (chunk_ != null) chunk_.rebuildMesh();
					chunk_ = (ClientChunk) getRenderChunk(cx, cz - 1);
					if (chunk_ != null) chunk_.rebuildMesh();
					chunk_ = (ClientChunk) getRenderChunk(cx + 1, cz);
					if (chunk_ != null) chunk_.rebuildMesh();
					chunk_ = (ClientChunk) getRenderChunk(cx - 1, cz);
					if (chunk_ != null) chunk_.rebuildMesh();
				});
			}
		}
	}

	public boolean hasChunk(ClientChunk chunk) {
		return this.chunks.containsValue(chunk);
	}

	public void updateChunksForRendering() {
		this.ncTick = !this.ncTick;

		if (this.ncTick) {
			while (!this.toAddToQueue.isEmpty()) {
				ClientChunk c = this.toAddToQueue.remove(0);
				this.toAddToQueuePositions.remove(c.getPos());

				if (this.chunksForRendering.size() < 8) {
					this.chunksForRendering.add(c);
				} else {
					this.toAddForRendering.add(c);
				}
			}

			if (!this.toAddForRendering.isEmpty()) {
				ClientChunk c = this.toAddForRendering.remove();
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
			if (this.toAddToQueuePositions.contains(c.getPos())) {
				this.toAddToQueue.remove(c);
				this.toAddToQueuePositions.remove(c.getPos());
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
