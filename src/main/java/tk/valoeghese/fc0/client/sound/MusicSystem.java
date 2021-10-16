package tk.valoeghese.fc0.client.sound;

import org.joml.Matrix4f;
import org.lwjgl.openal.AL11;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.screen.Screen;
import valoeghese.scalpel.audio.AudioBuffer;
import valoeghese.scalpel.audio.AudioSource;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class MusicSystem {
	@Nullable
	private static Optional<MusicSettings> currentMusic = null;
	private static long nextTime;
	private static AudioSource musicSource;
	private static float currentGain = 1.0f;
	private static float targetGain = 1.0f;

	public static void init() {
		musicSource = new AudioSource();

		for (MusicPiece piece : MusicPiece.PIECES) {
			piece.generateBuffers();
		}

		musicStarted = true;
	}

	public static void tick(Screen currentScreen) {
		if (currentMusic == null) { // startup
			currentMusic = currentScreen.getMusic();
			currentMusic.ifPresent(MusicSystem::play);
		} else {
			//if (Keybinds.JUMP.isPressed()) {
			//	System.out.println(nextTime+","+currentGain+","+targetGain+musicSource.isPlaying());
			//}
			if (!currentScreen.getMusic().equals(currentMusic)) {
				// Fade out
				targetGain = 0.0f;
				// Screw the timer
				nextTime = 0;
				// New Music
				currentMusic = currentScreen.getMusic();
			}

			if (targetGain == 0.0f) {
				if (currentGain <= 0.001f) {
					if (musicSource.isPlaying()) {
						AL11.alSourceStop(musicSource.source);
					}

					currentMusic.ifPresent(MusicSystem::play);
				}
			} else {
				if (System.currentTimeMillis() >= nextTime) {
					currentMusic.ifPresent(MusicSystem::play);
				}
			}

			if (currentGain > targetGain + 0.001f) {
				currentGain -= 0.01f;
				AL11.alSourcef(musicSource.source, AL11.AL_GAIN, currentGain);
			} else if (currentGain < targetGain - 0.001f) {
				currentGain += 0.01f;
				AL11.alSourcef(musicSource.source, AL11.AL_GAIN, currentGain);
			}
		}
	}

	private static void play(MusicSettings settings) {
		AudioBuffer piece = settings.selectPiece(Client2fc.RANDOM).getBuffer();

		// https://stackoverflow.com/questions/7978912/how-to-get-length-duration-of-a-source-with-single-buffer-in-openal
		int lengthSamples = AL11.alGetBufferi(piece.soundBuffer, AL11.AL_SIZE) * 8 /
				(AL11.alGetBufferi(piece.soundBuffer, AL11.AL_BITS) * AL11.alGetBufferi(piece.soundBuffer, AL11.AL_CHANNELS));
		float duration = (float) lengthSamples / (float) AL11.alGetBufferi(piece.soundBuffer, AL11.AL_FREQUENCY);

		nextTime = System.currentTimeMillis() + (long) (duration * 1000) + settings.selectDelay(Client2fc.RANDOM);
		targetGain = 1.0f;

		musicSource.attachBufferData(piece);
		musicSource.play();
	}

	public static void shutdown() {
		musicSource.destroy();
	}

	public static boolean shouldGenerateBuffers() {
		return musicStarted;
	}

	private static boolean musicStarted = false;
}
