package com.y1ban.recommender.minhash;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;

public class Instance {

	public final long id;
	public final SparseBitVector features;

	private Instance(final long id, final SparseBitVector features) {
		this.id = id;
		this.features = features;
	}

	public static Instance of(final long id, final SparseBitVector features) {
		return new Instance(id, features);
	}

	public static Instance of(final long id) {
		return of(id, new SparseBitVector());
	}

	public void addFeature(int feature) {
		features.add(feature);
	}

	public void removeFeature(int feature) {
		features.remove(feature);
	}

	public List<Integer> minhashes(final List<Integer> seeds) {
		return Lists.newArrayList(Iterables.transform(seeds,
				new Function<Integer, Integer>() {
					public Integer apply(Integer seed) {
						int minhash = Integer.MAX_VALUE;
						for (int feature : features) {
							int hash = Hashing.murmur3_32(seed)
									.hashInt(feature).asInt();
							if (hash < minhash) {
								minhash = hash;
							}
						}
						return minhash;
					}
				}));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Instance && ((Instance) obj).id == id;
	}

	@Override
	public String toString() {
		return "instance:" + id;
	}

	@Override
	public int hashCode() {
		return new Long(id).hashCode();
	}
}
