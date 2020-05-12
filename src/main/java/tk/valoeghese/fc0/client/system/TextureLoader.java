package tk.valoeghese.fc0.client.system;

import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public final class TextureLoader {
	public static int textureARGB(BufferedImage image) {
		final int width = image.getWidth();
		final int height = image.getHeight();
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); // 4 bytes for ARGB
		int[] pixels = new int[width *  height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

		for (int u = 0; u < width; ++u) {
			for (int v = 0; v < height; ++v) {
				int pixel = pixels[v * width + u];
				// convert ARGB to RGBA
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // r
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // g
				buffer.put((byte) (pixel & 0xFF)); // b
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // a
			}
		}

		buffer.flip();

		int texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST_MIPMAP_NEAREST);
		glGenerateMipmap(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, 0);
		return texture;
	}
}
