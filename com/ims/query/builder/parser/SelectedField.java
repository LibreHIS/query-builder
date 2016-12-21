/*
 * Created on Jan 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.builder.parser;


/**
 * @author vpurdila
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SelectedField
{
	private String BusinessObject;
	private String FieldName;
	private String _FieldType;
	private int FieldSize;
	private int Agregate;
	
	public SelectedField(String businessObject, String fieldName,
			String fieldType, int fieldSize, int agregate)
	{
		super();
		BusinessObject = businessObject;
		FieldName = fieldName;
		_FieldType = fieldType;
		FieldSize = fieldSize;
		Agregate = agregate;
	}
	
	public SelectedField()
	{
		super();
	}
	
	public int getAgregate()
	{
		return Agregate;
	}
	public void setAgregate(int agregate)
	{
		Agregate = agregate;
	}
	public String getBusinessObject()
	{
		return BusinessObject;
	}
	public void setBusinessObject(String businessObject)
	{
		BusinessObject = businessObject;
	}
	public String getFieldName()
	{
		return FieldName;
	}
	public void setFieldName(String fieldName)
	{
		FieldName = fieldName;
	}
	public int getFieldSize()
	{
		return FieldSize;
	}
	public void setFieldSize(int fieldSize)
	{
		FieldSize = fieldSize;
	}
	public String getFieldType()
	{
		return _FieldType;
	}
	public void setFieldType(String fieldType)
	{
		_FieldType = fieldType;
	}
	
	public String getAgregatePrefix()
	{
		if(Agregate == 0) //COUNT
			return "CountOf_";
		else if(Agregate == 1) //MIN
				return "MinOf_";
		else if(Agregate == 2) //MAX
			return "MaxOf_";
		else if(Agregate == 3) //SUM
			return "SumOf_";
		else if(Agregate == 4) //AVG
			return "AvgOf_";

		return "";
	}

	public String getFieldTypeAsString()
	{
		if(this._FieldType.startsWith(JL))
			return this._FieldType.substring(JL.length());
		else if(this._FieldType.startsWith(JM))
			return this._FieldType.substring(JM.length());
		else if(this._FieldType.startsWith(JS))
			return this._FieldType.substring(JS.length());
		else if(this._FieldType.startsWith(JU))
			return this._FieldType.substring(JU.length());
		
		return this._FieldType;
	}
	
	private static final String JL = "java.lang.";
	private static final String JM = "java.math.";
	private static final String JS = "java.sql.";
	private static final String JU = "java.util.";
}
