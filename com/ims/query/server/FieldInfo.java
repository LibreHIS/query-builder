/*
 * Created on 7 Mar 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.server;

public class FieldInfo
{
	private String fieldName = "";
	private boolean allowNull = true;
	private boolean isUnique = false;
	private boolean isIndexed = false;
	private String indexNames = "";

	public FieldInfo()
	{
		
	}
	
	public FieldInfo(String fieldName, boolean allowNull, boolean isUnique, boolean isIndexed, String indexNames)
	{
		this.fieldName = fieldName;
		this.allowNull = allowNull;
		this.isUnique = isUnique;
		this.isIndexed = isIndexed;
		this.indexNames = indexNames;
	}
	
	public boolean isAllowNull()
	{
		return allowNull;
	}
	public void setAllowNull(boolean allowNull)
	{
		this.allowNull = allowNull;
	}
	public String getFieldName()
	{
		return fieldName;
	}
	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	public String getIndexNames()
	{
		return indexNames;
	}
	public void setIndexNames(String indexNames)
	{
		this.indexNames = indexNames;
	}
	public boolean isIndexed()
	{
		return isIndexed;
	}
	public void setIndexed(boolean isIndexed)
	{
		this.isIndexed = isIndexed;
	}
	public boolean isUnique()
	{
		return isUnique;
	}
	public void setUnique(boolean isUnique)
	{
		this.isUnique = isUnique;
	}	
	
	
}