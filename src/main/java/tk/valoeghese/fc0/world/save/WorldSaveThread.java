package tk.valoeghese.fc0.world.save;

public class WorldSaveThread extends Thread {
	public WorldSaveThread(Runnable r) {
		super(r);
	}

	private static boolean ready = false;

	@Override
	public void run() {
		ready = false;
		super.run();
	}

	static void setReady() {
		ready = true;
	}

	static boolean isReady() {
		return ready;
	}
}
