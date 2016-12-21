/*
 * Created on Jan 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.builder.flags;

/**
 * @author vpurdila
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class FieldType
{
	private int nType;
	
	public static final FieldType TYPE_INTEGER = new FieldType(0);
	public static final FieldType TYPE_BOOLEAN = new FieldType(1);
	public static final FieldType TYPE_STRING = new FieldType(2);
	public static final FieldType TYPE_DECIMAL = new FieldType(3);
	public static final FieldType TYPE_DATE = new FieldType(4);
	public static final FieldType TYPE_TIME = new FieldType(5);
	public static final FieldType TYPE_DATETIME = new FieldType(6);
	public static final FieldType TYPE_OBJECT = new FieldType(7);
	
	public FieldType(int type)
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
