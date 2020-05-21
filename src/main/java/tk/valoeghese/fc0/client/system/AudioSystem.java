package tk.valoeghese.fc0.client.system;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTEfx.ALC_MAX_AUXILIARY_SENDS;

public final class AudioSystem {
	private AudioSystem() {
	}

	public static void initAL() {
		device = alcOpenDevice((ByteBuffer) null);

		ALCCapabilities capabilities = ALC.createCapabilities(device);
		IntBuffer attributes = BufferUtils.createIntBuffer(16);
		// I don't know what half these parameters do
		attributes.put(ALC_REFRESH);
		attributes.put(60);

		attributes.put(ALC_SYNC);
		attributes.put(ALC_FALSE);

		attributes.put(ALC_MAX_AUXILIARY_SENDS);
		attributes.put(2);

		attributes.put(0);
		attributes.flip();

		context = alcCreateContext(device, attributes);

		if (!alcMakeContextCurrent(context)) {
			throw new RuntimeException("Error making audio context current while initialising OpenAL!");
		}

		AL.createCapabilities(capabilities);
	}

	private static long device;
	private static long context;

	public static long getContext() {
		return context;
	}

	public static long getDevice() {
		return device;
	}
}
