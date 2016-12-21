/*
 * Created on Mar 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.builder.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vpurdila
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

public class CacheScript
{
	private static Map scripts = null;

	public static synchronized boolean hasKey(Long key)
	{
		if(scripts == null)
			return false;
		
		return scripts.containsKey(key);
	}
	
	public static synchronized void cacheObject(Long key, Object obj)
	{
		if(scripts == null)
			scripts = new HashMap();
		
		scripts.put(key, obj);
	}
	
	public static synchronized Object getCachedObject(Long key)
	{
		if(scripts == null)
			return null;
		
		return scripts.get(key);
	}
	
}
