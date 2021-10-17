package tk.valoeghese.fc0.client.sound;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * The music settings for a screen.
 * Delays are in tenths of a second.
 */
public record MusicSettings(Supplier<List<MusicPiece>> pieces, long minDel, long maxDel, float gain) {
	public long selectDelay(Random rand) {
		return 100 * (rand.nextInt((int) (this.maxDel - this.minDel)) + this.minDel);
	}

	@Nullable
	public MusicPiece selectPiece(Random rand) {
		List<MusicPiece> available = pieces.get();
		return available.isEmpty() ? null : available.get(rand.nextInt(available.size()));
	}

	public static MusicSettings createFixed(List<MusicPiece> pieces, long minDel, long maxDel) {
		return new MusicSettings(() -> pieces, minDel, maxDel, 1.0f);
	}
}
