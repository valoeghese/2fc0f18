package tk.valoeghese.fc0.client.render.gui.button;

import valoeghese.scalpel.gui.PseudoGUI;

/**
 * Interface for an interactable GUI button.
 */
public interface Button extends PseudoGUI {
	boolean isCursorSelecting(float x, float y);

	default boolean isCursorSelecting(float[] positions) {
		return this.isCursorSelecting(positions[0], positions[1]);
	}
}
