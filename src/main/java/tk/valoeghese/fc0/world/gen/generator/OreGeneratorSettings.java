package tk.valoeghese.fc0.world.gen.generator;

public class OreGeneratorSettings extends HeightCountGeneratorSettings {
	public OreGeneratorSettings(int count, int minY, int maxY, byte ore) {
		super(count, minY, maxY);
		this.ore = ore;
	}

	public final byte ore;
}
