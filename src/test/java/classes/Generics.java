package classes;

import java.util.List;
import java.util.Map;
import java.util.Random;

public interface Generics<T, Z> {

	public static List<Integer> INTS = null;

	public Map<Random, Random> get(List<T> list);

	public <A, B> int compare(A a, B b);

	public void apply(List<T> list, List<Z> other);

	public void apply(Map<T, T> list, Map<Z, Z> other);
}