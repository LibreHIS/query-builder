/*
 * Created on Jan 24, 2005
 *
 */
package com.ims.query.builder.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

import com.ims.query.server.StringUtils;

/**
 * @author vpurdila
 *
 */
public class QueryBuilderParser
{
	private ArrayList hibQueries;
	
	public QueryBuilderParser()
	{
		super();
		hibQueries = new ArrayList();
	}
	
	public ArrayList getHibQuery()
	{
		return hibQueries;
	}
	public void setHibQuery(ArrayList hibQueries)
	{
		this.hibQueries = hibQueries;
	}
	
	public boolean parseQueryBuilderProject(String xmlBuffer) throws DocumentException
	{
		Document maindoc = getXmlDocument(xmlBuffer);

		List list = maindoc.selectNodes("/Project/Queries/HQL");
		
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement query = (DefaultElement) iter.next();

			Node node = query.selectSingleNode("Content");
			String content = node.getStringValue();
			
			Document doc = getXmlDocument(content);
			
			ImsHibernateQuery hibQuery = new ImsHibernateQuery();
			
			node = doc.selectSingleNode("Query/Name");
			hibQuery.setName(node.getStringValue());
			node = doc.selectSingleNode("Query/Description");
			hibQuery.setDescription(node.getStringValue());
			
			List listSelectedFields = doc.selectNodes("/Query/SelectedFields/Field");
			for (Iterator iter_fields = listSelectedFields.iterator(); iter_fields.hasNext();)
			{
				DefaultElement field = (DefaultElement) iter_fields.next();
				
				SelectedField sf = new SelectedField();
				sf.setBusinessObject(field.valueOf("@BO"));
				sf.setFieldName(field.valueOf("@asField"));
				sf.setFieldType(field.valueOf("@nFieldType"));
				sf.setFieldSize(Integer.valueOf(field.valueOf("@nFieldSize")).intValue());
				sf.setAgregate(Integer.valueOf(field.valueOf("@nAgregate")).intValue());

				hibQuery.getSelectedFields().add(sf);
			}
			
			List listParams = doc.selectNodes("/Query/Params/Param");
			for (Iterator iter_params = listParams.iterator(); iter_params.hasNext();)
			{
				DefaultElement field = (DefaultElement) iter_params.next();
				
				Param param = new Param();
				param.setQuery(field.valueOf("@Query"));
				param.setIndex(Integer.valueOf(field.valueOf("@Index")).intValue());
				param.setFieldType(field.valueOf("@nFieldType"));
				param.setField(field.valueOf("@asField"));
				
				hibQuery.getParams().add(param);
			}
			
			node = doc.selectSingleNode("Query/HibernateQuery");
			hibQuery.setHqlString(StringUtils.replaceSeparators(node.getStringValue()));
			
			node = doc.selectSingleNode("Query/LIMIT");
			if(node != null && node.getStringValue() != null && node.getStringValue().length() > 0)
			{
				hibQuery.setLimit(Integer.valueOf(node.getStringValue()).intValue());
			}
			
			node = doc.selectSingleNode("Query/Native");
			if(node != null && node.getStringValue() != null && node.getStringValue().length() > 0)
			{
				hibQuery.setNative(node.getStringValue().equalsIgnoreCase("true") ? true : false);
			}
			
			//parse WhereClauseIndexes
			node = doc.selectSingleNode("Query/WhereClauseIndexes");
			if(node != null)
			{
				String value = node.valueOf("@nStartIndex");
				if(value != null && value.length() > 0)
					hibQuery.setStartIndex(Integer.valueOf(value).intValue());
				else
					hibQuery.setStartIndex(-1);
				value = node.valueOf("@nEndIndex");
				if(value != null && value.length() > 0)
					hibQuery.setEndIndex(Integer.valueOf(value).intValue());
				else
					hibQuery.setEndIndex(-1);
			}
			else
			{
				hibQuery.setStartIndex(-1);
				hibQuery.setEndIndex(-1);
			}

			//parse XmlClauses
			node = doc.selectSingleNode("Query/XmlClauses");
			if(node != null)
				hibQuery.setXmlClauses(node.getStringValue());
			else
				hibQuery.setXmlClauses("");
			
			hibQueries.add(hibQuery);
		}
		
		ArrayList al = new ArrayList();
		for (int i = 0; i < hibQueries.size(); i++)
		{
			ImsHibernateQuery q = (ImsHibernateQuery)hibQueries.get(i);
			al.add(q.getName());
		}
		for (int i = 0; i < hibQueries.size(); i++)
		{
			ImsHibernateQuery q = (ImsHibernateQuery)hibQueries.get(i);
			q.setQueryNames(al);
			hibQueries.set(i, q);
		}
		return true;
	}
	
	/*
	private static Document getXmlDocument(Class clazz, String file) throws DocumentException
	{
		InputStream stream = clazz.getResourceAsStream(file);
		
		SAXReader reader = new SAXReader();
		Document document = reader.read(stream);
		return document;
	}
	*/

	private static Document getXmlDocument(String xmlBuffer) throws DocumentException
	{
		return DocumentHelper.parseText(xmlBuffer);
	}
	
}
