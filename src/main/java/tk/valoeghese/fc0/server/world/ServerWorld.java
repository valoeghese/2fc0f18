package tk.valoeghese.fc0.server.world;

import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.save.Save;

import javax.annotation.Nullable;

public class ServerWorld extends GameplayWorld<ServerChunk> {
	public ServerWorld(@Nullable Save save, long seed, int size, WorldGen.ChunkConstructor<ServerChunk> constructor) {
		super(save, seed, size, constructor);
	}

	@Override
	protected void onChunkRemove(Chunk c) {
	}
}
