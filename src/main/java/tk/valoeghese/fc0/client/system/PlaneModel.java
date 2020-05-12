package tk.valoeghese.fc0.client.system;

public class PlaneModel extends Model {
	public PlaneModel(int mode, Shader shader) {
		super(mode, shader);
		this.vertex(-0.5f, 0.5f, -0.5f, 0.0f, 1.0f); // tl
		this.vertex(-0.5f, -0.5f, -0.5f, 0.0f, 0.0f); // bl
		this.vertex(0.5f, 0.5f, -0.5f, 1.0f, 1.0f); // tr
		this.vertex(0.5f, -0.5f, -0.5f, 1.0f, 0.0f); // br
		this.tri(0, 1, 3);
		this.tri(0, 2, 3);
		this.generateBuffers();
	}
}
