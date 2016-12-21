package com.ims.query.builder;

/*
 * Created on Jan 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
import java.util.ArrayList;

import com.ims.query.server.CsvUtils;
import com.ims.query.server.StringUtils;

/**
 * @author vpurdila
 *
 */
public class DataSource
{
	private static final String IMS_RTF_TAG = "<!-- _ims_rich_text_control_1234567890_tag_ -->";

	private String name;
	private ArrayList fields;
	private boolean bHasFields = true;
	
	public DataSource(String name, ArrayList fields)
	{
		super();
		this.name = name;
		this.fields = fields;
	}

	public DataSource(String name, ArrayList fields, boolean bHasFields)
	{
		super();
		this.name = name;
		this.fields = fields;
		this.bHasFields = bHasFields;
	}
	
	public ArrayList getFields()
	{
		return fields;
	}
	public void setFields(ArrayList fields)
	{
		this.fields = fields;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	public boolean hasFields()
	{
		return bHasFields;
	}

	public void setHasFields(boolean hasFields)
	{
		bHasFields = hasFields;
	}
	
	public String toXml()
	{
		String type;
		char c = 'c';
		StringBuffer sb = new StringBuffer();
		
		sb.append("<d name=\"" + getName() + "\" hasFields=\"" + (hasFields() ? "true" : "false") + "\">");
		for (int j = 0; j < getFields().size(); j++)
		{
			DataField df = (DataField)getFields().get(j);
			
			type = df.getType();
			
			if(type.equalsIgnoreCase(DataSource2XML.JL_INTEGER))
				c = '0';
			else if(type.equalsIgnoreCase(DataSource2XML.JM_BIGINTEGER))
				c = '1';
			else if(type.equalsIgnoreCase(DataSource2XML.JL_LONG))
				c = '2';
			else if(type.equalsIgnoreCase(DataSource2XML.JL_SHORT))
				c = '3';
			else if(type.equalsIgnoreCase(DataSource2XML.JL_BOOLEAN))
				c = '4';
			else if(type.equalsIgnoreCase(DataSource2XML.JL_STRING))
				c = '5';
			else if(type.equalsIgnoreCase(DataSource2XML.JM_BIGDECIMAL))
				c = '6';
			else if(type.equalsIgnoreCase(DataSource2XML.JL_FLOAT))
				c = '7';
			else if(type.equalsIgnoreCase(DataSource2XML.JL_DOUBLE))
				c = '8';
			else if(type.equalsIgnoreCase(DataSource2XML.JS_DATE))
				c = '9';
			else if(type.equalsIgnoreCase(DataSource2XML.JS_TIME))
				c = 'a';
			else if(type.equalsIgnoreCase(DataSource2XML.JU_DATE))
				c = 'b';
			else if(type.equalsIgnoreCase(DataSource2XML.JL_OBJECT))
				c = 'c';
			
			sb.append("<f n=\"" + df.getName() + "\" t=\"" + c + "\">");
				//sb.append("<n>" + df.getName() + "</n>");
				for(int k=0; k < df.getValues().size(); k++)
				{
					sb.append("<v>" + StringUtils.encodeXML((String)df.getValues().get(k)) + "</v>");
				}
			sb.append("</f>");
		}
		
		sb.append("</d>");
		
		return sb.toString();
	}
	
	public String toCsv()
	{
		CsvUtils csv = new CsvUtils();
		StringBuffer sbHeader = new StringBuffer();
		
		int[] maxFieldSize = new int[getFields().size()];
		boolean[] bHtmlField = new boolean[getFields().size()];
		
		for(int i = 0; i < maxFieldSize.length; i++)
		{
			maxFieldSize[i] = 1;
			bHtmlField[i] = false;
		}
		
		sbHeader.append("\"@@IMSDATASOURCENAME@@\",\"" + getName() + "\"");
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append(csv.ansiQuotedStr("@@FILE VERSION@@", '"'));
		sbHeader.append(',');
		sbHeader.append(csv.ansiQuotedStr("251", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append(csv.ansiQuotedStr("@@TABLEDEF START@@", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');

		int nMaxRec = 0;
		for (int i = 0; i < getFields().size(); i++)
		{
			DataField df = (DataField)getFields().get(i);
			
			if(df.getValues().size() > nMaxRec)
				nMaxRec = df.getValues().size();
		}
		
		String val;
		String fieldValues;
		StringBuffer csvValues = new StringBuffer();
		for(int i = 0; i < nMaxRec; i++)
		{
			fieldValues = "";
			for (int j = 0; j < getFields().size(); j++)
			{
				DataField df = (DataField)getFields().get(j);

				if(i < df.getValues().size())
				{
					val = (String)df.getValues().get(i);
					
					if(val != null && df.getType().equalsIgnoreCase("java.lang.Boolean"))
					{
						if(val.equalsIgnoreCase("true"))
							val = "True";
						else 
							val = "False";
					}
				}
				else
					val = "";
				
				if(val.length() > maxFieldSize[j])
					maxFieldSize[j] = val.length();
				
				if(df.getType().equalsIgnoreCase("java.lang.String") && val.indexOf(IMS_RTF_TAG) > -1)
					bHtmlField[j] = true;
				
				if(j > 0)
					fieldValues += ",";
				
				if(val.length() > 0)
					fieldValues += csv.ansiQuotedStr(csv.stringToCodedString(val), '"');
			}
			
			csvValues.append(fieldValues);
			csvValues.append('\r');
			csvValues.append('\n');
		}
		
		String fieldList = "";
		String str;
		for (int i = 0; i < getFields().size(); i++)
		{
			DataField df = (DataField)getFields().get(i);
			String paramTypeForDelphi = csv.getParamTypeForDelphi(df.getType());

			if(paramTypeForDelphi.equalsIgnoreCase("String"))
				paramTypeForDelphi = "Memo";
			
			str = df.getName() + "=" + paramTypeForDelphi + 
			"," + "0" + 
			",\"" + df.getName() + "\"" + 
			"," + "\"\"" +
			"," + "110" +
			"," + "Data" +
			"," + "\"\"";
			
			sbHeader.append(csv.ansiQuotedStr(str, '"'));
			sbHeader.append('\r');
			sbHeader.append('\n');
			
			if(fieldList.length() > 0)
				fieldList += ",";
			
			fieldList += "\"" + df.getName() + "\"";
			
			//System.out.println(df.getName() + " >> " + bHtmlField[i]);
		}
		
		sbHeader.append(csv.ansiQuotedStr("@@INDEXDEF START@@", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append(csv.ansiQuotedStr("@@INDEXDEF END@@", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append(csv.ansiQuotedStr("@@TABLEDEF END@@", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append(fieldList);
		sbHeader.append('\r');
		sbHeader.append('\n');
		
		sbHeader.append(csvValues);
		
		return sbHeader.toString();
	}
	
	public DataField getDataFieldByName(String name)
	{
		if(fields == null || fields.size() == 0)
			return null;
		
		for (int i = 0; i < fields.size(); i++)
		{
			DataField df = (DataField) fields.get(i);
			
			if(df != null && df.getName().equalsIgnoreCase(name))
				return df;
		}
		
		return null;
	}
}

