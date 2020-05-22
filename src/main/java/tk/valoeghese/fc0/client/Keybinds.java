package tk.valoeghese.fc0.client;

import tk.valoeghese.fc0.client.keybind.Keybind;

import static org.lwjgl.glfw.GLFW.*;

public final class Keybinds {
	public static final Keybind MOVE_FORWARDS = new Keybind("move_forwards", GLFW_KEY_W, false);
	public static final Keybind MOVE_BACKWARDS = new Keybind("move_backwards", GLFW_KEY_S, false);
	public static final Keybind MOVE_LEFT = new Keybind("move_left", GLFW_KEY_A, false);
	public static final Keybind MOVE_RIGHT = new Keybind("move_right", GLFW_KEY_D, false);
	public static final Keybind ESCAPE = new Keybind("escape", GLFW_KEY_ESCAPE, false);
	public static final Keybind JUMP = new Keybind("jump", GLFW_KEY_SPACE, false);
	public static final Keybind RUN = new Keybind("run", GLFW_KEY_LEFT_SHIFT, false);
	public static final Keybind DESTROY = new Keybind("destroy", GLFW_MOUSE_BUTTON_1, true);
	public static final Keybind INTERACT = new Keybind("interact", GLFW_MOUSE_BUTTON_2, true);
	public static final Keybind RESPAWN = new Keybind("respawn", GLFW_KEY_R, false);
}
