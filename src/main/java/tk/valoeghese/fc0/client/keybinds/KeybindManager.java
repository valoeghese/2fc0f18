package tk.valoeghese.fc0.client.keybinds;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;

public enum KeybindManager implements GLFWKeyCallbackI {
	INSTANCE;

	static final Int2ObjectMap<List<Keybind>> keybinds = new Int2ObjectArrayMap<>();

	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if (key < GLFW_KEY_LAST) { // yes this can happen. Volume button.
			if (keybinds.containsKey(key)) {
				for (Keybind k : keybinds.get(key)) {
					k.update(action);
				}
			}
		}
	}
}
