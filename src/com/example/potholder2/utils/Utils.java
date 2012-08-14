package com.example.potholder2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Utils
{
	public static boolean saveSetting(Context c, String key, String value) 
	{
		SharedPreferences shared = PreferenceManager
				.getDefaultSharedPreferences(c);

		Editor e = shared.edit();

		e.putString(key, value);
		return e.commit();
	}
	
	public static String getSetting(Context c, String key, String defaultValue) 
	{

		SharedPreferences shared = PreferenceManager
				.getDefaultSharedPreferences(c);

		if (shared.contains(key)) 
		{
			return shared.getString(key, defaultValue);
		}

		return defaultValue;
	}
}
