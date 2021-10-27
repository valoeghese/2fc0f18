package tk.valoeghese.fc0.world.tile;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import tk.valoeghese.fc0.Game2fc;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.util.maths.Vec2i;
import tk.valoeghese.fc0.world.TileAccess;

import java.util.List;
import java.util.function.Function;

public class MetaPlacementTile extends Tile {
	public MetaPlacementTile(List<String> textures, int id, float iota) {
		super(textures.get(0), id, iota);
		this.textures = textures;
		this.metaBound = this.textures.size();
	}

	private final List<String> textures;
	private final int metaBound;
	private final Byte2ObjectArrayMap<Vec2i> metaTextures = new Byte2ObjectArrayMap<>();

	@Override
	public void requestUV(Function<String, Vec2i> uvs) {
		byte i = 0;

		for (String tex : this.textures) {
			this.metaTextures.put(i++, uvs.apply(tex));
		}
	}

	@Override
	public int getU(int faceAxis, byte meta) {
		return this.metaTextures.get(meta).getX();
	}

	@Override
	public int getV(int faceAxis, byte meta) {
		return this.metaTextures.get(meta).getY();
	}

	@Override
	public void onPlace(TileAccess world, TilePos pos) {
		world.writeMeta(pos.x, pos.y, pos.z, (byte) Game2fc.RANDOM.nextInt(this.metaBound));
	}
}
