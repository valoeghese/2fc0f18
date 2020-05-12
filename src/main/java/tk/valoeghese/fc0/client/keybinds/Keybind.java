package tk.valoeghese.fc0.client.keybinds;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public final class Keybind {
	public Keybind(int key, boolean mouse) {
		this.bind(key, mouse);
	}

	private boolean isPressed = false;
	private boolean hasBeenPressed = false;
	private int key;
	private boolean mouse;

	void update(int action) {
		isPressed = action != GLFW_RELEASE;

		if (action == GLFW_PRESS) {
			hasBeenPressed = true;
		}
	}

	private void bind(int key, boolean mouse) {
		this.key = key;
		this.mouse = mouse;

		if (mouse) {
			KeybindManager.keybinds
					.computeIfAbsent(key, i -> new ArrayList<>())
					.add(this);
		} else {
			MousebindManager.mousebinds
					.computeIfAbsent(key, i -> new ArrayList<>())
					.add(this);
		}
	}

	public void rebind(int newKey, boolean newMouse) {
		if (this.mouse) {
			MousebindManager.mousebinds.get(this.key).remove(this);
		} else {
			KeybindManager.keybinds.get(this.key).remove(this);
		}

		this.bind(newKey, newMouse);
	}

	public boolean isPressed() {
		return this.isPressed;
	}

	public boolean hasBeenPressed() {
		if (this.hasBeenPressed) {
			this.hasBeenPressed = false;
			return true;
		} else {
			return false;
		}
	}
}
