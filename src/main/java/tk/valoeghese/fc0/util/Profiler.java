package tk.valoeghese.fc0.util;

/**
 * A naïve profiler for basic per-second profiling (FPS, TPS).
 */
public class Profiler {
	public Profiler(int buffer) {
		this.storage = new int[buffer];
	}

	private final int storage[];
	private int currentIndex;
	private int currentCount; // current count in order to not interfere with calculations.
	private int nSlotsUsed; // used as the profiler is filling up to make more accurate values. so maybe this profiler is slightly less naïve

	private float average;
	private int minValue;
	private int maxValue;

	public void next() {
		// increase
		this.nSlotsUsed = Math.min(this.nSlotsUsed + 1, this.storage.length);

		// store data and reset current count
		this.storage[this.currentIndex++] = this.currentCount;
		this.currentCount = 0;

		// loop back (wrap)
		if (this.currentIndex == this.storage.length) {
			this.currentIndex = 0;
		}

		// update min/max
		// was going to check (if this.minIndex == prevIndex) but then realised I'm recomputing the average anyway so no major performance boost from that
		int lowestCount = Integer.MAX_VALUE; // always override the index/count
		int highestCount = 0;
		int totalCount = 0;

		for (int i = 0; i < this.nSlotsUsed; ++i) {
			int value = this.storage[i];

			if (value < lowestCount) lowestCount = value;
			if (value > highestCount) highestCount = value;

			totalCount += value;
		}

		this.minValue = lowestCount;
		this.maxValue = highestCount;
		this.average = (float) totalCount / (float) this.storage.length;
	}

	public void increment() {
		this.currentCount++;
	}

	public int getMin() {
		return this.minValue;
	}
	public int getMax() {
		return this.maxValue;
	}

	/**
	 * @return the average rate of whatever this is measuring.
	 */
	public float getAverageRate() {
		return this.average;
	}
}
