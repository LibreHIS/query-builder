package com.ims.query.builder;

import java.util.ArrayList;

/*
 * Created on Jan 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/**
 * @author vpurdila
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DataField
{
	private String name;
	private ArrayList values;
	private String type;
	
	public DataField(String name, ArrayList values, String type)
	{
		super();
		this.name = name;
		this.values = values;
		this.type = type;
	}

	public DataField(String name, String value, String type)
	{
		super();
		this.name = name;
		this.values = new ArrayList();
		this.values.add(value);
		this.type = type;
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public ArrayList getValues()
	{
		return values;
	}
	public void setValues(ArrayList values)
	{
		this.values = values;
	}
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		this.type = type;
	}
}
