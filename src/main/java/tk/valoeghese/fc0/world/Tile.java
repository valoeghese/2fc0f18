package tk.valoeghese.fc0.world;

public class Tile {
	public Tile(int id, int u, int v) {
		BY_ID[id] = this;
		this.id = id;
		this.u = u;
		this.v = v;
	}

	public final int id;
	public final int u;
	public final int v;

	public static final Tile[] BY_ID = new Tile[256];
	public static final Tile STONE = new Tile(1, 0, 1);
}
