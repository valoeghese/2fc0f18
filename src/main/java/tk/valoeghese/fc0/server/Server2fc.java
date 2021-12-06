package tk.valoeghese.fc0.server;

import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.server.world.ServerWorld;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.sound.SoundEffect;

import javax.annotation.Nullable;

public class Server2fc extends Game2fc<ServerWorld, Player> implements Runnable {
	@Override
	public void run() {

	}

	@Override
	public void playSound(@Nullable Player toExcept, SoundEffect effect, double x, double y, double z, float volume) {
		// TODO
	}

	@Override
	public boolean isMainThread() {
		return true; // TODO
	}
}
