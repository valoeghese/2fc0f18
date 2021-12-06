package tk.valoeghese.fc0.server;

import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.server.world.ServerWorld;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.sound.SoundEffect;

public class Server2fc extends Game2fc<ServerWorld, Player> implements Runnable {
	@Override
	public void run() {

	}

	@Override
	public void playSound(SoundEffect effect, double x, double y, double z) {
		// TODO
	}

	@Override
	public boolean isMainThread() {
		return true; // TODO
	}
}
