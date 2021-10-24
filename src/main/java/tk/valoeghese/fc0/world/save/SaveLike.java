package tk.valoeghese.fc0.world.save;

import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.player.Item;
import tk.valoeghese.fc0.world.player.Player;

import java.util.Iterator;

public interface SaveLike {
	void writeChunks(Iterator<? extends Chunk> chunks);
	void writeForClient(Player player, GameplayWorld world, Iterator<Item> inventory, int invSize, Pos playerPos, Pos spawnPos, long time);
	<T extends Chunk> void loadChunk(WorldGen worldGen, ChunkLoadingAccess<T> parent, int x, int z, WorldGen.ChunkConstructor<T> constructor, ChunkLoadStatus status);
}
