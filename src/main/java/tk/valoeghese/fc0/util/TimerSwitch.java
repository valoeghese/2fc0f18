package tk.valoeghese.fc0.util;

public final class TimerSwitch {
	private long time = 0;
	private boolean on;

	public void switchOn(long milliDelay) {
		this.time = System.currentTimeMillis() + milliDelay;
		this.on = true;
	}

	public void update() {
		this.on = System.currentTimeMillis() < this.time;
	}

	public boolean isOn() {
		return this.on;
	}
}
