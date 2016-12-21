/*
 * Created on Feb 23, 2005
 *
 */
package com.ims.query.server;

/**
 * @author vpurdila
 *
 */

public class AttrType
{
	private int nType;
	
	public static final AttrType PROPERTY = new AttrType(0);
	public static final AttrType ONE2MANY = new AttrType(1);
	public static final AttrType MANY2ONE = new AttrType(2);
	public static final AttrType ONE2ONE = new AttrType(3);
	public static final AttrType ANY = new AttrType(4);
	public static final AttrType COMPONENT = new AttrType(5);
	public static final AttrType ID = new AttrType(6);
	public static final AttrType UNKNOWN = new AttrType(7);
	public static final AttrType VERSION = new AttrType(8);
	
	public AttrType(int type)
	{
		nType = type;
	}
	public String toString()
	{
		return String.valueOf(nType);
	}
	
	public int getValue()
	{
		return nType;
	}
}

