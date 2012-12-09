package com.y1ban.recommender.minhash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.y1ban.recommender.minhash.repository.InstanceRepository;
import com.y1ban.recommender.minhash.repository.berkeleydb.BagEntity;
import com.y1ban.recommender.minhash.repository.berkeleydb.BerkeleyDBInstanceRepository;
import com.y1ban.recommender.minhash.repository.berkeleydb.HashKey;
import com.y1ban.recommender.minhash.repository.berkeleydb.InstanceEntity;

public class DefaultModule extends AbstractModule {

	@Override
	protected void configure() {
		Names.bindProperties(binder(), loadProperties("/app.properties"));
		bind(InstanceRepository.class).to(BerkeleyDBInstanceRepository.class);
		bind(Random.class).toInstance(new Random());
	}

	private Properties loadProperties(final String name) {
		final Properties properties = new Properties();
		final InputStream in = DefaultModule.class.getResourceAsStream(name);
		try {
			properties.load(in);
		} catch (IOException e) {
			addError(e);
		}
		return properties;
	}

	@Provides
	@Singleton
	EnvironmentConfig environmentConfig() {
		EnvironmentConfig environmentConfig = new EnvironmentConfig();
		environmentConfig.setAllowCreate(true);
		environmentConfig.setTransactional(true);
		return environmentConfig;
	}

	@Provides
	@Named("dataDir")
	@Singleton
	File dataDir(@Named("dataDirectory") String dataDirectory)
			throws IOException {
		File dataDir = new File(dataDirectory);
		FileUtils.forceMkdir(dataDir);
		return dataDir;
	}

	@Provides
	@Singleton
	Environment environment(@Named("dataDir") File dataDir,
			EnvironmentConfig environmentConfig) {
		return new Environment(dataDir, environmentConfig);
	}

	@Provides
	@Singleton
	StoreConfig storeConfig() {
		StoreConfig storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(true);
		storeConfig.setTransactional(true);
		return storeConfig;
	}

	@Provides
	@Singleton
	EntityStore entityStore(Environment environment,
			@Named("storeName") String storeName, StoreConfig storeConfig) {
		return new EntityStore(environment, storeName, storeConfig);
	}

	@Provides
	@Named("seeds")
	@Singleton
	List<Integer> seeds(@Named("seedNumbers") String seeds) {
		return Lists.newArrayList(Iterables.transform(Splitter.on(',')
				.omitEmptyStrings().trimResults().split(seeds),
				new Function<String, Integer>() {
					public Integer apply(String seed) {
						return Integer.valueOf(seed);
					}
				}));
	}

	@Provides
	@Singleton
	PrimaryIndex<Long, InstanceEntity> instanceEntityById(EntityStore store) {
		return store.getPrimaryIndex(Long.class, InstanceEntity.class);
	}
	
	@Provides
	@Singleton
	PrimaryIndex<HashKey, BagEntity> bagEntityByHashKey(EntityStore store) {
		return store.getPrimaryIndex(HashKey.class, BagEntity.class);
	}
	
	@Provides
	ConnectionType connectionType(@Named("connectionType") String connectionType) {
		return ConnectionType.valueOf(connectionType);
	}
}
