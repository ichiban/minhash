package com.y1ban.recommender.minhash;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import sparsebitmap.SparseBitmap;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.sleepycat.persist.model.Persistent;

@Persistent
public class SparseBitVector implements Serializable, Iterable<Integer> {
	private static final long serialVersionUID = 6161396584019533773L;

	// these fields reflect SparseBitmap's ones in order to serialize bit set.
	private int[] buffer;
	private int wordusage;
	private int sizeinword;
	private int cardinality;

	public SparseBitVector() {
		reflect(new SparseBitmap());
	}

	public static SparseBitVector of(final int... features) {
		final SparseBitVector vector = new SparseBitVector();
		vector.add(features);
		return vector;
	}

	public boolean add(final int n) {
		return add(Lists.newArrayList(n));
	}

	public boolean add(final int... ns) {
		final List<Integer> elems = Lists.newArrayList();
		for (int n : ns) {
			elems.add(n);
		}
		return add(elems);
	}

	public boolean add(final Iterable<Integer> ns) {
		final List<Integer> elems = Lists.newArrayList(ns);
		final SparseBitmap oldMap = sparseBitmap();
		final SparseBitmap newMap = new SparseBitmap(oldMap.buffer.length);
		boolean added = false;
		Collections.sort(elems);
		if (0 == oldMap.cardinality) {
			for (int n : elems) {
				newMap.set(n);
			}
		} else {
			for (int n : elems) {
				for (int i : oldMap) {
					if (i == n) {
						return true;
					} else if (!added && i > n) {
						newMap.set(n);
						added = true;
						newMap.set(i);
					} else {
						newMap.set(i);
					}
				}
			}
		}
		reflect(newMap);
		return false;
	}

	public boolean remove(final int n) {
		final SparseBitmap oldMap = sparseBitmap();
		final SparseBitmap newMap = new SparseBitmap(oldMap.buffer.length);
		boolean removed = false;
		for (int i : oldMap) {
			if (i == n) {
				removed = true;
			} else {
				newMap.set(i);
			}
		}
		reflect(newMap);
		return removed;
	}

	public boolean contains(final int n) {
		for (int i : sparseBitmap()) {
			if (i == n) {
				return true;
			}
		}
		return false;
	}

	protected SparseBitmap sparseBitmap() {
		final SparseBitmap sparseBitmap = new SparseBitmap();
		sparseBitmap.buffer = buffer;
		sparseBitmap.wordusage = wordusage;
		sparseBitmap.sizeinwords = sizeinword;
		sparseBitmap.cardinality = cardinality;
		return sparseBitmap;
	}

	protected void reflect(final SparseBitmap sparseBitmap) {
		this.buffer = sparseBitmap.buffer;
		this.wordusage = sparseBitmap.wordusage;
		this.sizeinword = sparseBitmap.sizeinwords;
		this.cardinality = sparseBitmap.cardinality;
	}

	public Iterator<Integer> iterator() {
		return sparseBitmap().iterator();
	}

	public Iterator<Boolean> bitIterator() {
		final Iterator<Integer> iter = iterator();
		return new Iterator<Boolean>() {
			private Optional<Integer> next = Optional.absent();
			private int count = 0;

			public boolean hasNext() {
				return iter.hasNext();
			}

			public Boolean next() {
				boolean result = false;

				if (!next.isPresent()) {
					next = Optional.of(iter.next());
				} else if (next.get().equals(count)) {
					result = true;
					next = Optional.of(iter.next());
				}

				count++;
				return result;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	public double dotProduct(Iterable<Double> vector) {
		final Iterator<Boolean> lIter = bitIterator();
		final Iterator<Double> rIter = vector.iterator();
		double result = 0;

		while (rIter.hasNext()) {
			result += (lIter.hasNext() && lIter.next()) ? rIter.next() : 0;
		}

		return result;
	}
}
