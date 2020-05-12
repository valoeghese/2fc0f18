package tk.valoeghese.fc0.client.keybinds;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LAST;

public enum MousebindManager implements GLFWMouseButtonCallbackI {
	INSTANCE;

	static final Int2ObjectMap<List<Keybind>> mousebinds = new Int2ObjectArrayMap<>();

	@Override
	public void invoke(long window, int button, int action, int mods) {
		if (button < GLFW_MOUSE_BUTTON_LAST) { // in case mr megaman has 500 mouse buttons
			if (mousebinds.containsKey(button)) {
				for (Keybind k : mousebinds.get(button)) {
					k.update(action);
				}
			}
		}
	}
}
