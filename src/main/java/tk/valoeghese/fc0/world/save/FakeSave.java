package tk.valoeghese.fc0.world.save;

import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.player.Item;
import tk.valoeghese.fc0.world.player.Player;

import java.util.Iterator;
import java.util.Random;

public class FakeSave implements SaveLike {
	public FakeSave(long seed) {
		this.seed = seed;
	}

	private final long seed;
	private final Random genRand = new Random();

	@Override
	public void writeChunks(Iterator<? extends Chunk> chunks) {
	}

	@Override
	public void writeForClient(Player player, GameplayWorld world, Iterator<Item> inventory, int invSize, Pos playerPos, Pos spawnPos, long time) {
	}

	@Override
	public <T extends Chunk> void loadChunk(WorldGen worldGen, ChunkLoadingAccess<T> parent, int x, int z, WorldGen.ChunkConstructor<T> constructor, ChunkLoadStatus status) {
		this.genRand.setSeed(this.seed + 134 * x + -529 * z);
		T chunk = worldGen.generateChunk(constructor, parent, x, z, this.genRand);
		Game2fc.getInstance().runLater(() -> parent.addUpgradedChunk(chunk, status));
	}
}
