package tk.valoeghese.fc0.client.render.gui;

import tk.valoeghese.fc0.client.Client2fc;

public class MoveableRect extends GUI {
	public MoveableRect(int texture, float size) {
		this(texture, size, size);
	}

	public MoveableRect(int texture, float sizeX, float sizeY) {
		super(texture);

		this.protoVertices = new float[][]{
				{-sizeX, sizeY},
				{-sizeX, -sizeY},
				{sizeX, sizeY},
				{sizeX, -sizeY}
		};

		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	protected final float sizeX;
	protected final float sizeY;
	protected final float[][] protoVertices;

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
}
