package net.enderturret.umldiagram.util;

/**
 * Your standard Pair class. It holds two values. I wonder how many pair implementations exist these days?
 * @author EnderTurret
 *
 * @param <L> The type of the left value.
 * @param <R> The type of the right value.
 */
public final class Pair<L, R> {

	private final L left;
	private final R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * @return The left value.
	 */
	public L left() {
		return left;
	}

	/**
	 * @return The right value.
	 */
	public R right() {
		return right;
	}

	@Override
	public String toString() {
		return String.valueOf(left) + ", " + right;
	}
}