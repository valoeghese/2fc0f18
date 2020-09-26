package tk.valoeghese.fc0.client.world;

import org.joml.Vector3d;
import org.joml.Vector3f;
import tk.valoeghese.fc0.client.Client2fc;
import tk.valoeghese.fc0.util.Face;
import tk.valoeghese.fc0.util.Pair;
import tk.valoeghese.fc0.util.RaycastResult;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.util.raycasting.RayCasting;
import tk.valoeghese.fc0.util.raycasting.Pos;
import tk.valoeghese.fc0.util.raycasting.WorldSolidBlockDistanceFunction;
import tk.valoeghese.fc0.world.gen.ecozone.EcoZone;
import tk.valoeghese.fc0.world.player.Player;
import valoeghese.scalpel.Camera;
import static org.joml.Math.cos;
import static org.joml.Math.sin;

public class ClientPlayer extends Player {
	public ClientPlayer(Camera camera, Client2fc game, boolean dev) {
		super(dev, size -> new ClientInventory(size, game));
		this.camera = camera;
		this.camera.translateScene(new Vector3f(0, -1.8f, 0)); // 2 blocks tall, camera at head
	}

	private final Camera camera;
	public EcoZone cachedZone;
	public TilePos cachedPos;

	public Camera getCamera() {
		return this.camera;
	}

	@Override
	public void setPos(tk.valoeghese.fc0.util.maths.Pos pos) {
		super.setPos(pos);
		this.camera.setPos((float) -pos.getX(), (float) -pos.getY() - 1.8f, (float) -pos.getZ());
	}

	@Override
	public boolean move(double x, double y, double z) {
		boolean result = super.move(x, y, z);

		if (result) {
			this.camera.translateScene(new Vector3f((float) -x, (float) -y, (float) -z));
		}

		return result;
	}

	public RaycastResult rayCast(double maxDistance) {
		Pos direction;
		Pair<Pos, Boolean> pair;

		{
			tk.valoeghese.fc0.util.maths.Pos toUse = this.pos.ofAdded(0, 1.8, 0);
			Vector3d start = new Vector3d(toUse.getX(), toUse.getY(), toUse.getZ());
			Vector3d dir = getDirectionVector(camera.getYaw(), camera.getPitch());
			Vector3d end = new Vector3d(start).add(new Vector3d(dir).mul(maxDistance));

			Pos from = new Pos(start);
			Pos to = new Pos(end);
			Pos v = to.subtract(from);
			pair = RayCasting.rayCast(from, direction = v.normalize(), v.length(), new WorldSolidBlockDistanceFunction(world));
		}

		Pos hit = pair.getLeft();
		return new RaycastResult(new TilePos((int) hit.x, (int) hit.y, (int) hit.z), pair.getRight() ? Face.findFace(direction) : null);
	}

	private static Vector3d getDirectionVector(float yaw, float pitch) {
		double x = sin(yaw) * cos(pitch);
		double y = -sin(pitch);
		double z = -cos(yaw) * cos(pitch);

		return new Vector3d(x, y, z);
	}

	@Override
	public void tick() {
		super.tick();
		this.camera.wrapYaw();
	}
}
