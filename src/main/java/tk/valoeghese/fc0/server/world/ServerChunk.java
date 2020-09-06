package tk.valoeghese.fc0.server.world;

import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;

import javax.annotation.Nullable;

public class ServerChunk extends Chunk {
	public ServerChunk(ChunkAccess parent, int x, int z, byte[] tiles, byte[] meta, @Nullable int[] kingdoms) {
		super(parent, x, z, tiles, meta, kingdoms);
	}
}
