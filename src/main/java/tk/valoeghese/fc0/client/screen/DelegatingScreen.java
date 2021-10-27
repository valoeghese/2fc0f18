package tk.valoeghese.fc0.client.screen;

import tk.valoeghese.fc0.client.Client2fc;
import valoeghese.scalpel.Window;

import java.util.ArrayList;
import java.util.List;

public abstract class DelegatingScreen extends Screen {
	public DelegatingScreen(Client2fc game) {
		super(game);
	}

	protected List<Screen> delegates = new ArrayList<>();
	private Screen selected;
	private int selectedIndex;

	protected int getSelectedIndex() {
		return this.selectedIndex;
	}

	@Override
	public void renderGUI(float lighting) {
		this.selected.renderGUI(lighting);
	}

	@Override
	public void handleMouseInput(double dx, double dy) {
		this.selected.handleMouseInput(dx, dy);
	}

	@Override
	public void handleKeybinds() {
		this.selected.handleKeybinds();
	}

	@Override
	public void handleEscape(Window window) {
		this.selected.handleEscape(window);
	}

	@Override
	public void onFocus() {
		this.selected.onFocus();
	}

	public void switchScreen(int delegate) {
		if (delegate < this.delegates.size()) {
			this.selected = this.delegates.get(delegate);
			this.selectedIndex = delegate;
			this.selected.onFocus();
		}
	}

	@Override
	public String toString() {
		return "Hi Juuz. This is a really cursed piece of code that really defeats the point of why I made separate Screen classes which are switchable in the main class. Have fun mapping this.";
	}
}
