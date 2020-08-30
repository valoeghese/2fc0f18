package tk.valoeghese.fc0.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Pair<A, B> {
	public Pair(A left, B right) {
		this.left = left;
		this.right = right;
	}

	private final A left;
	private final B right;

	public A getLeft() {
		return this.left;
	}

	public B getRight() {
		return this.right;
	}

	public Pair<B, A> swap() {
		return new Pair<>(this.right, this.left);
	}

	public <C> Pair<A, C> mapRight(Function<B, C> mappingFunction) {
		return new Pair<>(this.left, mappingFunction.apply(this.right));
	}

	public <C> Pair<C, B> mapLeft(Function<A, C> mappingFunction) {
		return new Pair<>(mappingFunction.apply(this.left), this.right);
	}

	public <C, D> Pair<C, D> map(Function<A, C> mappingFunctionLeft, Function<B, D> mappingFunctionRight) {
		return new Pair<>(mappingFunctionLeft.apply(this.left), mappingFunctionRight.apply(this.right));
	}

	public <C> C flatMap(BiFunction<A, B, C> mappingFunction) {
		return mappingFunction.apply(this.left, this.right);
	}
}
