package tk.valoeghese.fc0.client.world;

import org.joml.Vector3f;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.maths.MathsUtils;
import valoeghese.scalpel.Camera;
import tk.valoeghese.fc0.util.Face;
import tk.valoeghese.fc0.util.RaycastResult;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.player.Player;
import tk.valoeghese.fc0.world.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class ClientPlayer extends Player  {
	public ClientPlayer(Camera camera, Client2fc game, boolean dev) {
		super(dev, size -> new ClientInventory(size, game));
		this.camera = camera;
		this.camera.translateScene(new Vector3f(0, -1.8f, 0)); // 2 blocks tall, camera at head
	}

	private final Camera camera;
	public EcoZone cachedZone;
	/**
	 * Cached tile pos for use for checking whether to update debug displays.
	 */
	public TilePos cachedPos;

	public Camera getCamera() {
		return this.camera;
	}

	public void updateCameraPos(float tickDelta) {
		Pos interpolated = new Pos(this.pos, this.nextPos, tickDelta);
		this.camera.setPos((float) -interpolated.getX(), (float) -interpolated.getY() - 1.8f, (float) -interpolated.getZ());
	}

	public RaycastResult rayCast(double maxDistance, boolean bridgeBlocks) {
		// By p0nki, hydos, valoeghese
		Pos toUse = this.pos.ofAdded(0, 1.8, 0);
		TilePos feetTilePos = this.getTilePos().down();
		TilePos bridgePos = null;
		Vector3f start = new Vector3f((float) toUse.getX(), (float) toUse.getY(), (float) toUse.getZ());
		Vector3f dir = this.camera.getDirection();
		Vector3f end = new Vector3f(start).add(new Vector3f(dir).mul((float) maxDistance));

		final float x1 = start.x;
		final float y1 = start.y;
		final float z1 = start.z;
		final float x2 = end.x;
		final float y2 = end.y;
		final float z2 = end.z;

		int floorX1 = MathsUtils.floor(x1);
		int floorY1 = MathsUtils.floor(y1);
		int floorZ1 = MathsUtils.floor(z1);

		final int di = Float.compare(x2, x1);
		final int dj = Float.compare(y2, y1);
		final int dk = Float.compare(z2, z1);
		final float dx = 1 / Math.abs(x2 - x1);
		final float dy = 1 / Math.abs(y2 - y1);
		final float dz = 1 / Math.abs(z2 - z1);

		final float minx = MathsUtils.floor(x1), maxx = minx + 1;
		final float miny = MathsUtils.floor(y1), maxy = miny + 1;
		final float minz = MathsUtils.floor(z1), maxz = minz + 1;

		float tx = (x1 > x2 ? x1 - minx : maxx - x1) * dx;
		float ty = (y1 > y2 ? y1 - miny : maxy - y1) * dy;
		float tz = (z1 > z2 ? z1 - minz : maxz - z1) * dz;

		List<TilePos> list = new ArrayList<>();

		for (int step = 0; step < maxDistance; ++step) {
			TilePos position = new TilePos(floorX1, floorY1, floorZ1);
			list.add(position);

			if (bridgeBlocks
					&& position.y == feetTilePos.y
					&& Math.abs(position.x - feetTilePos.x) + Math.abs(position.z - feetTilePos.z) == 1) {
				bridgePos = position;
			}

			if (tx <= ty && tx <= tz) {
				tx += dx;
				floorX1 += di;
			} else if (ty <= tz) {
				ty += dy;
				floorY1 += dj;
			} else {
				tz += dz;
				floorZ1 += dk;
			}
		}

		for (int ind = 1; ind < maxDistance; ind++) {
			TilePos pos = list.get(ind);

			if (this.world.isInWorld(pos.x, pos.y, pos.z)) {
				byte tile = this.world.readTile(pos.x, pos.y, pos.z);

				if (Tile.BY_ID[tile].shouldRender()) {
					int nx = list.get(ind - 1).x - pos.x;
					int ny = list.get(ind - 1).y - pos.y;
					int nz = list.get(ind - 1).z - pos.z;

					Face face = null;

					// yandere dev level if statement. I approve.
					if (nx == -1) {
						face = Face.SOUTH;
					} else if (nx == 1) {
						face = Face.NORTH;
					} else if (ny == -1) {
						face = Face.DOWN;
					} else if (ny == 1) {
						face = Face.UP;
					} else if (nz == -1) {
						face = Face.WEST;
					} else if (nz == 1) {
						face = Face.EAST;
					}

					if (face == null) {
						throw new RuntimeException("tf\n//I blame p0nki.");
					}

					if (nx != face.getX()) {
						throw new RuntimeException("Unmatching faceX on raycast " + nx + ":" + face.getX());
					}

					if (ny != face.getY()) {
						throw new RuntimeException("Unmatching faceY on raycast " + ny + ":" + face.getY());
					}

					if (nz != face.getZ()) {
						throw new RuntimeException("Unmatching faceZ on raycast " + nz + ":" + face.getZ());
					}

					return new RaycastResult(new TilePos(pos.x, pos.y, pos.z), face);
				}
			}
		}

		// use .down() and Face.UP as a hack to make placing blocks treat it as a position
		return bridgePos != null ? new RaycastResult(bridgePos.down(), Face.UP) : new RaycastResult(new TilePos(MathsUtils.floor(end.x), MathsUtils.floor(end.y), MathsUtils.floor(end.z)), null);
	}

	@Override
	public void tick() {
		super.tick();
		this.camera.wrapYaw();
	}
}
