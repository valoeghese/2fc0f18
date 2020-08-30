package tk.valoeghese.fc0.world.tile;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import tk.valoeghese.fc0.util.maths.Vec2i;

import java.util.List;
import java.util.function.Function;

public class MetaPlacementTile extends Tile {
	public MetaPlacementTile(List<String> textures, int id, float iota) {
		super(textures.get(0), id, iota);
		this.textures = textures;
	}

	private final List<String> textures;
	private final Byte2ObjectArrayMap<Vec2i> metaTextures = new Byte2ObjectArrayMap<>();

	@Override
	public void requestUV(Function<String, Vec2i> uvs) {
		super.requestUV(uvs);
	}
}
