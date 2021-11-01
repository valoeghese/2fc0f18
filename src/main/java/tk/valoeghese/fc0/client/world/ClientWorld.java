package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.save.SaveLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ClientWorld extends GameplayWorld<ClientChunk> {
	public ClientWorld(SaveLike save, long seed, int size) {
		super(save, seed, size, ClientChunk::new);
	}

	private final List<ClientChunk> toAddToQueue = new ArrayList<>(); // changed to regular array list from ordered list as ordering is now done on the chunkload end.
	private final Set<ChunkPos> toAddToQueuePositions = new HashSet<>();

	private final Queue<ClientChunk> toAddForRendering = new LinkedList<>();
	private final List<ClientChunk> chunksForRendering = new ArrayList<>();
	private boolean offRenderTick = false;

	private void addToToAddToQueue(ClientChunk chunk) {
		if (chunk != null && this.toAddToQueuePositions.add(chunk.getPos())) { // if did not already contain, add to queue
			this.toAddToQueue.add(chunk);
		}
	}

	@Nullable
	public ClientChunk getRenderChunk(int x, int z) {
		ClientChunk c = this.getChunk(x, z);

		if (c == null) {
			return null;
		}

		if (c.preRender) {
			return c;
		}

		return null;
	}

	@Override
	public void addUpgradedChunk(final ClientChunk chunk, ChunkLoadStatus status) {
		super.addUpgradedChunk(chunk, status);

		if (status == ChunkLoadStatus.RENDER) {
			if (!chunk.preRender) {
				chunk.preRender = true;

				// check which surrounding chunks are already loaded and thus meshing will work properly
				if (this.getChunk(chunk.x, chunk.z + 1) != null) { // our chunk is relatively -z from +z chunk, so use that flag
					chunk.receiveUpdateFromNeighbour(0b100);
				}
				if (this.getChunk(chunk.x + 1, chunk.z) != null) {
					chunk.receiveUpdateFromNeighbour(0b1000);
				}
				if (this.getChunk(chunk.x, chunk.z - 1) != null) {
					chunk.receiveUpdateFromNeighbour(0b1);
				}
				if (this.getChunk(chunk.x - 1, chunk.z) != null) {
					if (chunk.receiveUpdateFromNeighbour(0b10)) { // if this is true, the final update, then may as well queue it
						chunk.render = true;
						this.addToToAddToQueue(chunk);
					}
				}
			}
		}

		this.updateNeighbours(chunk.getPos());
	}

	private void updateNeighbours(final ChunkPos pos) {
		ClientChunk neighbour = this.getRenderChunk(pos.x, pos.z + 1);

		if (neighbour != null && !neighbour.render && neighbour.receiveUpdateFromNeighbour(0b1)) {
			neighbour.render = true;
			this.addToToAddToQueue(neighbour);
		}

		neighbour = this.getRenderChunk(pos.x + 1, pos.z);

		if (neighbour != null && !neighbour.render && neighbour.receiveUpdateFromNeighbour(0b10)) {
			neighbour.render = true;
			this.addToToAddToQueue(neighbour);
		}

		neighbour = this.getRenderChunk(pos.x, pos.z - 1);

		if (neighbour != null && !neighbour.render && neighbour.receiveUpdateFromNeighbour(0b100)) {
			neighbour.render = true;
			this.addToToAddToQueue(neighbour);
		}

		neighbour = this.getRenderChunk(pos.x - 1, pos.z);

		if (neighbour != null && !neighbour.render && neighbour.receiveUpdateFromNeighbour(0b1000)) {
			neighbour.render = true;
			this.addToToAddToQueue(neighbour);
		}
	}

	public boolean hasChunk(ClientChunk chunk) {
		return this.chunks.containsValue(chunk);
	}

	public void updateChunksForRendering() {
		this.offRenderTick = !this.offRenderTick;

		if (this.offRenderTick) {
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

		if (((ClientChunk) c).render) {
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
