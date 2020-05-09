package tk.valoeghese.fc0.client.system;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
	public Shader(String vertexResource, String fragmentResource) {
		// vertex
		int vertex = glCreateShader(GL_VERTEX_SHADER);

		try {
			glShaderSource(vertex, Resources.loadAsString(vertexResource));
		} catch (IOException e) {
			throw new UncheckedIOException("Error loading Vertex Shader Source!", e);
		}

		glCompileShader(vertex);
		int[] success = new int[1];
		glGetShaderiv(vertex,GL_COMPILE_STATUS,success);

		if (success[0] == 0) {
			throw new RuntimeException("Error compiling vertex shader", new RuntimeException(glGetShaderInfoLog(vertex)));
		}

		// fragment
		int fragment = glCreateShader(GL_FRAGMENT_SHADER);

		try {
			glShaderSource(fragment,Resources.loadAsString(fragmentResource));
		} catch (IOException e) {
			throw new UncheckedIOException("Error loading Fragment Shader Source!", e);
		}

		glCompileShader(fragment);
		glGetShaderiv(fragment, GL_COMPILE_STATUS, success);

		if (success[0] == 0) {
			throw new RuntimeException("Error compiling fragment shader", new RuntimeException(glGetShaderInfoLog(fragment)));
		}

		// link
		this.shaderId = glCreateProgram();

		glAttachShader(this.shaderId, vertex);
		glAttachShader(this.shaderId, fragment);
		glLinkProgram(this.shaderId);

		glGetProgramiv(this.shaderId, GL_LINK_STATUS, success);

		if (success[0] == 0) {
			throw new RuntimeException("Error linking shaders", new RuntimeException(glGetProgramInfoLog(this.shaderId)));
		}
	}

	private final int shaderId;
}
