package com.y1ban.recommender.minhash.repository.berkeleydb;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Sets;
import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class BagEntity implements Serializable {
	private static final long serialVersionUID = -3454025517471552371L;

	@PrimaryKey
	public HashKey hashKey;
	@SecondaryKey(relate = Relationship.MANY_TO_MANY, relatedEntity = InstanceEntity.class, onRelatedEntityDelete = DeleteAction.CASCADE)
	public Set<Long> instanceIds = Sets.newHashSet();

	public static BagEntity of(final HashKey hashKey,
			final Set<Long> instanceIds) {
		final BagEntity bagEntity = new BagEntity();
		bagEntity.hashKey = hashKey;
		bagEntity.instanceIds = instanceIds;
		return bagEntity;
	}
}
