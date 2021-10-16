package tk.valoeghese.fc0.client.sound;

import java.util.List;
import java.util.Random;

/**
 * The music settings for a screen.
 * Delays are in tenths of a second.
 */
public record MusicSettings(List<MusicPiece> pieces, long minDel, long maxDel) {
	public long selectDelay(Random rand) {
		return 100 * rand.nextInt((int) (this.maxDel - this.minDel)) + this.minDel;
	}
	public MusicPiece selectPiece(Random rand) {
		return this.pieces.get(rand.nextInt(this.pieces.size()));
	}
}
