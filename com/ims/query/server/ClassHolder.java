/*
 * Created on Feb 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.server;

/**
 * @author vpurdila
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ClassHolder implements Comparable
{
	private Class clazz;

	public ClassHolder(Class clazz)
	{
		this.clazz = clazz;
	}

	public Class getClazz()
	{
		return clazz;
	}
	public void setClazz(Class clazz)
	{
		this.clazz = clazz;
	}
	
	public int compareTo(Object other)
	{
		if (!(other instanceof ClassHolder))
		      throw new ClassCastException("A ClassHolder object is expected.");
		
		return clazz.getName().compareTo(((ClassHolder)other).getClazz().getName());
	}
}
