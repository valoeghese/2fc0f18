package test;

import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.sound.SoundEffect;

public class Dummy2fc extends Game2fc {
	private Dummy2fc() {
	}

	@Override
	public boolean isMainThread() {
		return true;
	}

	@Override
	public void playSound(Player toExcept, SoundEffect effect, double x, double y, double z, float volume) {
	}

	public static void use() {
		new Dummy2fc();
	}
}
