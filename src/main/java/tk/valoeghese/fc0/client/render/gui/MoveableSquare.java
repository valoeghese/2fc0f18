package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.Client2fc;

public class MoveableSquare extends GUI {
	public MoveableSquare(int texture, float size) {
		super(texture);

		this.protoVertices = new float[][]{
				{-size, size},
				{-size, -size},
				{size, size},
				{size, -size}
		};

		this.size = size;
	}

	protected final float size;

	public void setPosition(float xOffset, float yOffset) {
		this.destroy();

		float aspect = 1f / Client2fc.getInstance().getWindowAspect();

		int tl = this.vertex(xOffset + aspect * this.protoVertices[0][0], yOffset + this.protoVertices[0][1], 0, 1);
		int bl = this.vertex(xOffset + aspect * this.protoVertices[1][0], yOffset + this.protoVertices[1][1], 0, 0);
		int tr = this.vertex(xOffset + aspect * this.protoVertices[2][0], yOffset + this.protoVertices[2][1], 1, 1);
		int br = this.vertex(xOffset + aspect * this.protoVertices[3][0], yOffset + this.protoVertices[3][1], 1, 0);

		this.tri(tl, bl, br);
		this.tri(tl, tr, br);

		this.generateBuffers();
	}

	private final float[][] protoVertices;
}
