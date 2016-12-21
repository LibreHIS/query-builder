/*
 * Created on Jan 24, 2005
 *
 */
package com.ims.query.builder.parser;


/**
 * @author vpurdila
 *
 */
public class Param
{
	private String Query;
	private int Index;
	private String _FieldType;
	private String Field;
	
	public Param(String query, int index, String fieldType, String asField)
	{
		super();
		Query = query;
		Index = index;
		_FieldType = fieldType;
		Field = asField;
	}
	
	public Param()
	{
		super();
	}
	
	public int getIndex()
	{
		return Index;
	}
	public void setIndex(int index)
	{
		Index = index;
	}
	public String getQuery()
	{
		return Query;
	}
	public void setQuery(String query)
	{
		Query = query;
	}
	
	public String getFieldType()
	{
		return _FieldType;
	}
	public void setFieldType(String fieldType)
	{
		_FieldType = fieldType;
	}
	
	public String getField()
	{
		return Field;
	}
	public void setField(String field)
	{
		Field = field;
	}
	public String getFieldAltered()
	{
		return replaceChar(Field, '.', '_');
	}
	
	private String replaceChar(String val, char cWhat, char cWith)
	{
		StringBuffer sb = new StringBuffer(val.length());
		char c;
		
		for(int i = 0; i < val.length(); i++)
		{
			c = val.charAt(i);
			
			if(c == cWhat)
				c = cWith;
			
			sb.append(c);
		}
		
		return sb.toString();
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
	
	public String getNormalizedFieldType()
	{
		if(this._FieldType.startsWith(JL) || this._FieldType.startsWith(JM)
				|| this._FieldType.startsWith(JS) || this._FieldType.startsWith(JU))
			return this._FieldType;
		
		return "java.lang.Integer";
	}
	
	private static final String JL = "java.lang.";
	private static final String JM = "java.math.";
	private static final String JS = "java.sql.";
	private static final String JU = "java.util.";
}
