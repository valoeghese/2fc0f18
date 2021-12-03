package tk.valoeghese.fc0.client;

import java.util.Arrays;

public class Main {
	public static void main(String[] args) {
		println("Starting 2fc0f18 Client.");
		Thread.currentThread().setName("client-main");

		boolean dev = false, noclip = false;

		for (String s : args) {
			if (s.equals("-d") || s.equals("--dev")) dev = true;
			else if (s.equals("-nc") || s.equals("--no-clip")) noclip = true;
		}

		Client2fc instance = new Client2fc(dev, noclip);
		instance.run();
		// add post run handling here
		println("Client shut down.");
	}

	private static final <T> void println(T t) {
		System.out.println(t);
	}
}
