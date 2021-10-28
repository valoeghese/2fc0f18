package tk.valoeghese.fc0.client.render.gui;

public class ResizableRect extends MoveableRect {
	public ResizableRect(int texture) {
		super(texture, 0.0f);
	}

	public void setSize(float xSize, float ySize) {
		System.arraycopy(new float[][]{
						{-xSize, ySize},
						{-xSize, -ySize},
						{xSize, ySize},
						{xSize, -ySize}},
				0, this.protoVertices, 0, this.protoVertices.length);
	}

	public void setProportions(float xSizeL, float ySizeL, float xSizeR, float ySizeR) {
		System.arraycopy(new float[][]{
						{-xSizeL, ySizeR},
						{-xSizeL, -ySizeL},
						{xSizeR, ySizeR},
						{xSizeR, -ySizeL}},
				0, this.protoVertices, 0, this.protoVertices.length);
	}

	@Override
	public void setPosition(float xOffset, float yOffset) {
		super.setPosition(xOffset, yOffset);
	}
}
