package tk.valoeghese.fc0.client.render.model;

import valoeghese.scalpel.Model;
import valoeghese.scalpel.Shader;

import javax.annotation.Nullable;

public class SquareModel extends Model {
	public SquareModel(int mode, @Nullable Shader shader) {
		this(mode, shader, 0.0f, 0.0f, 1.0f, 1.0f);
	}

	protected SquareModel(int mode, @Nullable Shader shader, float startU, float startV, float endU, float endV) {
		super(mode, shader);

		int tl = this.vertex(-1.0f, 1.0f, 0.0f, startU, endV);
		int bl = this.vertex(-1.0f, -1.0f, 0.0f, startU, startV);
		int tr = this.vertex(1.0f, 1.0f, 0.0f, endU, endV);
		int br = this.vertex(1.0f, -1.0f, 0.0f, endU, startV);
		this.tri(tl, bl, br);
		this.tri(tl, tr, br);
		this.generateBuffers();
	}
}
