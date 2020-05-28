package tk.valoeghese.fc0.world.save;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.sod.BinaryData;
import tk.valoeghese.sod.DataSection;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;

public class Save {
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
		} else {
			this.seed = seed;
			this.lastSavePos = null;
			this.spawnLocPos = null;
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

	public long getSeed() {
		return this.seed;
	}

	public void write(Chunk[] chunks, Pos playerPos, Pos spawnPos, long time) {
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

			for (Chunk c : chunks) {
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

				DataSection playerData = new DataSection();
				playerData.writeDouble(playerPos.getX());
				playerData.writeDouble(playerPos.getY());
				playerData.writeDouble(playerPos.getZ());
				playerData.writeDouble(spawnPos.getX());
				playerData.writeDouble(spawnPos.getY());
				playerData.writeDouble(spawnPos.getZ());
				data.put("player", playerData);

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
