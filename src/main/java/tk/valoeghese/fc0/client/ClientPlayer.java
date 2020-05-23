package tk.valoeghese.fc0.client;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import tk.valoeghese.fc0.client.system.Camera;
import tk.valoeghese.fc0.util.*;
import tk.valoeghese.fc0.util.maths.MathsUtils;
import tk.valoeghese.fc0.util.maths.MutablePos;
import tk.valoeghese.fc0.util.maths.Pos;
import tk.valoeghese.fc0.util.maths.TilePos;
import tk.valoeghese.fc0.world.Chunk;
import tk.valoeghese.fc0.world.World;
import tk.valoeghese.fc0.world.tile.Tile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

public class ClientPlayer {
	public ClientPlayer(Camera camera, World world) {
		this.pos = new MutablePos(0, 0, 0);
		this.velocity = new MutablePos(0, 0, 0);
		this.camera = camera;
		this.camera.translateScene(new Vector3f(0, -1.8f, 0)); // 2 blocks tall, camera at head
		this.world = world;
		this.move(0, world.getHeight(0, 0) + 1, 0);
	}

	private final MutablePos pos;
	private final Camera camera;
	private final World world;
	private final MutablePos velocity;
	private boolean falling = false;
	@Nullable
	public Chunk chunk = null;
	public long lockSwim = 0;

	public Camera getCamera() {
		return this.camera;
	}

	public int getX() {
		return new TilePos(this.pos).x;
	}

	public int getZ() {
		return new TilePos(this.pos).z;
	}

	public void addVelocity(double x, double y, double z) {
		this.velocity.offset(x, y, z);
	}

