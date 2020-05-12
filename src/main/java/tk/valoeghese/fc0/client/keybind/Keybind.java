package tk.valoeghese.fc0.client.keybind;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public final class Keybind {
	public Keybind(String name, int key, boolean mouse) {
		this.name = name;
		this.bind(key, mouse);
	}

	private boolean isPressed = false;
	private boolean hasBeenPressed = false;
	private int key;
	private boolean mouse;
	private final String name;

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
			MousebindManager.mousebinds
					.computeIfAbsent(key, i -> new ArrayList<>())
					.add(this);
		} else {
			KeybindManager.keybinds
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

	@Override
	public String toString() {
		return "keybind." + this.name;
	}
}
