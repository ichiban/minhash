package com.y1ban.recommender.minhash.repository;

import java.util.List;

import com.y1ban.recommender.minhash.Instance;
import com.y1ban.recommender.minhash.SimilarInstance;

public interface InstanceRepository {
	void put(Instance instance);

	void put(Iterable<Instance> instances);

	void delete(Instance instance);

	Instance findById(long id);

	List<SimilarInstance> findByHash(List<Integer> hash,
			double resemblance);
}
