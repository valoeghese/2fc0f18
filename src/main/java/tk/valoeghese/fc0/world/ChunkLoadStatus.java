package tk.valoeghese.fc0.world;

public enum ChunkLoadStatus {
	UNLOADED(-1, false),
	GENERATE(0, false),
	POPULATE(1, false),
	LIGHT(2, false),
	TICK(3, true),
	RENDER(4, true);

	ChunkLoadStatus(int i, boolean full) {
		this.i = i;
		this.full = full;
	}

	private final int i;
	private final boolean full;

	public boolean isFull() {
		return this.full;
	}

	public ChunkLoadStatus upgrade(ChunkLoadStatus status) {
		return status.i > this.i ? status : this;
	}
}
