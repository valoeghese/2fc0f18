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
	// counts extended over 2 in a row
	private int extendedMin;
	private int extendedMax;

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
		int prevCount = this.storage[this.nSlotsUsed - 1]; // the count of the previous one. nSlotsUsed cannot be 0 (so no AIoutofbounds) bc of increase at beginning, related to always at least having one at this point.

		for (int i = 0; i < this.nSlotsUsed; ++i) {
			int value = this.storage[i];
			// note to self the last check here means "the latest added value is not the one before this index" because I bet I'm gonna get confused reading this massive boolean logic statement later
			boolean dontUseChain = (i == 0 && (this.nSlotsUsed != this.storage.length || this.currentIndex == this.storage.length - 1)) || this.currentIndex == i - 1; // don't wrap chain if unuseable i.e. prev value is not in chain with this, including no complete data thus i=0 not in chain with last. last check means

			if (!dontUseChain) {
				int chain = prevCount + value;

				if (chain < lowestCount) lowestCount = chain;
				if (chain > highestCount) highestCount = chain;
			}

			totalCount += value;
			prevCount = value;
		}

		this.extendedMin = lowestCount;
		this.extendedMax = highestCount;
		this.average = (float) totalCount / (float) this.storage.length;
	}

	public void increment() {
		this.currentCount++;
	}

	public int getExtendedMin() {
		return this.extendedMin;
	}

	public int getExtendedMax() {
		return this.extendedMax;
	}

	/**
	 * @return the average rate of whatever this is measuring.
	 */
	public float getAverageRate() {
		return this.average;
	}
}
