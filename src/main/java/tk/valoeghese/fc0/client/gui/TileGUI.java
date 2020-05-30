package tk.valoeghese.fc0.client.gui;

import tk.valoeghese.fc0.client.model.Shaders;
import tk.valoeghese.fc0.client.model.Textures;
import tk.valoeghese.fc0.world.tile.Tile;

public class TileGUI extends GUI {
	public TileGUI(float xOffset, float yOffset, float size) {
		super(Textures.TILE_ATLAS);
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.size = size;
	}

	private final float xOffset;
	private final float yOffset;
	private final float size;

	public void setTile(Tile tile, byte meta, float aspect) {
		this.destroy();

		float horizontalSize = aspect * this.size;

		if (tile.isCross()) {
			// render west face
			float startU = (tile.getU(3, meta) / 16.0f);
			float startV = (tile.getV(3, meta) / 16.0f);
			float endU = startU + 0.0625f;
			float endV = startV + 0.0625f;

			int tl = this.vertex(this.xOffset - horizontalSize, this.yOffset + this.size, startU, endV);
			int bl = this.vertex(this.xOffset - horizontalSize, this.yOffset - this.size, startU, startV);
			int tr = this.vertex(this.xOffset + horizontalSize, this.yOffset + this.size, endU, endV);
			int br = this.vertex(this.xOffset + horizontalSize, this.yOffset - this.size, endU, startV);

			this.tri(tl, bl, br);
			this.tri(tl, tr, br);
		} else {
			// up face
			Shaders.gui.uniformFloat("lighting", 0.9f);
			float startU = (tile.getU(1, meta) / 16.0f);
			float startV = (tile.getV(1, meta) / 16.0f);
			float endU = startU + 0.0625f;
			float endV = startV + 0.0625f;

			int tl = this.vertex(this.xOffset, this.yOffset + this.size, startU, endV);
			int bl = this.vertex(this.xOffset - horizontalSize, this.yOffset + 0.5f * this.size, startU, startV);
			int tr = this.vertex(this.xOffset + horizontalSize, this.yOffset + 0.5f * this.size, endU, endV);
			int br = this.vertex(this.xOffset, this.yOffset, endU, startV);

			this.tri(tl, bl, br);
			this.tri(tl, tr, br);

			// left, north face
			Shaders.gui.uniformFloat("lighting", 1.0f);
			startU = (tile.getU(2, meta) / 16.0f);
			startV = (tile.getV(2, meta) / 16.0f);
			endU = startU + 0.0625f;
			endV = startV + 0.0625f;

			tl = this.vertex(this.xOffset - horizontalSize, this.yOffset + 0.5f * this.size, startU, endV);
			bl = this.vertex(this.xOffset - horizontalSize, this.yOffset - 0.5f * this.size, startU, startV);
			tr = this.vertex(this.xOffset, this.yOffset, endU, endV);
			br = this.vertex(this.xOffset, this.yOffset - this.size, endU, startV);

			this.tri(tl, bl, br);
			this.tri(tl, tr, br);

			// right, west face
			Shaders.gui.uniformFloat("lighting", 0.8f);
			startU = (tile.getU(3, meta) / 16.0f);
			startV = (tile.getV(3, meta) / 16.0f);
			endU = startU + 0.0625f;
			endV = startV + 0.0625f;

			tl = this.vertex(this.xOffset, this.yOffset, startU, endV);
			bl = this.vertex(this.xOffset, this.yOffset - this.size, startU, startV);
			tr = this.vertex(this.xOffset + horizontalSize, this.yOffset + 0.5f * this.size, endU, endV);
			br = this.vertex(this.xOffset + horizontalSize, this.yOffset - 0.5f * this.size, endU, startV);

			this.tri(tl, bl, br);
			this.tri(tl, tr, br);
		}

		this.generateBuffers();
		Shaders.gui.uniformFloat("lighting", 1.0f);
	}
}
