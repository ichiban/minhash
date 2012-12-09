package com.y1ban.recommender.minhash.repository.berkeleydb;

import java.io.Serializable;

import com.google.common.base.Function;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.y1ban.recommender.minhash.Instance;
import com.y1ban.recommender.minhash.SparseBitVector;

@Entity
public class InstanceEntity implements Serializable {
	private static final long serialVersionUID = -8336714576908067202L;

	@PrimaryKey
	public long id;
	public SparseBitVector features;

	public static InstanceEntity of(final Instance instance) {
		final InstanceEntity entity = new InstanceEntity();
		entity.id = instance.id;
		entity.features = instance.features;
		return entity;
	}

	public Instance asInstance() {
		return Instance.of(id, features);
	}

	public static Function<InstanceEntity, Instance> asInstanceFunction = new Function<InstanceEntity, Instance>() {
		public Instance apply(InstanceEntity entity) {
			return entity.asInstance();
		}
	};
}
