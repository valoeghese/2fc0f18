package tk.valoeghese.fc0.client.render.system.gui;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.ArrayList;
import java.util.List;

public abstract class GUICollection<T extends PseudoGUI> implements PseudoGUI {
	protected List<T> guis = new ArrayList<>();
	private IntSet disabled = new IntArraySet();

	protected void disable(int component) {
		this.disabled.add(component);
	}

	protected boolean enable(int component) {
		return this.disabled.remove(component);
	}

	@Override
	public void render() {
		for (int i = 0; i < this.guis.size(); ++i) {
			if (!this.disabled.contains(i)) {
				this.guis.get(i).render();
			}
		}
	}

	@Override
	public void destroy() {
		for (PseudoGUI gui : this.guis) {
			gui.destroy();
		}

		this.guis.clear();
	}
}
