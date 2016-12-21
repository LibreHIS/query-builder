/*
 * Created on Apr 18, 2005
 *
 */
package com.ims.query.server;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vpurdila
 *
 */
public class ResultCollection
{
	private static Map results = new HashMap();
	
	public ResultCollection()
	{
	}
	
	public static synchronized ResultHolder getResult(String key)
	{
		return (ResultHolder)results.get(key);
	}
	
	public static synchronized void putResult(String key, ResultHolder result)
	{
		results.put(key, result);
	}
	
	public static synchronized void clearResult(String key)
	{
		results.remove(key);
	}
}
;