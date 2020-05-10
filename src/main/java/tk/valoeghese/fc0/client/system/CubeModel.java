package tk.valoeghese.fc0.client.system;

public class CubeModel extends Model {
	public CubeModel(int mode, Shader shader) {
		super(mode, shader);
		this.vertex(-0.5f, -0.5f, -0.5f);
		this.vertex(0.5f, -0.5f, -0.5f);
		this.vertex(0.5f, -0.5f, 0.5f);
		this.vertex(-0.5f, -0.5f, 0.5f);
		this.vertex(-0.5f, 0.5f, -0.5f);
		this.vertex(0.5f, 0.5f, -0.5f);
		this.vertex(0.5f, 0.5f, 0.5f);
		this.vertex(-0.5f, 0.5f, 0.5f);
		// bottom
		this.tri(0, 3, 2);
		this.tri(0, 2, 1);
		// top
		this.tri(4, 5, 6);
		this.tri(4, 6, 7);
		// left
		this.tri(7, 3, 0);
		this.tri(7, 0, 4);
		// right
		this.tri(5, 1, 2);
		this.tri(5, 2, 6);
		// front
		this.tri(4, 0, 1);
		this.tri(4, 1, 5);
		// back
		this.tri(6, 2, 3);
		this.tri(6, 2, 7);

		this.generateBuffers();
	}
}
