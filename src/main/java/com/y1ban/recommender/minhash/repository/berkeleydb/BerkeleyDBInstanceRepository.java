package com.y1ban.recommender.minhash.repository.berkeleydb;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sleepycat.persist.PrimaryIndex;
import com.y1ban.recommender.minhash.Instance;
import com.y1ban.recommender.minhash.SimilarInstance;
import com.y1ban.recommender.minhash.repository.InstanceRepository;

public class BerkeleyDBInstanceRepository implements InstanceRepository {

	private final PrimaryIndex<Long, InstanceEntity> instanceEntityById;
	private final PrimaryIndex<HashKey, BagEntity> bagEntityByHashKey;
	private final List<Integer> seeds;

	@Inject
	public BerkeleyDBInstanceRepository(
			PrimaryIndex<Long, InstanceEntity> instanceEntityById,
			PrimaryIndex<HashKey, BagEntity> bagEntityByHashKey,
			@Named("seeds") List<Integer> seeds) {
		this.instanceEntityById = instanceEntityById;
		this.bagEntityByHashKey = bagEntityByHashKey;
		this.seeds = seeds;
	}

	public void put(Instance instance) {
		put(Lists.newArrayList(instance));
	}

	public void put(Iterable<Instance> instances) {
		for (Instance instance : instances) {
			instanceEntityById.put(InstanceEntity.of(instance));
			final List<Integer> minhashes = instance.minhashes(seeds);
			for (int i = 0; i < minhashes.size(); i++) {
				final HashKey hashKey = HashKey.of(i, minhashes.get(i));
				final BagEntity bagEntity = bagEntityByHashKey.get(hashKey);
				if (null == bagEntity) {
					bagEntityByHashKey.put(BagEntity.of(hashKey,
							Sets.newHashSet(instance.id)));
				} else {
					final Set<Long> instanceEntities = bagEntity.instanceIds;
					instanceEntities.add(instance.id);
					bagEntityByHashKey.put(BagEntity.of(hashKey,
							instanceEntities));
				}
			}
		}
	}

	public void delete(Instance instance) {
		instanceEntityById.delete(InstanceEntity.of(instance).id);
	}

	public Instance findById(long id) {
		return instanceEntityById.get(id).asInstance();
	}

	public List<SimilarInstance> findByHash(List<Integer> hash,
			double minResemblance) {
		final List<Set<Instance>> bags = Lists.newArrayList();
		for (int i = 0; i < hash.size(); i++) {
			final HashKey hashKey = HashKey.of(i, hash.get(i));
			final BagEntity bagEntity = bagEntityByHashKey.get(hashKey);
			bags.add(Sets.newHashSet(Iterables.transform(bagEntity.instanceIds,
					new Function<Long, Instance>() {
						public Instance apply(Long id) {
							return instanceEntityById.get(id).asInstance();
						}

					})));
		}

		final Set<Instance> all = union(bags);
		final Map<Instance, Integer> counts = Maps.newHashMap();
		for (Instance instance : all) {
			counts.put(instance, 0);
		}
		for (Instance instance : all) {
			for (Set<Instance> bag : bags) {
				if (bag.contains(instance)) {
					counts.put(instance, counts.get(instance) + 1);
				}
			}
		}

		final TreeSet<SimilarInstance> result = Sets.newTreeSet();
		final double size = bags.size();
		for (Entry<Instance, Integer> entry : counts.entrySet()) {
			final double resemblance = entry.getValue() / size;
			if (resemblance >= minResemblance) {
				result.add(SimilarInstance.of(entry.getKey(), resemblance));
			}
		}

		return Lists.newArrayList(result.descendingIterator());
	}

	private Set<Instance> union(Iterable<Set<Instance>> sets) {
		Set<Instance> result = Sets.newHashSet();
		for (Set<Instance> instances : sets) {
			result.addAll(instances);
		}
		return result;
	}
}
