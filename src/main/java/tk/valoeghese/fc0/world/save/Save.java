package tk.valoeghese.fc0.world.save;

import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.world.chunk.Chunk;
import tk.valoeghese.fc0.world.chunk.ChunkLoadStatus;
import tk.valoeghese.fc0.world.GameplayWorld;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.fc0.world.player.Item;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.sod.BinaryData;
import tk.valoeghese.sod.DataSection;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Save implements SaveLike {
	// client specific stuff is here. will need to change on server
	public Save(String name, long seed) {
		this.parentDir = new File("./saves/" + name);
		this.parentDir.mkdirs();

		this.saveDat = new File(this.parentDir, "save.gsod");

		boolean devMode = false;
		int hp = 100;
		int maxHp = 100;

		if (this.saveDat.exists()) {
			BinaryData data = BinaryData.readGzipped(this.saveDat);
			DataSection mainData = data.get("data");
			this.seed = mainData.readLong(0);
			Client2fc.getInstance().time = mainData.readLong(1);

			DataSection playerData = data.get("player");
			this.lastSavePos = new Pos(playerData.readDouble(0), playerData.readDouble(1), playerData.readDouble(2));
			this.spawnLocPos = new Pos(playerData.readDouble(3), playerData.readDouble(4), playerData.readDouble(5));

			try {
				devMode = playerData.readBoolean(6);
				hp = playerData.readInt(7);
				maxHp = playerData.readInt(8);
			} catch (Exception ignored) {
				//ignored.printStackTrace();
				// @reason compat between save versions
			}

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

		this.loadedDevMode = devMode;
		this.loadedHP = hp;
		this.loadedMaxHP = maxHp;
	}

	private final File parentDir;
	private final File saveDat;
	private final long seed;
	@Nullable
	public final Pos lastSavePos;
	@Nullable
	public final Pos spawnLocPos;
	@Nullable
	public final Item[] loadedInventory;
	public final boolean loadedDevMode;
	public final int loadedHP;
	public final int loadedMaxHP;

	// count stuff and the executor
	private static ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
	private static int count = 0;
	private static final Object COUNT_LOCK = new Object();
	private static final Object READ_WRITE_LOCK = new Object();

	public long getSeed() {
		return this.seed;
	}

	public static boolean isThreadAlive() {
		synchronized (COUNT_LOCK) {
			return count > 0;
		}
	}

	@Override
	public void writeChunks(Iterator<? extends Chunk> chunks) {
		synchronized (COUNT_LOCK) {
			count++;
		}

		saveExecutor.submit(() -> {
			synchronized (READ_WRITE_LOCK) {
				while (chunks.hasNext()) {
					Chunk c = chunks.next();

					if (c != null) {
						this.saveChunk(c);
					}
				}
			}

			try {
				Thread.sleep(5); // pls fix save loading bugs TODO can I remove this now?
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (COUNT_LOCK) {
				count--;
			}
		});
	}

	@Override
	public void writeForClient(Player player, GameplayWorld world, Iterator<Item> inventory, int invSize, Pos playerPos, Pos spawnPos, long time) {
		Iterator<? extends Chunk> chunks = world.getChunks();

		synchronized (COUNT_LOCK) {
			count++;
		}

		saveExecutor.submit(() -> {
			System.out.println("Saving Chunks");

			synchronized (READ_WRITE_LOCK) {
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
					clientPlayerData.writeBoolean(player.dev);
					clientPlayerData.writeInt(player.getHealth());
					clientPlayerData.writeInt(player.getMaxHealth());
					data.put("player", clientPlayerData);

					DataSection clientPlayerInventory = new DataSection();
					this.storeInventory(clientPlayerInventory, inventory, invSize);
					data.put("playerInventory", clientPlayerInventory);

					data.writeGzipped(this.saveDat);
				} catch (IOException e) {
					throw new UncheckedIOException("Error writing save data", e);
				}
			}

			try {
				Thread.sleep(5); // pls fix save loading bugs
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (COUNT_LOCK) {
				count--;
			}
		});
	}

	private void storeInventory(DataSection playerInventoryData, Iterator<Item> inventory, int size) {
		playerInventoryData.writeInt(size);

		while (inventory.hasNext()) {
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

	@Override
	public <T extends Chunk> void loadChunk(WorldGen worldGen, ChunkLoadingAccess<T> parent, int x, int z, WorldGen.ChunkConstructor<T> constructor, ChunkLoadStatus status) {
		File folder = new File(this.parentDir, x + "/" + z);
		File file = new File(folder, "c" + x + "." + z + ".gsod");

		// don't generate in R_W_LOCK but do ALL file operations there including exists() just in case tm
		// TODO should loading be off-thread too?
		synchronized (READ_WRITE_LOCK) {
			if (file.exists()) {
				try {
					saveExecutor.submit(() -> {
						T chunk = Chunk.read(parent, constructor, BinaryData.readGzipped(file));
						Game2fc.getInstance().runLater(() -> parent.addLoadedChunk(chunk, status));
					});
					return;
				} catch (Exception e) {
					System.err.println("Error loading chunk at " + x + ", " + z + "! Possible corruption? Regenerating Chunk.");
					file.renameTo(new File(file.getParentFile(), "CORRUPTED_" + Game2fc.RANDOM.nextInt() + "c" + x + "." + z + ".gsod"));
					// and generate
				}
			}
		}

		Random genRand = new Random(parent.getSeed() + 134 * x + -529 * z);
		parent.addLoadedChunk(worldGen.generateChunk(constructor, parent, x, z, genRand), status);
	}

	private void saveChunk(Chunk chunk) {
		// only save modified chunks
		if (chunk.isDirty()) {
			File folder = new File(this.parentDir, chunk.x + "/" + chunk.z);
			folder.mkdirs();
			File file = new File(folder, "c" + chunk.x + "." + chunk.z + ".gsod");

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

	public static void shutdown() {
		System.out.println("Shutting Down Save Thread");
		saveExecutor.shutdown();

		try {
			if (!saveExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				System.out.println("...Taking too long! Forcing Save Thread Shutdown");
				System.exit(0);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
