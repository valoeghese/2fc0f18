package tk.valoeghese.fc0.client.system;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.opengl.GL.createCapabilities;

public final class GraphicsSystem {
    public static void initGLFW() {
        if (!glfwInit()) {
            throw new RuntimeException("Error initialising GLFW");
        }
    }

    public static void initGL() {
    	createCapabilities();
    }

    public static final int NULL = 0;
}
