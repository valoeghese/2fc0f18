package tk.valoeghese.fc0.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.function.Supplier;

public class SingleTimeLog {
	private SingleTimeLog(int target) {
		this.target = target;
	}

	private int target;

	public void log(Supplier<String> supplier) {
		if (target-- == 0) {
			System.out.println(supplier.get());
		}
	}

	public static SingleTimeLog get(int id, int target) {
		return objects.computeIfAbsent(id, i -> new SingleTimeLog(target));
	}

	private static Int2ObjectMap<SingleTimeLog> objects = new Int2ObjectArrayMap<>();
}
