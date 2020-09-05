package tk.valoeghese.fc0.client.render.system;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTEfx.ALC_MAX_AUXILIARY_SENDS;
import static tk.valoeghese.fc0.client.render.system.util.GLUtils.NULL;

public final class Audio {
	private static boolean active;

	private Audio(int id, String file) {
		// TODO actually touch the file object
		this.id = id;
		ByteBuffer data = MemoryUtil.memAlloc(8192);
		IntBuffer memoryConsumed = MemoryStack.stackMallocInt(1);
		IntBuffer error = MemoryStack.stackMallocInt(1);

		if (STBVorbis.stb_vorbis_open_pushdata(data, memoryConsumed, error, null) == NULL) {
			throw new RuntimeException(String.valueOf(alGetError()));
		}

		alBufferData(this.id, AL_FORMAT_STEREO16, data, 44100);
	}

	public final int id;
	private static final IntList sources = new IntArrayList();

	public static void shutdown() {
		if (active) {
			alcCloseDevice(Audio.getDevice());
		}
	}

	public void playAudio(float pitch, float gain, float x, float y, float z) {
		int source = AL10.alGenSources();
		alSourcef(source, AL_PITCH, pitch);
		alSourcef(source, AL_GAIN, gain);
		alSource3f(source, AL_POSITION, x, y, z);
		alSourcei(source, AL_LOOPING, AL_FALSE);
		alSourcei(source, AL_BUFFER, this.id);
		alSourcePlay(source);
	}

	public static void tickAudio() {
		for (int source : sources) {
			if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
				alDeleteSources(source);
			}
		}
	}

	public static void initAL() {
		try{
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
			active = true;
		}catch (Exception e){
			System.out.println("Open AL was unable to start.");
		}
	}

	public static Audio createAudioSystem(String file) {
		return new Audio(AL10.alGenBuffers(), file);
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
