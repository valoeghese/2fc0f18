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
	public static final Keybind FOV_UP = new Keybind("fov_up", GLFW_KEY_RIGHT_BRACKET, false);
	public static final Keybind FOV_DOWN = new Keybind("fov_down", GLFW_KEY_LEFT_BRACKET, false);
	public static final Keybind SELECT_0 = new Keybind("select_0", GLFW_KEY_0, false);
	public static final Keybind SELECT_1 = new Keybind("select_1", GLFW_KEY_1, false);
	public static final Keybind SELECT_2 = new Keybind("select_2", GLFW_KEY_2, false);
	public static final Keybind SELECT_3 = new Keybind("select_3", GLFW_KEY_3, false);
	public static final Keybind SELECT_4 = new Keybind("select_4", GLFW_KEY_4, false);
	public static final Keybind SELECT_5 = new Keybind("select_5", GLFW_KEY_5, false);
	public static final Keybind SELECT_6 = new Keybind("select_6", GLFW_KEY_6, false);
	public static final Keybind SELECT_7 = new Keybind("select_7", GLFW_KEY_7, false);
	public static final Keybind SELECT_8 = new Keybind("select_8", GLFW_KEY_8, false);
	public static final Keybind SELECT_9 = new Keybind("select_9", GLFW_KEY_9, false);
	public static final Keybind INVENTORY = new Keybind("inventory", GLFW_KEY_I, false);

	// Dev Only Commands

	// -nc flag
	public static final Keybind NO_CLIP = new Keybind("no_clip", GLFW_KEY_G, false);
	public static final Keybind NO_CLIP_DOWN = new Keybind("no_clip_down", GLFW_KEY_LEFT_CONTROL, false);

	// -d flag
	public static final Keybind DEV_MODE = new Keybind("dev_mode", GLFW_KEY_F1, false);
	public static final Keybind DEV_ITEMS = new Keybind("dev_items", GLFW_KEY_F8, false);
	public static final Keybind HIDE_DEBUG = new Keybind("hide_debug", GLFW_KEY_TAB, false);
	public static final Keybind REMOVE_TIME = new Keybind("remove_time", GLFW_KEY_F2, false);
	public static final Keybind ADD_TIME = new Keybind("add_time", GLFW_KEY_F3, false);
	public static final Keybind HIDE_WORLD = new Keybind("hide_world", GLFW_KEY_F4, false);
}
