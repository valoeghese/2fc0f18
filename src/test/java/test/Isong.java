package test;

import java.util.Arrays;
import java.util.List;

// you have no context
public class Isong {
	public static void main(String[] args) {
		List<List<String>> thebestlist = Arrays.asList(Arrays.asList("ur mom", "is"), Arrays.asList("gae", "lmao", "get rekt"));
		thebestlist.stream()
				.flatMap(List::stream)
				.filter(s -> s.matches(".* .*"))
				.map(s -> "not " + s)
				.forEach(System.out::println);
	}
}
