package tk.valoeghese.fc0.client.gui;

import tk.valoeghese.fc0.client.system.gui.GUI;

public class MoveableSquare extends GUI {
	public MoveableSquare(int texture, float windowAspect, float size) {
		super(texture);

		this.protoVertices = new float[][]{
				{-size * 1f / windowAspect, size},
				{-size * 1f / windowAspect, -size},
				{size * 1f / windowAspect, size},
				{size * 1f / windowAspect, -size}
		};
	}

	public void setPosition(float xOffset, float yOffset) {
		this.destroy();

		int tl = this.vertex(xOffset + this.protoVertices[0][0], yOffset + this.protoVertices[0][1], 0, 1);
		int bl = this.vertex(xOffset + this.protoVertices[1][0], yOffset + this.protoVertices[1][1], 0, 0);
		int tr = this.vertex(xOffset + this.protoVertices[2][0], yOffset + this.protoVertices[2][1], 1, 1);
		int br = this.vertex(xOffset + this.protoVertices[3][0], yOffset + this.protoVertices[3][1], 1, 0);

		this.tri(tl, bl, br);
		this.tri(tl, tr, br);

		this.generateBuffers();
	}

	private final float[][] protoVertices;
}
