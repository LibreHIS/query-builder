/*
 * Created on 02-Dec-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.DefaultElement;


public class ClauseParser
{
	private HashMap seeds;
	private String xmlClause;
	private Clause rootClause;
	private ArrayList clauses;
	private Stack operators;
	
	public ClauseParser()
	{
		this.seeds = new HashMap();
		this.clauses = new ArrayList();
		this.operators = new Stack();
	}
	
	public void setSeed(String key, SeedHolder seed)
	{
		seeds.put(key, seed);
	}
	public HashMap getSeeds()
	{
		return seeds;
	}

	public String getXmlClause()
	{
		return xmlClause;
	}

	public void setXmlClause(String xmlClause)
	{
		this.xmlClause = xmlClause;
	}
	
	public String buildHql()
	{
		parseXml();
		
		if(rootClause == null)
			return "";
		
		String where = "where ";
		
		
		String hql = parse(rootClause);
		
		if(hql.length() == 0)
			return "";
		
		StringBuffer sb = new StringBuffer();
		sb.append(where);
		sb.append("(");
		sb.append(hql);
		sb.append(")");
		
		return sb.toString();
	}

	public ArrayList ExtractNamedParameters(String hql)
	{
		ArrayList vRet = new ArrayList();
	    String param = "";
	    
	    int nSingleQuotes = 0;
	    int nLen = hql.length();
	    boolean bBeginParam = false;

	    for(int i = 0; i < nLen; i++)
	    {
	        if(hql.charAt(i) == '\'')
	            nSingleQuotes++;
	        else
	        if(hql.charAt(i) == ':')
	        {
	            if(nSingleQuotes % 2 == 0)
	            {
	                param = "";
	                bBeginParam = true;
	            }
	        }
	        else
	        if(hql.charAt(i) == '(' || hql.charAt(i) == ')' || hql.charAt(i) == ' ' || hql.charAt(i) == '\r' || hql.charAt(i) == '\n' || hql.charAt(i) == ',')
	        {
	            if(bBeginParam == true)
	            {
	                if(vRet.contains(param) == false)
	                    vRet.add(param);
	                param = "";
	                bBeginParam = false;
	            }
	        }
	        else
	        {
	            param += hql.charAt(i);
	        }
	    }

	    if(bBeginParam == true && param != "")
	    {
            if(vRet.contains(param) == false)
                vRet.add(param);
	    }

	    return vRet;
	}
	
	private String parse(Clause clause)
	{
		if(clause.getClause() == Clause.NO_CLAUSE)
		{
			if(clause.getValidator() == null || clause.getValidator().length() == 0)
				return clause.getHql();
			else
			{
				SeedHolder seed = (SeedHolder)seeds.get(clause.getValidator());
				
				if(seed == null || seed.getValue() == null)
					return "";
				
				return clause.getHql();
			}
		}

		String extraOp = "";
		if(clause.getClause() == Clause.CLAUSE_ALL)
		{
			operators.push("and");
		}
		else if(clause.getClause() == Clause.CLAUSE_ANY)
		{
			operators.push("or");
		}
		else if(clause.getClause() == Clause.CLAUSE_NONE)
		{
			operators.push("or");
			extraOp = " not ";
		}
		else if(clause.getClause() == Clause.CLAUSE_NOT_ALL)
		{
			operators.push("and");
			extraOp = " not ";
		}
		
		StringBuffer sb = new StringBuffer();
		String operator = (String)operators.lastElement();
		if(operator == null)
			operator = "";
		
		for(int i = 0; i < clause.getChildren().size(); i++)
		{
			String hql = parse(clause.getChildrenAt(i));

			if(sb.length() > 0 && operator.length() > 0 && hql.length() > 0)
			{
				sb.append(' ');
				sb.append(operator);
				sb.append(' ');
			}
			
			sb.append(hql);
		}
		
		operators.pop();
		
		if(sb.length() > 0)
		{
			sb.insert(0, " (");
			if(extraOp.length() > 0)
			{
				sb.insert(0, extraOp);
			}
			sb.append(")");
		}
		
		return sb.toString();
	}

	private void parseXml()
	{
		int parentId = 0;
		
		try
		{
			Document doc = DocumentHelper.parseText(xmlClause);
			
			List list = doc.selectNodes("clauses/clause[*]");
			
			for (Iterator iter = list.iterator(); iter.hasNext();)
			{
				DefaultElement elem = (DefaultElement) iter.next();

				Clause clause = new Clause();
				
				parentId = Integer.valueOf(elem.elementText("parent")).intValue();
				clause.setId(Integer.valueOf(elem.elementText("id")).intValue());
				clause.setClause(Integer.valueOf(elem.elementText("clause_type")).intValue());
				clause.setHql(elem.elementText("hql"));
				clause.setValidator(elem.elementText("validator"));
				
				if(rootClause == null && clause.getId() == 0)
					rootClause = clause;
				
				if(clause.getId() > 0)
				{
					Clause parent = getClauseById(parentId);
					
					parent.addChildren(clause);
				}
				
				clauses.add(clause);
			}			
		}
		catch (DocumentException e)
		{
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		
	}

	private Clause getClauseById(int parentId)
	{
		int nSize = clauses.size();
		
		for(int i = 0; i < nSize; i++)
		{
			if(((Clause)clauses.get(i)).getId() == parentId)
				return (Clause)clauses.get(i);
		}	
		
		return null;
	}
}
