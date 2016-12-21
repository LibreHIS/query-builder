package com.ims.query.builder;
/*
 * Created on Jan 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.type.Type;

import com.ims.query.server.CsvUtils;
import com.ims.query.server.StringUtils;

/**
 * @author vpurdila
 *
 */
@SuppressWarnings("rawtypes")
public abstract class ImsHQLQuery
{
	protected Session session;
	protected ArrayList fields = new ArrayList();
	protected Object[] field_values = null;
	
	protected String[] fieldNames = null;
	protected String[] fieldTypes = null;
	protected int[] maxFieldSize = null;
	protected StringBuffer[] xmlField = null;
	protected StringBuffer csvValues = null;
	
	protected DateFormat qbDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	protected DateFormat qbTimeFormat = new SimpleDateFormat("HH:mm");
	protected DateFormat qbDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	protected boolean bHasFields = true;
	protected String dsName = "";
	
	protected int startIndex = -1;
	protected int endIndex = -1;
	protected String xmlClauses;
	protected ArrayList usedSeeds;
	
	protected StringBuffer systemLog;
	protected static final String CRNL = "\n";
	
	public ImsHQLQuery()
	{
	}

	public ArrayList getFields()
	{
		return fields;
	}

	public void setFields(ArrayList fields)
	{
		this.fields = fields;
	}
	
	
	public boolean isBHasFields()
	{
		return bHasFields;
	}

	public void setBHasFields(boolean hasFields)
	{
		bHasFields = hasFields;
	}

	public String getDsName()
	{
		return dsName;
	}

	public void setDsName(String dsName)
	{
		this.dsName = dsName;
	}
	
	public StringBuffer getSystemLog()
	{
		return systemLog;
	}

	public void setSystemLog(StringBuffer systemLog)
	{
		this.systemLog = systemLog;
	}

	public abstract void ExecuteQuery() throws HibernateException;

	public Object getFieldValueByIndex(int index)
	{
		if(field_values == null || field_values[index] == null)
				return null;
		
		if(field_values[index] instanceof java.math.BigDecimal)
		{
			if("java.lang.Integer".equalsIgnoreCase(fieldTypes[index]))
			{
				return new Integer(((java.math.BigDecimal)field_values[index]).intValue());
			}
			else if("java.lang.Float".equalsIgnoreCase(fieldTypes[index]))
			{
				return new Float(((java.math.BigDecimal)field_values[index]).floatValue());
			}
			else if("java.lang.Double".equalsIgnoreCase(fieldTypes[index]))
			{
				return new Float(((java.math.BigDecimal)field_values[index]).doubleValue());
			}
		}
		
		return field_values[index];	
	}
	
	public void addFieldValue(int index, String value)
	{
		if(xmlField[index] == null)
			xmlField[index] = new StringBuffer();
		
		StringBuffer sb = xmlField[index];
		
		sb.append("<v>");
		sb.append(StringUtils.encodeXML(value));
		sb.append("</v>");
	}
	
	@SuppressWarnings("unchecked")
	public void addFieldValue(String name, String value, String type)
	{
		boolean bFound = false;
		DataField df = null;
		
		for(int i = 0; i < fields.size(); i++)
		{
			df = (DataField)fields.get(i);
			
			if(df.getName().equalsIgnoreCase(name))
			{
				bFound = true;
				break;
			}
		}
		
		if(bFound == true)
		{
			df.getValues().add(value);
		}
		else
		{
			df = new DataField(name, value, type);
			fields.add(df);
		}
	}

