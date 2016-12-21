/*
 * Created on 05-May-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.DefaultElement;
import com.ims.query.builder.QueryBuilderEngine;
import com.ims.query.builder.SeedHolder;
import com.ims.query.builder.exceptions.QueryBuilderException;
import com.ims.report.client.HttpReportClient;
import com.ims.report.client.exceptions.HttpReportClientException;

/**
 * @author vpurdila
 *
 */
public class PrintReport extends HttpServlet
{
	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException
	{
		//boolean bReturnUrl = false;
		
		HttpSession httpSession = request.getSession(true);
		
		ParseRequestAttributes(request, httpSession);
		
		Map attributes = (Map)httpSession.getAttribute("attributes");
		
		if(attributes.get("project") == null)
		{
			attributes = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);
			
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The QueryBuilder project content was not passed down !");
			return;
		}

		if(attributes.get("template") == null)
		{
			attributes = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The Template content was not passed down !");
			return;
		}

		if(attributes.get("urlServer") == null)
		{
			attributes = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The Report Server url was not passed down !");
			return;
		}

		if(attributes.get("printTo") == null)
		{
			attributes = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The Printer name was not passed down !");
			return;
		}

		try
		{
			//create the map of seeds
			ParseSeedsXml((String)attributes.get("seeds"), httpSession);
		} 
		catch (DocumentException e)
		{
			attributes = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , e.toString());
			return;
		} catch (ParseException e)
		{
			attributes = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , e.toString());
			return;
		}
		
		Session session = null;
		try
		{
			session = HibernateUtil.currentSession();
		} catch (HibernateException e3)
		{
			attributes = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , e3.toString());
			return;
		}

		QueryBuilderEngine engine = null;
		try
		{
			engine = new QueryBuilderEngine();
		} catch (QueryBuilderException e1)
		{
			attributes = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , e1.toString());
			return;
		}
		
		Object dsXml = "";
		String key;
		
		Map seeds = (Map)httpSession.getAttribute("seeds");
		Boolean multiRowSeed = (Boolean) httpSession.getAttribute("multiRowSeed");
		
		try
		{
			engine.setSession(session);
			
			Iterator keys = seeds.keySet().iterator();
			while(keys.hasNext())
			{
				key = (String)keys.next();
				engine.setSeed(key, seeds.get(key));
			}
			
			if(multiRowSeed != null && multiRowSeed.booleanValue() == true)
				engine.setSeedsArray((ArrayList) httpSession.getAttribute("seedsArray"));
			
			dsXml = engine.run((String)attributes.get("project"));
		} 
		catch (QueryBuilderException e)
		{
			attributes = null;
			seeds = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("seeds");
			httpSession.removeAttribute("multiRowSeed");
			
			invalidateHttpSession(httpSession);

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , e.toString());
			return;
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			} catch (HibernateException e2)
			{
				attributes = null;
				seeds = null;
				httpSession.removeAttribute("attributes");
				httpSession.removeAttribute("seeds");
				httpSession.removeAttribute("multiRowSeed");
				
				invalidateHttpSession(httpSession);

				response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , e2.toString());
				return;
			}
		}
		
		//ExportType et = null;
		String mimeType = "text/html";
		response.setContentType(mimeType);
		
		HttpReportClient client = new HttpReportClient();		
		byte[] result = null;
		try
		{
			String reportServerUrl = (String)attributes.get("urlServer");
			
			if(reportServerUrl.endsWith("/"))
				reportServerUrl = reportServerUrl.substring(0, reportServerUrl.length() - 1);

			if(!reportServerUrl.endsWith("/PrintReport"))
				reportServerUrl += "/PrintReport";
			
			if(dsXml instanceof String)
			{
				String ds = (String) dsXml;
				result = client.printReport(reportServerUrl, ((String)attributes.get("template")).getBytes(), ds.getBytes(), (String)attributes.get("printTo"), Integer.valueOf((String)attributes.get("copies")).intValue());
			}
			else
			{
				byte[] ds = (byte[]) dsXml;
				result = client.printReport(reportServerUrl, ((String)attributes.get("template")).getBytes(), ds, (String)attributes.get("printTo"), Integer.valueOf((String)attributes.get("copies")).intValue());
			}	
			
		} 
		catch (HttpReportClientException e2)
		{
			attributes = null;
			seeds = null;
			httpSession.removeAttribute("attributes");
			httpSession.removeAttribute("seeds");
			httpSession.removeAttribute("multiRowSeed");
			invalidateHttpSession(httpSession);

			response.setContentType("text/html");
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , e2.toString());
			return;
		}

		response.getOutputStream().write(result);
		response.getOutputStream().flush();
		
		attributes = null;
		seeds = null;
		
		httpSession.removeAttribute("attributes");
		httpSession.removeAttribute("seeds");
		httpSession.removeAttribute("multiRowSeed");
		invalidateHttpSession(httpSession);
	}

	private void invalidateHttpSession(HttpSession httpSession)
	{
		//httpSession.invalidate();
	}

	private void ParseSeedsXml(String xmlSeeds, HttpSession httpSession) throws DocumentException, ParseException
	{
		String name;
		String value;
		String type;
		
		Map seeds = new HashMap();
		ArrayList seedsArray = new ArrayList();

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat tf = new SimpleDateFormat("HH:mm");
		DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		if(xmlSeeds == null)
			return;
		
		Document maindoc = getXmlDocument(xmlSeeds);

		List list = maindoc.selectNodes("/seeds/seed");
		
		Object obj;
		Boolean multiRowSeed = Boolean.FALSE;
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement seed = (DefaultElement) iter.next();

			name = seed.valueOf("name");
			value = seed.valueOf("value");
			type = seed.valueOf("type");
			
			obj = null;
			if(type.equalsIgnoreCase("java.lang.Integer"))
			{
				if(value.length() > 0)
					obj = Integer.valueOf(value);
			}
			else if(type.equalsIgnoreCase("java.math.BigInteger"))
			{
				if(value.length() > 0)
					obj = new BigInteger(value);
			}
			else if(type.equalsIgnoreCase("java.lang.Long"))
			{
				if(value.length() > 0)
					obj = new Long(value);
			}
			else if(type.equalsIgnoreCase("java.lang.Short"))
			{
				if(value.length() > 0)
					obj = new Short(value);
			}
			else if(type.equalsIgnoreCase("java.lang.Boolean"))
			{
				if(value.length() > 0)
					obj = new Boolean(value);
			}
			else if(type.equalsIgnoreCase("java.lang.String"))
			{
				if(value.length() > 0)
					obj = new String(value);
			}
			else if(type.equalsIgnoreCase("java.math.BigDecimal"))
			{
				if(value.length() > 0)
					obj = new BigDecimal(value);
			}
			else if(type.equalsIgnoreCase("java.lang.Float"))
			{
				if(value.length() > 0)
					obj = new Float(value);
			}
			else if(type.equalsIgnoreCase("java.lang.Double"))
			{
				if(value.length() > 0)
					obj = new Double(value);
			}
			else if(type.equalsIgnoreCase("java.sql.Date"))
			{
				if(value.length() > 0)
					obj = df.parse(value);
			}
			else if(type.equalsIgnoreCase("java.sql.Time"))
			{
				if(value.length() > 0)
					obj = tf.parse(value);
			}
			else if(type.equalsIgnoreCase("java.util.Date"))
			{
				if(value.length() > 0)
					obj = dtf.parse(value);
			}
			else
			{
				if(value.length() > 0)
					obj = Integer.valueOf(value);
			}

			if(seeds.get(name) != null)
				multiRowSeed = Boolean.TRUE;
			
			seeds.put(name, new SeedHolder(name, type, obj));
			seedsArray.add(new SeedHolder(name, type, obj));
		}
		
		httpSession.setAttribute("seeds", seeds);
		httpSession.setAttribute("seedsArray", seedsArray);
		httpSession.setAttribute("multiRowSeed", multiRowSeed);
	}

	private static Document getXmlDocument(String xmlBuffer) throws DocumentException
	{
		return DocumentHelper.parseText(xmlBuffer);
	}
	
	private void ParseRequestAttributes(HttpServletRequest request, HttpSession httpSession) throws IOException
	{
		String name;
		String value;
		int equalIndex;
		
		Map attributes = new HashMap();
		
		String stream = this.read(request);
		
		String[] params = stream.split("&");
		for (int i = 0; i < params.length; i++)
		{
			String 	param = params[i];
			
			equalIndex = param.indexOf('=');
			if (equalIndex >= 0) 
			{
				name = URLDecoder.decode(param.substring(0, equalIndex), "UTF-8");
				value = URLDecoder.decode(param.substring(equalIndex + 1), "UTF-8");
				
				attributes.put(name, value);
			}
		}
		
		httpSession.setAttribute("attributes", attributes);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException
	{
		doGet(request, response);
	}
	
	private String read(HttpServletRequest request) throws IOException
	{
		ServletInputStream inputStream = request.getInputStream();
		StringBuffer sb = new StringBuffer(1024*64);
		int c;
		while ((c = inputStream.read()) != -1)
		{
			sb.append((char) c);
		}
		return sb.toString();
	}
	
}
