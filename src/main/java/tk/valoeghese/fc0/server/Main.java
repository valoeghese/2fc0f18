package tk.valoeghese.fc0.server;

import com.sun.corba.se.spi.activation.Server;

public class Main {
	private static void main(String[] args) {
		System.out.println("Starting 2fc0f18 server.");
		new Server2fc().run();
		System.out.println("Server shut down.");
	}
}
