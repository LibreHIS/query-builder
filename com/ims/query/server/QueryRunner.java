/*
 * Created on 22-Sep-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.DefaultElement;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import com.ims.query.builder.ClauseParser;
import com.ims.query.builder.DataSource;
import com.ims.query.builder.DataSource2XML;
import com.ims.query.builder.FieldHolder;
import com.ims.query.builder.ImsHQLQuery;
import com.ims.query.builder.SeedHolder;

public class QueryRunner extends HttpServlet
{
	protected DateFormat qbDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	protected DateFormat qbTimeFormat = new SimpleDateFormat("HH:mm");
	protected DateFormat qbDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		HttpSession httpSession = request.getSession(true);

		parseRequestAttributes(request, httpSession);

		String query = (String) httpSession.getAttribute("query");
		
		if(query != null && query.length() > 0)
		{
			try
			{
				parseQueryXml(query, httpSession);
			}
			catch (DocumentException e)
			{
				removeSessionAttributes(httpSession);
				
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getStackTrace(e));
				return;
			}
			catch (ParseException e)
			{
				removeSessionAttributes(httpSession);
	
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getStackTrace(e));
				return;
			}
	
			String dsXml = "";
			Map seeds = (Map) httpSession.getAttribute("seeds");
			String hql = (String)httpSession.getAttribute("hql");
			ArrayList fields = (ArrayList)httpSession.getAttribute("fields");
			String startIndex = (String)httpSession.getAttribute("nStartIndex");
			String endIndex = (String)httpSession.getAttribute("nEndIndex");
			String xmlClauses = (String)httpSession.getAttribute("XmlClauses");
			String limit = (String)httpSession.getAttribute("LIMIT");
			
			if(hql.length() == 0)
			{
				removeSessionAttributes(httpSession);
	
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The HQL query is null !");
				return;
			}
			if(fields.size() == 0)
			{
				removeSessionAttributes(httpSession);
	
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The HQL query fields array is null !");
				return;
			}
			
			try
			{
				//dsXml = runHQLQuery(seeds, hql, fields);
				dsXml = runHQLQueryToCSV(seeds, hql, fields, startIndex, endIndex, xmlClauses, limit);
				
				//System.out.println(dsXml);
			}
			catch (Exception e)
			{
				removeSessionAttributes(httpSession);
	
				httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, getStackTrace(e));
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getStackTrace(e));
				return;
			}
			
			response.setContentType("text/xml");

			response.getOutputStream().write(dsXml.getBytes());
			response.getOutputStream().flush();
		}
		else
		{
			query = (String) httpSession.getAttribute("nativequery");
			
			if(query != null && query.length() > 0)
			{
				try
				{
					parseQueryXml(query, httpSession);
				}
				catch (DocumentException e)
				{
					removeSessionAttributes(httpSession);
					
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getStackTrace(e));
					return;
				}
				catch (ParseException e)
				{
					removeSessionAttributes(httpSession);
		
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getStackTrace(e));
					return;
				}
		
				String dsXml = "";
				Map seeds = (Map) httpSession.getAttribute("seeds");
				String hql = (String)httpSession.getAttribute("hql");
				ArrayList fields = (ArrayList)httpSession.getAttribute("fields");
				String startIndex = (String)httpSession.getAttribute("nStartIndex");
				String endIndex = (String)httpSession.getAttribute("nEndIndex");
				String xmlClauses = (String)httpSession.getAttribute("XmlClauses");
				String limit = (String)httpSession.getAttribute("LIMIT");
				
				if(hql.length() == 0)
				{
					removeSessionAttributes(httpSession);
		
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The HQL query is null !");
					return;
				}
				/*
				if(fields.size() == 0)
				{
					removeSessionAttributes(httpSession);
		
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The HQL query fields array is null !");
					return;
				}
				*/
				
				try
				{
					dsXml = runNativeQueryToCSV(seeds, hql, fields, startIndex, endIndex, xmlClauses, limit);
				}
				catch (HibernateException e)
				{
					e.printStackTrace();
					removeSessionAttributes(httpSession);
					
					if(httpSession != null)
					{
						httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, getStackTrace(e));
						//httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, e.toString());
					}
		
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getStackTrace(e));
					return;
				}

				response.setContentType("text/xml");

				response.getOutputStream().write(dsXml.getBytes());
				response.getOutputStream().flush();
			}
			else
			{
				query = (String) httpSession.getAttribute("metadata");
				String paramCount = (String) httpSession.getAttribute("paramCount");
				
				if(query != null && query.length() > 0)
				{
					String metadata = null;
					
					try
					{
						metadata = getSQLMetadata(query, paramCount);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						removeSessionAttributes(httpSession);
						
						if(httpSession != null)
						{
							httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, getStackTrace(e));
							//httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, e.toString());
						}
			
						response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getStackTrace(e));
						return;
					}
					
					response.setContentType("text/xml");

					response.getOutputStream().write(metadata.getBytes());
					response.getOutputStream().flush();
				}
			}
		}
		
		removeSessionAttributes(httpSession);
	}

	private String getSQLMetadata(String query, String paramCount) throws SQLException
	{
		StringBuffer metadata = new StringBuffer();
		String type;
		
		//metadata.append("<?xml version=\"1.0\"?>");
		metadata.append("<fields>");
		
		Session session = HibernateUtil.currentSession();
		
		Connection conn = session.connection();
		PreparedStatement pst = null;
		
		int params = Integer.valueOf(paramCount).intValue();
		
		try
		{
			pst = conn.prepareStatement(query);
			
			for (int i = 0; i < params; i++)
			{
				pst.setObject(i+1, null);	
			}
			
			ResultSet rs = pst.executeQuery();

		    ResultSetMetaData rsMetaData = rs.getMetaData();
		    
		    int numberOfColumns = rsMetaData.getColumnCount();

		    for (int i = 1; i <= numberOfColumns; i++)
			{
				metadata.append("<field>");
					metadata.append("<name>");
					metadata.append(StringUtils.encodeXML(rsMetaData.getColumnName(i)));
					metadata.append("</name>");

					type = rsMetaData.getColumnClassName(i);
					
					if(type.equalsIgnoreCase("java.sql.Timestamp"))
						type = "java.util.Date";
					else if(type.equalsIgnoreCase("java.math.BigDecimal") && rsMetaData.getScale(i) == 0)
						type = "java.lang.Integer";
					else if(type.equalsIgnoreCase("java.math.BigDecimal") && rsMetaData.getScale(i) > 0)
						type = "java.lang.Float";
					else if(type.equalsIgnoreCase("oracle.sql.TIMESTAMP"))
						type = "java.util.Date";

					metadata.append("<type>");
					metadata.append(StringUtils.encodeXML(type));
					metadata.append("</type>");
				metadata.append("</field>");
				
				//System.out.println("");
				//System.out.println("column MetaData ");
				//System.out.println("column number " + i);
				// indicates the designated column's normal maximum width in
				// characters
				//System.out.println(rsMetaData.getColumnDisplaySize(i));
				// gets the designated column's suggested title
				// for use in printouts and displays.
				//System.out.println(rsMetaData.getColumnLabel(i));
				// get the designated column's name.
				//System.out.println(rsMetaData.getColumnName(i));

				// get the designated column's SQL type.
				//System.out.println(rsMetaData.getColumnType(i));

				// get the designated column's SQL type name.
				//System.out.println(rsMetaData.getColumnTypeName(i));

				// get the designated column's class name.
				//System.out.println(rsMetaData.getColumnClassName(i));

				// get the designated column's table name.
				//System.out.println(rsMetaData.getTableName(i));

				// get the designated column's number of decimal digits.
				//System.out.println(rsMetaData.getPrecision(i));

				// gets the designated column's number of
				// digits to right of the decimal point.
				//System.out.println(rsMetaData.getScale(i));

				// indicates whether the designated column is
				// automatically numbered, thus read-only.
				//System.out.println(rsMetaData.isAutoIncrement(i));

				// indicates whether the designated column is a cash value.
				//System.out.println(rsMetaData.isCurrency(i));

				// indicates whether a write on the designated
				// column will succeed.
				//System.out.println(rsMetaData.isWritable(i));

				// indicates whether a write on the designated
				// column will definitely succeed.
				//System.out.println(rsMetaData.isDefinitelyWritable(i));

				// indicates the nullability of values
				// in the designated column.
				//System.out.println(rsMetaData.isNullable(i));

				// Indicates whether the designated column
				// is definitely not writable.
				//System.out.println(rsMetaData.isReadOnly(i));

				// Indicates whether a column's case matters
				// in the designated column.
				//System.out.println(rsMetaData.isCaseSensitive(i));

				// Indicates whether a column's case matters
				// in the designated column.
				//System.out.println(rsMetaData.isSearchable(i));

				// indicates whether values in the designated
				// column are signed numbers.
				//System.out.println(rsMetaData.isSigned(i));

				// Gets the designated column's table's catalog name.
				//System.out.println(rsMetaData.getCatalogName(i));

				// Gets the designated column's table's schema name.
				//System.out.println(rsMetaData.getSchemaName(i));
			}
		}
		catch (HibernateException e)
		{
			throw e;
		}
		catch (SQLException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				pst.close();
				conn.close();
				HibernateUtil.closeSession();
			}
			catch (HibernateException e2)
			{
				e2.printStackTrace();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}

		metadata.append("</fields>");
		
		return metadata.toString();
	}

	@SuppressWarnings("unchecked")
	private String runHQLQuery(Map seeds, String hql, ArrayList fields) throws HibernateException
	{
		Transaction tx = null;
		Iterator it = null;
		Session session = null;

		session = HibernateUtil.currentSession();
		
		tx = session.beginTransaction();
		try
		{
			Query q = session.createQuery(hql);
			Iterator keys = seeds.keySet().iterator();
			String key;
			while (keys.hasNext())
			{
				key = (String) keys.next();
				
				q.setParameter(key, ((SeedHolder)seeds.get(key)).getValue(), getParamType(((SeedHolder)seeds.get(key)).getType()));
			}
	
			it = q.list().iterator();
			tx.commit();
		}
		catch(HibernateException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			}
			catch (HibernateException e2)
			{
			}
		}

		QueryInternal qi = new QueryInternal();
		if(it.hasNext() == false)
		{
			qi.setHasFields(false);
			
			for(int i = 0; i < fields.size(); i++)
			{
				qi.addFieldValue(((FieldHolder)fields.get(i)).getName(), "", ((FieldHolder)fields.get(i)).getType());
			}
		}
		else
		{
			while ( it.hasNext() ) 
			{
				//Object[] row = (Object[]) it.next();
				Object[] row = makeObjectArray(it.next());
				
				for(int i = 0; i < row.length; i++)
				{
					if(row[i] instanceof java.sql.Date)
					{
						qi.addFieldValue(((FieldHolder)fields.get(i)).getName(),(row[i] == null ? "" : qbDateFormat.format(row[i])), ((FieldHolder)fields.get(i)).getType());	
					}
					else if(row[i] instanceof java.sql.Time)
					{
						qi.addFieldValue(((FieldHolder)fields.get(i)).getName(),(row[i] == null ? "" : qbTimeFormat.format(row[i])), ((FieldHolder)fields.get(i)).getType());	
					}
					else if(row[i] instanceof java.util.Date)
					{
						qi.addFieldValue(((FieldHolder)fields.get(i)).getName(),(row[i] == null ? "" : qbDateTimeFormat.format(row[i])), ((FieldHolder)fields.get(i)).getType());	
					}
					else 
					{
						qi.addFieldValue(((FieldHolder)fields.get(i)).getName(),(row[i] == null ? "" : row[i].toString()), ((FieldHolder)fields.get(i)).getType());
					}
				}
			}
		}
		
		DataSource ds = new DataSource("QUERYRUNNER", qi.getFields(), qi.hasFields());
		ArrayList al = new ArrayList();
		al.add(ds);
		DataSource2XML ds2xml = new DataSource2XML(al);
		
		return ds2xml.toXml();
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

	
	private void removeSessionAttributes(HttpSession httpSession)
	{
		httpSession.removeAttribute("query");
		httpSession.removeAttribute("seeds");
		httpSession.removeAttribute("fields");
		httpSession.removeAttribute("hql");
		httpSession.removeAttribute("nStartIndex");
		httpSession.removeAttribute("nEndIndex");
		httpSession.removeAttribute("XmlClauses");
		httpSession.removeAttribute("LIMIT");
		
		httpSession.removeAttribute("nativequery");
		httpSession.removeAttribute("metadata");
		httpSession.removeAttribute("paramCount");
	}

	@SuppressWarnings("unchecked")
	private void parseQueryXml(String xmlQuery, HttpSession httpSession)
			throws DocumentException, ParseException
	{
		String name;
		String value;
		String type;

		Map seeds = new HashMap();

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat tf = new SimpleDateFormat("HH:mm");
		DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		if (xmlQuery == null)
			return;

		Document maindoc = getXmlDocument(xmlQuery);

		//parse seeds
		List list = maindoc.selectNodes("query/seeds/seed");

		Object obj;
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement seed = (DefaultElement) iter.next();

			name = seed.valueOf("name");
			value = seed.valueOf("value");
			type = seed.valueOf("type");

			obj = null;
			if (type.equalsIgnoreCase("java.lang.Integer"))
			{
				if (value.length() > 0)
					obj = Integer.valueOf(value);
			}
			else if (type.equalsIgnoreCase("java.math.BigInteger"))
			{
				if (value.length() > 0)
					obj = new BigInteger(value);
			}
			else if (type.equalsIgnoreCase("java.lang.Long"))
			{
				if (value.length() > 0)
					obj = new Long(value);
			}
			else if (type.equalsIgnoreCase("java.lang.Short"))
			{
				if (value.length() > 0)
					obj = new Short(value);
			}
			else if (type.equalsIgnoreCase("java.lang.Boolean"))
			{
				if (value.length() > 0)
					obj = new Boolean(value);
			}
			else if (type.equalsIgnoreCase("java.lang.String"))
			{
				if (value.length() > 0)
					obj = new String(value);
			}
			else if (type.equalsIgnoreCase("java.math.BigDecimal"))
			{
				if (value.length() > 0)
					obj = new BigDecimal(value);
			}
			else if (type.equalsIgnoreCase("java.lang.Float"))
			{
				if (value.length() > 0)
					obj = new Float(value);
			}
			else if (type.equalsIgnoreCase("java.lang.Double"))
			{
				if (value.length() > 0)
					obj = new Double(value);
			}
			else if (type.equalsIgnoreCase("java.sql.Date"))
			{
				if (value.length() > 0)
					obj = df.parse(value);
			}
			else if (type.equalsIgnoreCase("java.sql.Time"))
			{
				if (value.length() > 0)
					obj = tf.parse(value);
			}
			else if (type.equalsIgnoreCase("java.util.Date"))
			{
				if (value.length() > 0)
					obj = dtf.parse(value);
			}
			else
			{
				if (value.length() > 0)
					obj = Integer.valueOf(value);
			}

			seeds.put(name, new SeedHolder(name, type, obj));
		}
		httpSession.setAttribute("seeds", seeds);
		
		//parse fields
		list = maindoc.selectNodes("query/fields/field");
		ArrayList fields = new ArrayList();
		
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement seed = (DefaultElement) iter.next();

			name = seed.valueOf("name");
			type = seed.valueOf("type");
			
			fields.add(new FieldHolder(name, type));
		}
		httpSession.setAttribute("fields", fields);

		//parse hql
		list = maindoc.selectNodes("query/hql");
		String hql = "";
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement seed = (DefaultElement) iter.next();

			hql = seed.getStringValue();
		}
		httpSession.setAttribute("hql", hql);
		
		//parse WhereClauseIndexes
		list = maindoc.selectNodes("query/WhereClauseIndexes");
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement seed = (DefaultElement) iter.next();

			httpSession.setAttribute("nStartIndex", seed.attribute("nStartIndex").getValue());
			httpSession.setAttribute("nEndIndex", seed.attribute("nEndIndex").getValue());
			break;
		}

		//parse XmlClauses
		list = maindoc.selectNodes("query/XmlClauses");
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement seed = (DefaultElement) iter.next();

			httpSession.setAttribute("XmlClauses", seed.getStringValue());
			break;
		}

		//parse LIMIT
		list = maindoc.selectNodes("query/LIMIT");
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement seed = (DefaultElement) iter.next();

			httpSession.setAttribute("LIMIT", seed.getStringValue());
			break;
		}
		
	}

	private static Document getXmlDocument(String xmlBuffer)
			throws DocumentException
	{
		return DocumentHelper.parseText(xmlBuffer);
	}

	private void parseRequestAttributes(HttpServletRequest request,
			HttpSession httpSession) throws IOException
	{
		String[] val = request.getParameterValues("query");
		if(val != null)
		{
			httpSession.setAttribute("query", val[0]);
		}
		else
			httpSession.setAttribute("query", "");
		
		val = request.getParameterValues("nativequery");
		if(val != null)
		{
			httpSession.setAttribute("nativequery", val[0]);
		}
		else
			httpSession.setAttribute("nativequery", "");

		val = request.getParameterValues("metadata");
		if(val != null)
		{
			httpSession.setAttribute("metadata", val[0]);
		}
		else
			httpSession.setAttribute("metadata", "");

		val = request.getParameterValues("paramCount");
		if(val != null)
		{
			httpSession.setAttribute("paramCount", val[0]);
		}
		else
			httpSession.setAttribute("paramCount", "");
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		doGet(request, response);
	}

	private String read(HttpServletRequest request) throws IOException
	{
		ServletInputStream inputStream = request.getInputStream();
		StringBuffer sb = new StringBuffer(1024 * 64);
		int c;
		while ((c = inputStream.read()) != -1)
		{
			sb.append((char) c);
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	private String runHQLQueryToCSV(Map seeds, String hql, ArrayList fields, String startIndex, String endIndex, String xmlClauses, String limit) throws HibernateException
	{
		Transaction tx = null;
		Iterator it = null;
		Session session = null;
		ArrayList alUsedSeeds = null;
		
		String newHql = hql;
		
		if(startIndex != null && endIndex != null && xmlClauses != null && startIndex.length() > 0 && endIndex.length() > 0 && xmlClauses.length() > 0)
		{
			int nStartIndex = Integer.valueOf(startIndex).intValue();
			int nEndIndex = Integer.valueOf(endIndex).intValue();
			
			StringBuffer sb = new StringBuffer(hql.length());
			
			sb.append(hql.substring(0, nStartIndex - 1));
			
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
			if(nEndIndex < hql.length())
				sb.append(hql.substring(nEndIndex));
			
			newHql = sb.toString();
			alUsedSeeds = parser.ExtractNamedParameters(newHql);
			
			System.out.println(newHql);
		}
		
		CsvUtils csv = new CsvUtils();

		session = HibernateUtil.currentSession();

		tx = session.beginTransaction();
		try
		{
			Query q = session.createQuery(newHql);
			Iterator keys = seeds.keySet().iterator();
			String key;
			while (keys.hasNext())
			{
				key = (String) keys.next();

				if(alUsedSeeds == null || alUsedSeeds.contains(key))
					q.setParameter(key, ((SeedHolder) seeds.get(key)).getValue(), getParamType(((SeedHolder) seeds.get(key)).getType()));
			}
			
			if(limit != null && limit.length() > 0 && !limit.equals("0"))
				q.setMaxResults(Integer.valueOf(limit).intValue());

			it = q.list().iterator();
			tx.commit();
		}
		catch (HibernateException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			}
			catch (HibernateException e2)
			{
			}
		}
		
		String val = "";
		int[] maxFieldSize = new int[fields.size()];
		
		for(int i = 0; i < maxFieldSize.length; i++)
			maxFieldSize[i] = 1;
		
		String fieldValues = "";
		StringBuffer sbHeader = new StringBuffer(1024);
		StringBuffer sbData = new StringBuffer(1024);
		String str = "";
		String fieldList = "";
		
		int index = 1;
		while (it.hasNext())
		{
			Object[] row = makeObjectArray(it.next());
			fieldValues = "";
			
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
				
				if(fieldValues.length() > 0)
					fieldValues += ",";
				
				if(val.length() > 0)
					fieldValues += csv.ansiQuotedStr(csv.stringToCodedString(val), '"');
				else if(i == 0)
				{
					fieldValues += "\"\"";
				}
				
			}

			fieldValues = "\"" + String.valueOf(index) + "\"," + fieldValues;
			sbData.append(fieldValues);
			sbData.append('\r');
			sbData.append('\n');
			
			index++;
		}

		sbHeader.append("\"@@IMSDATASOURCENAME@@\",\"QUERYRUNNER\"");
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append( csv.ansiQuotedStr("@@FILE VERSION@@", '"'));
		sbHeader.append(',');
		sbHeader.append( csv.ansiQuotedStr("251", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append( csv.ansiQuotedStr("@@TABLEDEF START@@", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');

		sbHeader.append( csv.ansiQuotedStr("RecNo=Integer,0,\"RecNo\",\"\",16,Data,\"\"", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');
		
		fieldList = "\"RecNo\"";
		for (int i = 0; i < fields.size(); i++)
		{
			String paramTypeForDelphi = csv.getParamTypeForDelphi(((FieldHolder) fields.get(i)).getType());
			
			str = ((FieldHolder) fields.get(i)).getName() + "=" + paramTypeForDelphi + 
			"," + (paramTypeForDelphi.equalsIgnoreCase("String") ? String.valueOf(maxFieldSize[i]) : "0") + 
			",\"" + ((FieldHolder) fields.get(i)).getName() + "\"" + 
			"," + "\"\"" +
			"," + "110" +
			"," + "Data" +
			"," + "\"\"";
			
			sbHeader.append(csv.ansiQuotedStr(str, '"'));
			sbHeader.append('\r');
			sbHeader.append('\n');
			
			if(fieldList.length() > 0)
				fieldList += ",";
			
			fieldList += "\"" + ((FieldHolder) fields.get(i)).getName() + "\"";
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

		sbHeader.append(sbData);
		
		return sbHeader.toString();
	}

	@SuppressWarnings("unchecked")
	private String runNativeQueryToCSV(Map seeds, String hql, ArrayList fields, String startIndex, String endIndex, String xmlClauses, String limit) throws HibernateException
	{
		Transaction tx = null;
		Iterator it = null;
		Session session = null;
		ArrayList alUsedSeeds = null;
		
		String newHql = hql;
		
		/*
		if(startIndex != null && endIndex != null && xmlClauses != null && startIndex.length() > 0 && endIndex.length() > 0 && xmlClauses.length() > 0)
		{
			int nStartIndex = Integer.valueOf(startIndex).intValue();
			int nEndIndex = Integer.valueOf(endIndex).intValue();
			
			StringBuffer sb = new StringBuffer(hql.length());
			
			sb.append(hql.substring(0, nStartIndex - 1));
			
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
			if(nEndIndex < hql.length())
				sb.append(hql.substring(nEndIndex));
			
			newHql = sb.toString();
			alUsedSeeds = parser.ExtractNamedParameters(newHql);
			
			System.out.println(newHql);
		}
		*/
		
		CsvUtils csv = new CsvUtils();

		session = HibernateUtil.currentSession();
		
		tx = session.beginTransaction();
		try
		{
			SQLQuery q = session.createSQLQuery(newHql);
			Iterator keys = seeds.keySet().iterator();
			String key;
			while (keys.hasNext())
			{
				key = (String) keys.next();

				if(alUsedSeeds == null || alUsedSeeds.contains(key))
					q.setParameter(key, ((SeedHolder) seeds.get(key)).getValue(), getParamType(((SeedHolder) seeds.get(key)).getType()));
			}
			
			if(limit != null && limit.length() > 0 && !limit.equals("0"))
				q.setMaxResults(Integer.valueOf(limit).intValue());

			it = q.list().iterator();
			tx.commit();
		}
		catch (HibernateException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			}
			catch (HibernateException e2)
			{
			}
		}
		
		String val = "";
		int[] maxFieldSize = new int[fields.size()];
		
		for(int i = 0; i < maxFieldSize.length; i++)
			maxFieldSize[i] = 1;
		
		String fieldValues = "";
		StringBuffer sbHeader = new StringBuffer(1024);
		StringBuffer sbData = new StringBuffer(1024);
		String str = "";
		String fieldList = "";
		
		int index = 1;
		while (it.hasNext())
		{
			Object[] row = makeObjectArray(it.next());
			fieldValues = "";
			
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

			fieldValues = "\"" + String.valueOf(index) + "\"," + fieldValues;
			sbData.append(fieldValues);
			sbData.append('\r');
			sbData.append('\n');
			
			index++;
		}

		sbHeader.append("\"@@IMSDATASOURCENAME@@\",\"QUERYRUNNER\"");
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append( csv.ansiQuotedStr("@@FILE VERSION@@", '"'));
		sbHeader.append(',');
		sbHeader.append( csv.ansiQuotedStr("251", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');
		sbHeader.append( csv.ansiQuotedStr("@@TABLEDEF START@@", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');

		sbHeader.append( csv.ansiQuotedStr("RecNo=Integer,0,\"RecNo\",\"\",16,Data,\"\"", '"'));
		sbHeader.append('\r');
		sbHeader.append('\n');
		
		fieldList = "\"RecNo\"";
		for (int i = 0; i < fields.size(); i++)
		{
			String paramTypeForDelphi = csv.getParamTypeForDelphi(((FieldHolder) fields.get(i)).getType());
			
			str = ((FieldHolder) fields.get(i)).getName() + "=" + paramTypeForDelphi + 
			"," + (paramTypeForDelphi.equalsIgnoreCase("String") ? String.valueOf(maxFieldSize[i]) : "0") + 
			",\"" + ((FieldHolder) fields.get(i)).getName() + "\"" + 
			"," + "\"\"" +
			"," + "110" +
			"," + "Data" +
			"," + "\"\"";
			
			sbHeader.append(csv.ansiQuotedStr(str, '"'));
			sbHeader.append('\r');
			sbHeader.append('\n');
			
			if(fieldList.length() > 0)
				fieldList += ",";
			
			fieldList += "\"" + ((FieldHolder) fields.get(i)).getName() + "\"";
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

		sbHeader.append(sbData);
		
		return sbHeader.toString();
	}
	
	private Object[] makeObjectArray(Object obj)
	{
		if(obj instanceof Object[])
			return (Object[])obj;
		
		Object[] res = new Object[1];
		
		res[0] = obj;
		
		return res;
	}
	
	private String getStackTrace(Throwable aThrowable)
	{
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}
	
	
	public class QueryInternal extends ImsHQLQuery
	{
		public void ExecuteQuery() throws HibernateException
		{
		}
	}
}
