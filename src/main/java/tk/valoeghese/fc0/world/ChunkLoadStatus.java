package tk.valoeghese.fc0.world;

public enum ChunkLoadStatus {
	GENERATE(false),
	POPULATE(false),
	TICK(true),
	RENDER(true);

	ChunkLoadStatus(boolean full) {
		this.full = full;
	}

	private final boolean full;

	public boolean isFull() {
		return this.full;
	}
}
