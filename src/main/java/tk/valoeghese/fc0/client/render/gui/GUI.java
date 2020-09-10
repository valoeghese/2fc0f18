package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.render.Shaders;
import valoeghese.scalpel.Shader;
import valoeghese.scalpel.gui.PseudoGUI;

public abstract class GUI extends valoeghese.scalpel.gui.GUI implements PseudoGUI {
	protected GUI(int texture) {
		super(texture);
		this.shader = Shaders.gui;
	}

	private final Shader shader;

	public Shader getShader() {
		return this.shader;
	}
}