	private Type getParamType(String type)
	{
		if(type.equalsIgnoreCase("java.lang.Integer"))
		{
			return Hibernate.INTEGER;
		}
		else if(type.equalsIgnoreCase("java.math.BigInteger"))
		{
			return Hibernate.LONG;
		}
		else if(type.equalsIgnoreCase("java.lang.Long"))
		{
			return Hibernate.LONG;
		}
		else if(type.equalsIgnoreCase("java.lang.Short"))
		{
			return Hibernate.SHORT;
		}
		else if(type.equalsIgnoreCase("java.lang.Boolean"))
		{
			return Hibernate.BOOLEAN;
		}
		else if(type.equalsIgnoreCase("java.lang.String"))
		{
			return Hibernate.STRING;
		}
		else if(type.equalsIgnoreCase("java.math.BigDecimal"))
		{
			return Hibernate.BIG_DECIMAL;
		}
		else if(type.equalsIgnoreCase("java.lang.Float"))
		{
			return Hibernate.FLOAT;
		}
		else if(type.equalsIgnoreCase("java.lang.Double"))
		{
			return Hibernate.DOUBLE;
		}
		else if(type.equalsIgnoreCase("java.sql.Date"))
		{
			return Hibernate.DATE;
		}
		else if(type.equalsIgnoreCase("java.sql.Time"))
		{
			return Hibernate.TIME;
		}
		else if(type.equalsIgnoreCase("java.util.Date"))
		{
			return Hibernate.TIMESTAMP;
		}
		else
		{
			return Hibernate.INTEGER;
		}
	}
	
	public boolean hasFields()
	{
		return bHasFields;
	}

	public void setHasFields(boolean hasFields)
	{
		bHasFields = hasFields;
	}
	
	public char getFieldType(String type)
	{
		char c = 'c';
		
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
		
		return c;
	}
	
	public String getAsXml()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("<d name=\"" + StringUtils.encodeXML(dsName) + "\" hasFields=\"" + (bHasFields ? "true" : "false") + "\">");
		
		for(int i = 0; i < fieldNames.length; i++)
		{
			sb.append("<f n=\"");
			sb.append(StringUtils.encodeXML(fieldNames[i]));
			sb.append("\" t=\"");
			sb.append(getFieldType(fieldTypes[i]));
			sb.append("\">");

			sb.append(xmlField[i]);
			sb.append("</f>");
		}
		
		sb.append("</d>");
		
