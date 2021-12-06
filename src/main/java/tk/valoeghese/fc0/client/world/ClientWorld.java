package tk.valoeghese.fc0.client.world;

import tk.valoeghese.fc0.util.maths.ChunkPos;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.save.SaveLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientWorld extends GameplayWorld<ClientChunk> {
	public ClientWorld(SaveLike save, long seed, int size) {
		super(save, seed, size, ClientChunk::new);
	}

	private final List<ClientChunk> renderingChunks = new ArrayList<>();
	private final Set<ChunkPos> renderingAt = new HashSet<>(); // TODO is this still necessary? I think I have a failsafe for duplicates in the actual chunkloading so this could be changed.

	private void addRenderChunk(ClientChunk chunk) {
		if (chunk != null && this.renderingAt.add(chunk.getPos())) { // if did not already contain, add to queue
			chunk.render = true;
			this.renderingChunks.add(chunk);
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
					chunk.markNeighbourLoaded(0b100);
				}
				if (this.getChunk(chunk.x + 1, chunk.z) != null) {
					chunk.markNeighbourLoaded(0b1000);
				}
				if (this.getChunk(chunk.x, chunk.z - 1) != null) {
					chunk.markNeighbourLoaded(0b1);
				}
				if (this.getChunk(chunk.x - 1, chunk.z) != null) {
					if (chunk.markNeighbourLoaded(0b10)) { // if this is true, the final update, then may as well queue it
						this.addRenderChunk(chunk);
					}
				}
			}
		}

		this.updateNeighbours(chunk.getPos());
	}

	// when a chunk loads check all surrounding chunks
	// if those chunks now have all sides covered they can render
	// should I make it require LIGHTING?
	private void updateNeighbours(final ChunkPos pos) {
		ClientChunk neighbour = this.getRenderChunk(pos.x, pos.z + 1);

		if (neighbour != null && !neighbour.render && neighbour.markNeighbourLoaded(0b1)) {
			neighbour.render = true;
			this.addRenderChunk(neighbour);
		}

		neighbour = this.getRenderChunk(pos.x + 1, pos.z);

		if (neighbour != null && !neighbour.render && neighbour.markNeighbourLoaded(0b10)) {
			neighbour.render = true;
			this.addRenderChunk(neighbour);
		}

		neighbour = this.getRenderChunk(pos.x, pos.z - 1);

		if (neighbour != null && !neighbour.render && neighbour.markNeighbourLoaded(0b100)) {
			neighbour.render = true;
			this.addRenderChunk(neighbour);
		}

		neighbour = this.getRenderChunk(pos.x - 1, pos.z);

		if (neighbour != null && !neighbour.render && neighbour.markNeighbourLoaded(0b1000)) {
			neighbour.render = true;
			this.addRenderChunk(neighbour);
		}
	}

	public boolean hasChunk(ClientChunk chunk) {
		return this.chunks.containsValue(chunk);
	}

	public List<ClientChunk> getRenderingChunks() {
		return this.renderingChunks;
	}

	@Override
	protected void onChunkRemove(Chunk c) {
		c.destroy();

		if (((ClientChunk) c).render) {
			this.renderingAt.remove(c.getPos());
			this.renderingChunks.remove(c);
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		for (Chunk c : this.renderingChunks) {
			c.destroy();
			this.renderingAt.remove(c.getPos()); // justin case
		}
	}
}
