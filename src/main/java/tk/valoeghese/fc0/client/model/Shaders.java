package tk.valoeghese.fc0.client.model;

import tk.valoeghese.fc0.client.system.Shader;

public final class Shaders {
	public static Shader terrain;
	public static Shader gui;

	public static void loadShaders() {
		terrain = new Shader("assets/shader/terrain_v.glsl", "assets/shader/terrain_f.glsl");
		gui = new Shader("assets/shader/gui_v.glsl", "assets/shader/gui_f.glsl");
	}
}
