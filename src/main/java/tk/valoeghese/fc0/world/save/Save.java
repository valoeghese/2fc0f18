package tk.valoeghese.fc0.world.save;

import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.ChunkAccess;
import tk.valoeghese.fc0.world.ChunkSelection;
import tk.valoeghese.fc0.world.gen.WorldGen;
import tk.valoeghese.sod.BinaryData;
import tk.valoeghese.sod.DataSection;

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
			BinaryData data = BinaryData.read(saveDat, false);
			this.seed = data.get("data").readLong(0);
		} else {
			this.seed = seed;
		}
	}

	private final File parentDir;
	private final File saveDat;
	private final long seed;
	public static WorldSaveThread thread;
	private static final Object lock = new Object();

	public long getSeed() {
		return this.seed;
	}

	public void write(ChunkSelection<?> world) {
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

			for (Chunk c : world.getChunks()) {
				if (c != null) {
					this.saveChunk(c);
				}
			}

			try {
				this.saveDat.createNewFile();
				BinaryData data = new BinaryData();
				DataSection mainData = new DataSection();
				mainData.writeLong(this.seed);
				data.put("data", mainData);
				data.write(this.saveDat);
			} catch (IOException e) {
				throw new UncheckedIOException("Error writing save data", e);
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
			return Chunk.read(parent, constructor, BinaryData.read(file, false));
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
			data.write(file);
		} catch (IOException e) {
			throw new UncheckedIOException("Error writing chunk! " + chunk.getPos().toString(), e);
		}
	}
}