		return sb.toString();
	}
	
	public String getAsCsv()
	{
		CsvUtils csv = new CsvUtils();
		StringBuffer sbHeader = new StringBuffer();
		
		sbHeader.append("\"@@IMSDATASOURCENAME@@\",\"" + dsName + "\"");
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
		
		String fieldList = "";
		String str;
		for (int i = 0; i < fieldNames.length; i++)
		{
			String paramTypeForDelphi = csv.getParamTypeForDelphi(fieldTypes[i]);
			
			if(paramTypeForDelphi.equalsIgnoreCase("String"))
				paramTypeForDelphi = "Memo";

			str = fieldNames[i] + "=" + paramTypeForDelphi + 
			"," + "0" + 
			",\"" + fieldNames[i] + "\"" + 
			"," + "\"\"" +
			"," + "110" +
			"," + "Data" +
			"," + "\"\"";
			
			sbHeader.append(csv.ansiQuotedStr(str, '"'));
			sbHeader.append('\r');
			sbHeader.append('\n');
			
			if(fieldList.length() > 0)
				fieldList += ",";
			
			fieldList += "\"" + fieldNames[i] + "\"";
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
		
		if(csvValues != null)
			sbHeader.append(csvValues);
		
		return sbHeader.toString();
	}

	public void addCsvHeader(Compressor comp) throws IOException
	{
		CsvUtils csv = new CsvUtils();
		StringBuffer sbHeader = new StringBuffer();
		
		sbHeader.append("\"@@IMSDATASOURCENAME@@\",\"" + dsName + "\"");
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
		
		String fieldList = "";
		String str;
		for (int i = 0; i < fieldNames.length; i++)
		{
			String paramTypeForDelphi = csv.getParamTypeForDelphi(fieldTypes[i]);
			
			if(paramTypeForDelphi.equalsIgnoreCase("String"))
				paramTypeForDelphi = "Memo";

			str = fieldNames[i] + "=" + paramTypeForDelphi + 
			"," + "0" + 
			",\"" + fieldNames[i] + "\"" + 
			"," + "\"\"" +
			"," + "110" +
			"," + "Data" +
			"," + "\"\"";
			
			sbHeader.append(csv.ansiQuotedStr(str, '"'));
			sbHeader.append('\r');
			sbHeader.append('\n');
			
			if(fieldList.length() > 0)
				fieldList += ",";
			
			fieldList += "\"" + fieldNames[i] + "\"";
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
		
		comp.append(sbHeader.toString());
	}
	
	public void addCsvRow(Object[] row)
	{
		String val = "";
		String fieldValues = "";
		
		CsvUtils csv = new CsvUtils();
		
		if(csvValues == null)
			csvValues = new StringBuffer();
		
		for (int i = 0; i < row.length; i++)
		{
			if (row[i] instanceof java.sql.Date)
			{
				val = (row[i] == null ? "" : qbDateFormat.format(row[i]));
			}
			else if (row[i] instanceof java.sql.Time)
			{
				val = (row[i] == null ? "" : qbTimeFormat.format(row[i]));
			}
			else if (row[i] instanceof java.util.Date)
			{
				val = (row[i] == null ? "" : qbDateTimeFormat.format(row[i]));
			}
			else if (row[i] instanceof Boolean)
			{
				Boolean bool = (Boolean)row[i];
				
				val = (bool == null ? "" : (bool.booleanValue() == true ? "True" : "False"));
			}
			else
			{
				val = (row[i] == null ? "" : row[i].toString());
			}
			
			if(val.length() > maxFieldSize[i])
				maxFieldSize[i] = val.length();
			
			if(i > 0)
				fieldValues += ",";
			
			if(val.length() > 0)
				fieldValues += csv.ansiQuotedStr(csv.stringToCodedString(val), '"');
		}	
		
		csvValues.append(fieldValues);
		csvValues.append('\r');
		csvValues.append('\n');
	}
	
	public void addGzipCsvRow(Compressor comp, Object[] row) throws IOException
	{
		String val = "";
		String fieldValues = "";
		
		CsvUtils csv = new CsvUtils();
		
		if(csvValues == null)
			csvValues = new StringBuffer();
		
		for (int i = 0; i < row.length; i++)
		{
			if (row[i] instanceof java.sql.Date)
			{
				val = (row[i] == null ? "" : qbDateFormat.format(row[i]));
			}
			else if (row[i] instanceof java.sql.Time)
			{
				val = (row[i] == null ? "" : qbTimeFormat.format(row[i]));
			}
			else if (row[i] instanceof java.util.Date)
			{
				val = (row[i] == null ? "" : qbDateTimeFormat.format(row[i]));
			}
			else if (row[i] instanceof Boolean)
			{
				Boolean bool = (Boolean)row[i];
				
				val = (bool == null ? "" : (bool.booleanValue() == true ? "True" : "False"));
			}
			else
			{
				val = (row[i] == null ? "" : row[i].toString());
			}
			
			if(val.length() > maxFieldSize[i])
				maxFieldSize[i] = val.length();
			
			if(i > 0)
				fieldValues += ",";
			
			if(val.length() > 0)
				fieldValues += csv.ansiQuotedStr(csv.stringToCodedString(val), '"');
		}	
		
		comp.append(fieldValues);
		comp.append("\r\n");
	}
	
	public String applyValidators(Map seeds, String hql)
	{
		if(startIndex != -1 && endIndex != -1 && xmlClauses != null && xmlClauses.length() > 0)
		{
			StringBuffer sb = new StringBuffer(hql.length());
			
			sb.append(hql.substring(0, startIndex - 1));
			
			ClauseParser parser = new ClauseParser();
			parser.setXmlClause(xmlClauses);
			Iterator keys = seeds.keySet().iterator();
			String key;
			while (keys.hasNext())
			{
				key = (String) keys.next();

				parser.setSeed(key, ((SeedHolder) seeds.get(key)));
			}
			
			sb.append(' ');
			sb.append(parser.buildHql());
			sb.append(' ');
			if(endIndex < hql.length())
				sb.append(hql.substring(endIndex));
			
			String newHql = sb.toString();
			usedSeeds = parser.ExtractNamedParameters(newHql);
			
			System.out.println(newHql);
			
			return newHql;
		}
		else
			return hql;
	}
	
	protected Object[] makeObjectArray(Object obj)
	{
		if(obj instanceof Object[])
			return (Object[])obj;
		
		Object[] res = new Object[1];
		
		res[0] = obj;
		
		return res;
	}
	
	protected void updateUsedSeeds(String hql)
	{
		ClauseParser parser = new ClauseParser();
		
		usedSeeds = parser.ExtractNamedParameters(hql);
	}
}
