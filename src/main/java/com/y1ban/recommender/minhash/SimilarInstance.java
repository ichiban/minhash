package com.y1ban.recommender.minhash;

public class SimilarInstance implements Comparable<SimilarInstance> {
	public final Instance instance;
	public final double resemblance;
	
	private SimilarInstance(final Instance instance, final double resemblance) {
		this.instance = instance;
		this.resemblance = resemblance;
	}
	
	public static SimilarInstance of(final Instance instance, final double resemblance) {
		return new SimilarInstance(instance, resemblance);
	}

	public int compareTo(SimilarInstance o) {
		return Double.compare(resemblance, o.resemblance);
	}

	@Override
	public String toString() {
		return String.format("[%s:%f]", instance.toString(), resemblance);
	}
}
