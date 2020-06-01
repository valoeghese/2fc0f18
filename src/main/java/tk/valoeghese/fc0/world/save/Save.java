package tk.valoeghese.fc0.world.save;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.player.Item;
import tk.valoeghese.sod.BinaryData;
import tk.valoeghese.sod.DataSection;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Random;

public class Save {
	// client specific stuff is here. will need to change on server
	public Save(String name, long seed) {
		this.parentDir = new File("./saves/" + name);
		this.parentDir.mkdirs();

		this.saveDat = new File(this.parentDir, "save.gsod");

		if (this.saveDat.exists()) {
			BinaryData data = BinaryData.readGzipped(this.saveDat);
			DataSection mainData = data.get("data");
			this.seed = mainData.readLong(0);
			Client2fc.getInstance().time = mainData.readLong(1);

			DataSection playerData = data.get("player");
			this.lastSavePos = new Pos(playerData.readDouble(0), playerData.readDouble(1), playerData.readDouble(2));
			this.spawnLocPos = new Pos(playerData.readDouble(3), playerData.readDouble(4), playerData.readDouble(5));

			if (data.containsSection("playerInventory")) {
				DataSection playerInventoryData = data.get("playerInventory");
				this.loadedInventory = this.loadInventory(playerInventoryData);
			} else {
				this.loadedInventory = null;
			}
		} else {
			this.seed = seed;
			this.lastSavePos = null;
			this.spawnLocPos = null;
			this.loadedInventory = null;
		}
	}

	private final File parentDir;
	private final File saveDat;
	private final long seed;
	@Nullable
	public final Pos lastSavePos;
	@Nullable
	public final Pos spawnLocPos;
	private static WorldSaveThread thread;
	private static final Object lock = new Object();
	@Nullable
	public final Item[] loadedInventory;

	public long getSeed() {
		return this.seed;
	}

	public void writeChunks(Iterator<? extends Chunk> chunks) {
		synchronized (lock) {
			try {
				while (thread != null && !thread.isReady()) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		thread = new WorldSaveThread(() -> {
			while (chunks.hasNext()) {
				Chunk c = chunks.next();

				if (c != null) {
					this.saveChunk(c);
				}
			}

			try {
				Thread.sleep(5); // pls fix save loading bugs
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (lock) {
				WorldSaveThread.setReady();
				lock.notifyAll();
			}
		});

		thread.start();
	}

	public void writeForClient(Iterator<? extends Chunk> chunks, Iterator<Item> inventory, int invSize, Pos playerPos, Pos spawnPos, long time) {
		synchronized (lock) {
			try {
				while (thread != null && !thread.isReady()) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		thread = new WorldSaveThread(() -> {
			System.out.println("Saving Chunks");

			while (chunks.hasNext()) {
				Chunk c = chunks.next();

				if (c != null) {
					this.saveChunk(c);
				}
			}

			try {
				this.saveDat.createNewFile();
				BinaryData data = new BinaryData();
				DataSection mainData = new DataSection();
				mainData.writeLong(this.seed);
				mainData.writeLong(time);
				data.put("data", mainData);

				// the "self" player, for the client version, is the only player stored
				DataSection clientPlayerData = new DataSection();
				clientPlayerData.writeDouble(playerPos.getX());
				clientPlayerData.writeDouble(playerPos.getY());
				clientPlayerData.writeDouble(playerPos.getZ());
				clientPlayerData.writeDouble(spawnPos.getX());
				clientPlayerData.writeDouble(spawnPos.getY());
				clientPlayerData.writeDouble(spawnPos.getZ());
				data.put("player", clientPlayerData);

				DataSection clientPlayerInventory = new DataSection();
				this.storeInventory(clientPlayerInventory, inventory, invSize);
				data.put("playerInventory", clientPlayerInventory);

				data.writeGzipped(this.saveDat);
			} catch (IOException e) {
				throw new UncheckedIOException("Error writing save data", e);
			}

			try {
				Thread.sleep(5); // pls fix save loading bugs
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (lock) {
				WorldSaveThread.setReady();
				lock.notifyAll();
			}
		});

		thread.start();
	}

	private void storeInventory(DataSection playerInventoryData, Iterator<Item> inventory, int size) {
		playerInventoryData.writeInt(size);

		for (; inventory.hasNext();) {
			Item item = inventory.next();

			if (item == null) { // for compactness just write 0
				playerInventoryData.writeInt(0);
			} else {
				playerInventoryData.writeInt(item.id());
				playerInventoryData.writeByte(item.getMeta());
				playerInventoryData.writeInt(item.getCount());
			}
		}
	}

	private Item[] loadInventory(DataSection playerInventoryData) {
		int readIndex = 0;
		int slot = 0;
		int size = playerInventoryData.readInt(readIndex++);
		Item[] result = new Item[size];

		while (size --> 0) {
			int id = playerInventoryData.readInt(readIndex++);

			if (id == 0) {
				result[slot++] = null;
			} else {
				result[slot++] = new Item(id, playerInventoryData.readByte(readIndex++), playerInventoryData.readInt(readIndex++));
			}
		}

		return result;
	}

	public <T extends Chunk> T getOrCreateChunk(ChunkAccess parent, int x, int z, WorldGen.ChunkConstructor<T> constructor) {
		synchronized (lock) {
			try {
				while (thread != null && !thread.isReady()) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		File file = new File(this.parentDir, "c" + x + "." + z + ".gsod");

		if (file.exists()) {
			return Chunk.read(parent, constructor, BinaryData.readGzipped(file));
		} else {
			Random genRand = new Random(parent.getSeed() + 134 * x + -529 * z);
			return WorldGen.generateChunk(constructor, parent, x, z, parent.getSeed(), genRand);
		}
	}

	private void saveChunk(Chunk chunk) {
		// only save modified chunks
		if (chunk.isDirty()) {
			File file = new File(this.parentDir, "c" + chunk.x + "." + chunk.z + ".gsod");

			try {
				file.createNewFile();
				BinaryData data = new BinaryData();
				chunk.write(data);
				data.writeGzipped(file);
			} catch (IOException e) {
				throw new UncheckedIOException("Error writing chunk! " + chunk.getPos().toString(), e);
			}
		}
	}
}
