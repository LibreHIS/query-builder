/*
 * Created on Feb 21, 2005
 *
 */
package com.ims.query.server;

/**
 * @author vpurdila
 *
 */
public class HibernateInitFlag
{
	private static boolean bHibernateStarted = false;
	
	public static synchronized void setHibernateStarted()
	{
		bHibernateStarted = true;
	}
	
	public static boolean isHibernateStarted()
	{
		return bHibernateStarted;
	}
}
