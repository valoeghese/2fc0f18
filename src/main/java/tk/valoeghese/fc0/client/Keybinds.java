package tk.valoeghese.fc0.client;

import tk.valoeghese.fc0.client.keybind.Keybind;

import static org.lwjgl.glfw.GLFW.*;

public final class Keybinds {
	public static final Keybind MOVE_FOWARDS = new Keybind("move_fowards", GLFW_KEY_W, false);
	public static final Keybind MOVE_BACKWARDS = new Keybind("move_backwards", GLFW_KEY_S, false);
	public static final Keybind MOVE_LEFT = new Keybind("move_left", GLFW_KEY_A, false);
	public static final Keybind MOVE_RIGHT = new Keybind("move_right", GLFW_KEY_D, false);
}
