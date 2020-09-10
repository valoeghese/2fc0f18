package tk.valoeghese.fc0.client.render.gui.collection;

import tk.valoeghese.fc0.client.render.Textures;
import tk.valoeghese.fc0.client.render.gui.ButtonSquare;
import tk.valoeghese.fc0.client.render.gui.Text;
import valoeghese.scalpel.gui.GUICollection;
import valoeghese.scalpel.gui.PseudoGUI;

public class WorldSave extends GUICollection<WorldSave.WorldSaveEntry> {
	public WorldSave(String... saves) {
		float yOffset = 0.65f;

		for (String saveName : saves) {
			this.guis.add(new WorldSaveEntry(saveName, yOffset));
			yOffset -= 0.23f;
		}
	}

	public int getSelected(float x, float y) {
		// TODO optimise by checking x first, since the x of the button is same for each button?
		for (int i = 0; i < this.guis.size(); ++i) {
			WorldSaveEntry entry = this.guis.get(i);

			if (entry.isCursorSelecting(x, y)) {
				return i;
			}
		}

		return -1;
	}

	static class WorldSaveEntry implements PseudoGUI {
		public WorldSaveEntry(String text, float yOffset) {
			this.text = new Text(text, -0.5f, yOffset, 1.2f);
			this.entryButton = new ButtonSquare(Textures.ENTER, 0.1f);
			this.entryButton.setPosition(0.5f, yOffset);
		}

		private final ButtonSquare entryButton;
		private final Text text;

		private boolean isCursorSelecting(float x, float y) {
			return this.entryButton.isCursorSelecting(x, y);
		}

		@Override
		public void render() {
			this.text.render();
			this.entryButton.render();
		}

		@Override
		public void destroy() {
			this.text.destroy();
			this.entryButton.destroy();
		}
	}
}
