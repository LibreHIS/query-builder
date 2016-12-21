/*
 * Created on Mar 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.builder;

/**
 * @author vpurdila
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SeedHolder
{
	private String Name;
	private String Type;
	private Object Value;

	public SeedHolder(String name, String type, Object value)
	{
		Name = name;
		Type = type;
		Value = value;
	}
	
	public String getName()
	{
		return Name;
	}
	public void setName(String name)
	{
		Name = name;
	}
	public String getType()
	{
		return Type;
	}
	public void setType(String type)
	{
		Type = type;
	}
	public Object getValue()
	{
		return Value;
	}
	public void setValue(Object value)
	{
		Value = value;
	}
}
