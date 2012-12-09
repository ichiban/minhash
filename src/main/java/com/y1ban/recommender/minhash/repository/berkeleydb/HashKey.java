package com.y1ban.recommender.minhash.repository.berkeleydb;

import java.io.Serializable;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

@Persistent
public class HashKey implements Serializable {
	private static final long serialVersionUID = 3232240836436683215L;
	
	@KeyField(1)
	public int position;
	@KeyField(2)
	public int hash;
	
	public static HashKey of(final int position, final int hash) {
		final HashKey key = new HashKey();
		key.position = position;
		key.hash = hash;
		return key;
	}
}
