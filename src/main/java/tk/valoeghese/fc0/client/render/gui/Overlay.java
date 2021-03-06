package tk.valoeghese.fc0.client.render.gui;

public class Overlay extends GUI {
	public Overlay(int texture) {
		super(texture);

		int tl = this.vertex(-1.0f, 1.0f, 0.9988f, 0, 1);
		int bl = this.vertex(-1.0f, -1.0f, 0.9988f, 0, 0);
		int tr = this.vertex(1.0f, 1.0f, 0.9988f, 1, 1);
		int br = this.vertex(1.0f, -1.0f, 0.9988f, 1, 0);

		this.tri(tl, bl, br);
		this.tri(tl, tr, br);

		this.generateBuffers();
	}
}
