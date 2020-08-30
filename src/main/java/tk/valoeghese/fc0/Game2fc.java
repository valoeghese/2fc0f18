package tk.valoeghese.fc0;

import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.player.Player;

import java.util.Random;

public class Game2fc<W extends World, P extends Player> {
	public long time = 0;
	protected W world;
	protected P player;

	protected void tick() {
		this.player.tick();

		++this.time;
	}

	public static final Random RANDOM = new Random();
}
