package tk.valoeghese.fc0.util;

public class ReadiableThread extends Thread {
	public ReadiableThread(Runnable r) {
		super(r);
	}

	private static boolean ready = false;

	@Override
	public void run() {
		ready = false;
		super.run();
	}

	public static void setReady() {
		ready = true;
	}

	public static boolean isReady() {
		return ready;
	}
}
