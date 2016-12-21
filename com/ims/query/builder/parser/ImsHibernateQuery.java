/*
 * Created on Jan 24, 2005
 *
 */
package com.ims.query.builder.parser;

import java.util.ArrayList;

import com.ims.query.server.StringUtils;

/**
 * @author vpurdila
 *
 */
public class ImsHibernateQuery
{
	private String Name;
	private String Description;
	private ArrayList SelectedFields;
	private ArrayList Params;
	private String HqlString;
	private int StartIndex;
	private int EndIndex;
	private String XmlClauses;
	private int limit;
	private boolean nat;
	
	private ArrayList QueryNames;
	private ArrayList UsedSeeds = null;
	
	public ImsHibernateQuery()
	{
		this.SelectedFields = new ArrayList();
		this.Params = new ArrayList();
		this.limit = 0;
		this.nat = false;
	}
	
	public ImsHibernateQuery(String name, String description,
			ArrayList selectedFields, ArrayList params, String hqlString, int startIndex, int endIndex, String xmlClauses)
	{
		this.Name = name;
		this.Description = description;
		this.SelectedFields = selectedFields;
		this.Params = params;
		this.HqlString = hqlString;
		this.StartIndex = startIndex;
		this.EndIndex = endIndex;
		this.XmlClauses = xmlClauses;
		this.limit = 0;
		this.nat = false;
	}
	public String getDescription()
	{
		return Description;
	}
	public void setDescription(String description)
	{
		Description = description;
	}
	public String getHqlString()
	{
		return HqlString;
	}
	public void setHqlString(String hqlString)
	{
		HqlString = hqlString;
	}
	public String getName()
	{
		return Name;
	}
	public void setName(String name)
	{
		Name = name;
	}
	public ArrayList getParams()
	{
		return Params;
	}
	public void setParams(ArrayList params)
	{
		Params = params;
	}
	public ArrayList getSelectedFields()
	{
		return SelectedFields;
	}
	public void setSelectedFields(ArrayList selectedFields)
	{
		SelectedFields = selectedFields;
	}
	public void setQueryNames(ArrayList qn)
	{
		this.QueryNames = qn;
	}
	
	public int getEndIndex()
	{
		return EndIndex;
	}

	public void setEndIndex(int endIndex)
	{
		EndIndex = endIndex;
	}

	public int getStartIndex()
	{
		return StartIndex;
	}

	public void setStartIndex(int startIndex)
	{
		StartIndex = startIndex;
	}

	public String getXmlClauses()
	{
		return XmlClauses;
	}

	public String getXmlClausesFormatted()
	{
		return StringUtils.replaceSeparators(XmlClauses);
	}
	
	public void setXmlClauses(String xmlClauses)
	{
		XmlClauses = xmlClauses;
	}

	public ArrayList getUsedSeeds()
	{
		return UsedSeeds;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	public String getParamsList()
	{
		StringBuffer sb = new StringBuffer(200);
		
		for (int i = 0; i < Params.size(); i++)
		{
			Param p = (Param)Params.get(i);
			
			if(i > 0)
					sb.append(", ");
			
			if(p.getIndex() == -1)
			{
				if(p.getFieldType().equalsIgnoreCase("java.lang.Object"))
					sb.append("java.lang.Integer _" + p.getQuery());
				else
					sb.append(p.getFieldType() + " _" + p.getQuery());
			}
			else
			{
				if(p.getFieldType().equalsIgnoreCase("java.lang.Object"))
					sb.append("java.lang.Integer _" + p.getFieldAltered());
				else
					sb.append(p.getFieldType() + " _" + p.getFieldAltered());
			}
		}
		
		return sb.toString();
	}
	
	public String getParamsListNames()
	{
		StringBuffer sb = new StringBuffer(200);
		
		for (int i = 0; i < Params.size(); i++)
		{
			Param p = (Param)Params.get(i);
			
			if(i > 0)
					sb.append(", ");
			
			if(p.getIndex() == -1)
				sb.append(p.getQuery());
			else
			{
				int qi = getQueryIndex(p.getQuery());
				
				sb.append("q" + qi + ".getFieldValueByIndex(" + p.getIndex() + ")");
			}
		}
		
		return sb.toString();
	}

	public String getParamNameByIndex(int index)
	{
		StringBuffer sb = new StringBuffer(200);
		
		for (int i = 0; i < Params.size(); i++)
		{
			if(i == index)
			{
				Param p = (Param)Params.get(i);
				
				if(p.getIndex() == -1)
					sb.append(p.getQuery());
				else
				{
					int qi = getQueryIndex(p.getQuery());
					
					sb.append("q" + qi + ".getFieldValueByIndex(" + p.getIndex() + ")");
				}
				
				break;
			}
		}
		
		return sb.toString();
	}
	private int getQueryIndex(String query)
	{
		for (int i = 0; i < QueryNames.size(); i++)
		{
			if(query.equalsIgnoreCase((String)QueryNames.get(i)))
				return i;
		}
		
		return -1;
	}

	public void setNative(boolean b)
	{
		this.nat = b;
	}

	public boolean isNative()
	{
		return this.nat;
	}
}