	public void move(Pos pos) {
		this.move(pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean isUnderwater() {
		TilePos pos = new TilePos(this.pos.ofAdded(0, 1.8, 0));

		if (this.world.isInWorld(pos)) {
			return this.world.readTile(pos) == Tile.WATER.id;
		} else {
			return false;
		}
	}

	public boolean isSwimming() {
		TilePos pos = this.getTilePos();

		if (this.world.isInWorld(pos)) {
			return this.world.readTile(pos) == Tile.WATER.id;
		} else {
			return false;
		}
	}

	public void setPos(Pos pos) {
		this.pos.set(pos);
		this.camera.setPos((float) pos.getX(), (float) -pos.getY() - 1.8f, (float) pos.getZ());
	}

	public boolean move(double x, double y, double z) {
		Pos next = this.pos.ofAdded(x, y, z);
		TilePos tilePos = new TilePos(next);

		if (this.world.isInWorld(tilePos)) {
			if (Tile.BY_ID[this.world.readTile(tilePos)].isOpaque()) {
				return false;
			}
		}

		tilePos = tilePos.up();

		if (this.world.isInWorld(tilePos)) {
			if (Tile.BY_ID[this.world.readTile(tilePos)].isOpaque()) {
				return false;
			}
		}

		this.pos.set(next);
		this.world.updateChunkOf(this);
		this.camera.translateScene(new Vector3f((float) -x, (float) -y, (float) -z));
		return true;
	}

	public boolean isOnGround() {
		if (this.falling) {
			return false;
		}

		TilePos check = new TilePos(this.pos).down();

		if (this.world.isInWorld(check)) {
			if (Tile.BY_ID[this.world.readTile(check)].isOpaque()) {
				return true;
			}
		}

		return false;
	}

	public TilePos getTilePos() {
		return new TilePos(this.pos);
	}

	public RaycastResult rayCast(double maxDistance) {
		//Uncomment the line below if you want but its kind of useless
//		maxDistance *= maxDistance;
		double yaw = this.getCamera().getYaw() + Math.PI;
		double pitch = this.getCamera().getPitch();
		Pos toUse = this.pos.ofAdded(0, 1.8, 0);

		//https://github.com/jearmstrong21/GLMC3_COMMON/blob/master/common/entity/entity_player.cpp#L54
		//This code written by p0nki / jearmstrong21
		//Don't touch it unless you know what you're doing
		//I don't even know what I'm doing as I write this
		//
		Vector3f start=new Vector3f((float)toUse.getX(),(float)toUse.getY(),(float)toUse.getZ());
		start.add(0.5F,0.5F,0.5F);
		Vector3f dir=camera.getNormalisedDirection();
		Vector3f end=new Vector3f(start).add(new Vector3f(dir).mul((float)maxDistance));
		final float x1=start.x;
		final float y1=start.y;
		final float z1=start.z;
		final float x2=end.x;
		final float y2=end.y;
		final float z2=end.z;
		int i=(int)x1;
		int j=(int)y1;
		int k=(int)z1;
		final int di= Float.compare(x2, x1);
		final int dj=Float.compare(y2,y1);
		final int dk=Float.compare(z2,z1);
		final float dx=1/Math.abs(x2-x1);
		final float dy=1/Math.abs(y2-y1);
		final float dz=1/Math.abs(z2-z1);
		final float minx=(int)x1, maxx=minx+1;
		final float miny=(int)y1, maxy=miny+1;
		final float minz=(int)z1, maxz=minz+1;
		float tx=((x1>x2)?(x1-minx):(maxx-x1))*dx;
		float ty=((y1>y2)?(y1-miny):(maxy-y1))*dy;
		float tz=((z1>z2)?(z1-minz):(maxz-z1))*dz;
		List<Vector3i>list=new ArrayList<>();
		for(int __=0;__<maxDistance;__++){
			list.add(new Vector3i(i,j,k));
			if(tx<=ty&&tx<=tz){
				tx+=dx;
				i+=di;
			}else if(ty<=tz){
				ty+=dy;
				j+=dj;
			}else{
				tz+=dz;
				k+=dk;
			}
		}
		for(int ind=1;ind<maxDistance;ind++){
			Vector3i pos=list.get(ind);
			byte tile=world.readTile(pos.x,pos.y,pos.z);
			if(tile!=0){
				int nx=list.get(ind-1).x-pos.x;
				int ny=list.get(ind-1).y-pos.y;
				int nz=list.get(ind-1).z-pos.z;
				Face face=null;
				if(nx==-1)face=Face.WEST;
				else if(nx==1)face=Face.EAST;
				else if(ny==-1)face=Face.DOWN;
				else if(ny==1)face=Face.UP;
				else if(nz==-1)face=Face.SOUTH;
				else if(nz==1)face=Face.NORTH;
				if(face==null)throw new RuntimeException("tf");
				if(nx!=face.getX())throw new RuntimeException(nx+":"+face.getX());
				if(ny!=face.getY())throw new RuntimeException(ny+":"+face.getY());
				if(nz!=face.getZ())throw new RuntimeException(nz+":"+face.getZ());
				return new RaycastResult(new TilePos(pos.x,pos.y,pos.z),face);
			}
		}
		return new RaycastResult(new TilePos((int)end.x,(int)end.y,(int)end.z),null);
	}

	public void tick() {
		this.velocity.offsetY(this.isSwimming() ? -0.01f : -0.03f);
		this.velocity.mul(0.85, 0.9, 0.85);
		this.move(this.velocity.getX(), 0.0, 0.0);
		this.move(0.0, 0.0, this.velocity.getZ());

		if (Math.abs(this.velocity.getY()) > 0.03) {
			this.falling = true;
		}

		if (!this.move(0.0, this.velocity.getY(), 0.0)) {
			this.falling = false;
			this.velocity.setY(0.0);
		}

		this.camera.wrapYaw();
	}

	public float getHorizontalSlowness() {
		return 40.0f;
	}

	public double getJumpStrength() {
		return 12.0 / 30.0;
	}

	private static double HALF_PI = Math.PI / 2;
	private static double THREE_HALF_PI = 3 * Math.PI / 2;
}
