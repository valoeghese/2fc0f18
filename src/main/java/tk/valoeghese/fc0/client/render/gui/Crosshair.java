package tk.valoeghese.fc0.client.render.gui;

public class Crosshair extends GUI {
	public Crosshair() {
		super(0);

		this.vertex(0.01f, 0.01f, 1.0f, 1.0f);
		this.vertex(0.01f, -0.01f, 1.0f, -1.0f);
		this.vertex(-0.01f, 0.01f, -1.0f, 1.0f);
		this.vertex(-0.01f, -0.01f, -1.0f, -1.0f);
		this.tri(3, 2, 1);
		this.tri(3, 0, 1);
		this.generateBuffers();
	}
}
