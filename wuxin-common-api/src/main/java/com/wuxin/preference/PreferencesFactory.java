package com.wuxin.preference;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

public abstract class PreferencesFactory implements Referenceable {
	public abstract Preferences getPreferences(String paramString);
	public Reference getReference() throws NamingException {
		return null;
	}
}
