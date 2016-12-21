/*
 * Created on 26-Sep-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.builder;

import java.io.Serializable;

public class FieldHolder implements Serializable 
{
	private String Name;
	private String Type;

	public FieldHolder()
	{
	}
	public FieldHolder(String name, String type)
	{
		Name = name;
		Type = type;
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
}
