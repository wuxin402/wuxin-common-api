package com.wuxin.preference;

import java.util.concurrent.atomic.AtomicReference;

import javax.naming.InitialContext;

import com.wuxin.git.FileSystemPreferencesFactory;

public class PreferencesFactoryLoader {
	public static AtomicReference<PreferencesFactory> factoryRef = new AtomicReference<>();

	public static void setPreferencesFactory(PreferencesFactory factory) {
		factoryRef.set(factory);
	}

	public static void compareAndSetPreferencesFactory(PreferencesFactory expect, PreferencesFactory update) {
		factoryRef.compareAndSet(expect, update);
	}

	public static PreferencesFactory getPreferencesFactory() {
		PreferencesFactory factory = (PreferencesFactory) factoryRef.get();

		if (factory != null) {
			return factory;
		}
		try {
			String jndiName = "java:comp/env/platform/preferencesFile";
			InitialContext initCtx = new InitialContext();
			String filePath = (String) initCtx.lookup(jndiName);

			factory = new FileSystemPreferencesFactory(filePath);
			factoryRef.compareAndSet(null, factory);

			return (PreferencesFactory) factoryRef.get();
		} catch (Exception e) {
			throw new RuntimeException("getPreferencesFactory fail", e);
		}
	}
}
