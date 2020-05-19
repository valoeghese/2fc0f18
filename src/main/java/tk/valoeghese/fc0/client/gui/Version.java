package tk.valoeghese.fc0.client.gui;

import tk.valoeghese.fc0.client.model.Textures;

public class Version extends GUI {
	public Version() {
		super(Textures.VERSION);

		int tl = this.vertex(-1.0f, 1.0f, 0, 1);
		int bl = this.vertex(-1.0f, -1.0f, 0, 0);
		int tr = this.vertex(1.0f, 1.0f, 1, 1);
		int br = this.vertex(1.0f, -1.0f, 1, 0);

		this.tri(tl, bl, br);
		this.tri(tl, tr, br);

		this.generateBuffers();
	}
}
