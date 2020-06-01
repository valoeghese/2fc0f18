package tk.valoeghese.fc0.client.render.screen;

import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.client.Keybinds;
import tk.valoeghese.fc0.client.render.gui.Text;
import tk.valoeghese.fc0.client.render.system.Window;
import tk.valoeghese.fc0.client.render.system.gui.GUI;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class TitleScreen extends Screen {
	public TitleScreen(Client2fc game) {
		super(game);
		this.titleText = new Text("Click to start.", -0.85f, 0.5f, 2.2f);
	}

	private final GUI titleText;

	@Override
	public void renderGUI(float lighting) {
		this.titleText.render();
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
	}

	@Override
	public void handleKeybinds() {
		if (Keybinds.DESTROY.hasBeenPressed()) {
			this.game.switchScreen(this.game.gameScreen);
			this.game.createWorld();
			this.game.generateSpawnChunks();
		}
	}

	@Override
	public void handleEscape(Window window) {
		glfwSetWindowShouldClose(window.glWindow, true);
	}
}
