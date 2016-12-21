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
public class CacheInterpreter
{
	private static Map interpreters = null;

	public static synchronized boolean hasKey(Long key)
	{
		if(interpreters == null)
			return false;
		
		return interpreters.containsKey(key);
	}
	
	public static synchronized void cacheObject(Long key, Object obj)
	{
		if(interpreters == null)
			interpreters = new HashMap();
		
		interpreters.put(key, obj);
	}
	
	public static synchronized Object getCachedObject(Long key)
	{
		if(interpreters == null)
			return null;
		
		return interpreters.get(key);
	}
	
}
