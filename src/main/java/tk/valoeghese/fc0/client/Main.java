package tk.valoeghese.fc0.client;

public class Main {
	public static void main(String[] args) {
		println("Starting 2fc0f18 Client.");
		Client2fc instance = new Client2fc();
		instance.run();
		// add post run handling here
		println("Client shut down.");
	}

	private static final <T> void println(T t) {
		System.out.println(t);
	}
}
