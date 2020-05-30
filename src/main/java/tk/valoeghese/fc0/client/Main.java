package tk.valoeghese.fc0.client;

public class Main {
	public static void main(String[] args) {
		println("Starting 2fc0f18 Client.");
		/*for (int x = -2; x < 3; ++x) {
			for (int z = -2; z < 3; ++z) {
				println("x = " + x + ", z = " + z + ", chunk key = " + GameplayWorld.chunkKey(x, z));
			}
		}*/
		new Client2fc().run();
	}

	private static final <T> void println(T t) {
		System.out.println(t);
	}
}
