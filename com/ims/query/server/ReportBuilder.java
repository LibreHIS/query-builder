/*
 * Created on Feb 18, 2005
 *
 */
package com.ims.query.server;

import ims.configuration.gen.ConfigFlag;
import ims.framework.enumerations.SystemLogLevel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.DefaultElement;
import com.ims.query.builder.QueryBuilderEngine;
import com.ims.query.builder.SeedHolder;
import com.ims.query.builder.exceptions.QueryBuilderException;
import com.ims.report.client.ExportType;
import com.ims.report.client.HttpReportClient;
import com.ims.report.client.exceptions.HttpReportClientException;

/**
 * @author vpurdila
 *
 */
public class ReportBuilder extends HttpServlet
{
	static final Logger log = Logger.getLogger(ReportBuilder.class);
	static final String CRNL = "\n";
	
    private static final char[] HEX_CHARS = {'0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f',};
    
	
	@SuppressWarnings({"rawtypes"})
	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException
	{
		boolean bReturnUrl = false;
		long time1 = System.currentTimeMillis();
		StringBuffer systemLog = new StringBuffer();
		DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		LogReportHelper logHelper = new LogReportHelper();
		
		boolean bLogReport = ConfigFlag.GEN.LOG_REPORT_EXECUTION.getValue();
		boolean bCompress = ConfigFlag.GEN.COMPRESS_REPORT_DATA.getValue();
		
		HttpSession httpSession = request.getSession(true);
		
		clearHttpSessionAttributes(httpSession);
		httpSession.removeAttribute(StringUtils.IMS_LAST_ERROR);
		
		if(bLogReport)
		{
			systemLog.append("===  Report log execution ===");
			systemLog.append(CRNL);
			systemLog.append("Start time: " + dtf.format(new Date()));
			systemLog.append(CRNL);
		}

		ParseRequestAttributes(request, httpSession);
		
		Map attributes = (Map)httpSession.getAttribute("attributes");
		
		if(attributes.get("project") == null)
		{
			attributes = null;
			clearHttpSessionAttributes(httpSession);
			
			systemLog.append("The QueryBuilder project content was not passed down !");
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.ERROR);
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The QueryBuilder project content was not passed down !");
			return;
		}

		if(attributes.get("template") == null)
		{
			attributes = null;
			clearHttpSessionAttributes(httpSession);

			systemLog.append("The Template content was not passed down !");
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.ERROR);
			
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The Template content was not passed down !");
			return;
		}

		if(attributes.get("urlServer") == null)
		{
			attributes = null;
			clearHttpSessionAttributes(httpSession);

			systemLog.append("The Report Server url was not passed down !");
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.ERROR);
			
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The Report Server url was not passed down !");
			return;
		}

		if(attributes.get("format") == null)
		{
			attributes = null;
			clearHttpSessionAttributes(httpSession);

			systemLog.append("The Report's format was not passed down !");
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.ERROR);
			
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The Report's format was not passed down !");
			return;
		}

		if(attributes.get("returnUrl") != null)
		{
			if(((String)attributes.get("returnUrl")).equalsIgnoreCase("true"))
				bReturnUrl = true;
		}
		
		if(bLogReport)
		{
			systemLog.append(logHelper.getReportInfo(attributes.get("template").toString()));
		}
		
		try
		{
			//create the map of seeds
			ParseSeedsXml((String)attributes.get("seeds"), httpSession);
		} 
		catch (DocumentException e)
		{
			attributes = null;
			clearHttpSessionAttributes(httpSession);
			
			systemLog.append("Error thrown: " + e.toString());
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.FATALERROR);

			httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, systemLog.toString());
			
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , e.toString());
			return;
		} catch (ParseException e)
		{
			attributes = null;
			clearHttpSessionAttributes(httpSession);
			
			systemLog.append("Error thrown: " + e.toString());
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.FATALERROR);

			httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, systemLog.toString());
			
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
			clearHttpSessionAttributes(httpSession);
			
			systemLog.append("Error thrown: " + e3.toString());
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.FATALERROR);

			httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, systemLog.toString());
			
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , getStackTrace(e3));
			return;
		}

		QueryBuilderEngine engine = null;
		try
		{
			engine = new QueryBuilderEngine();
		} catch (QueryBuilderException e1)
		{
			attributes = null;
			clearHttpSessionAttributes(httpSession);
			
			systemLog.append("Error thrown: " + e1.toString());
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.FATALERROR);

			httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, systemLog.toString());
			
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , getStackTrace(e1));
			return;
		}
		
		Object dsXml = "";
		String key;
		
		Map seeds = (Map)httpSession.getAttribute("seeds");
		Boolean multiRowSeed = (Boolean) httpSession.getAttribute("multiRowSeed");
		
		try
		{
			engine.setSession(session);
			
			//WDEV-10113
			engine.setSystemLog(systemLog);
			
			//WDEV-15182
			engine.setCompressData(bCompress);
			
			Iterator keys = seeds.keySet().iterator();
			while(keys.hasNext())
			{
				key = (String)keys.next();
				engine.setSeed(key, seeds.get(key));
			}
			
			if(multiRowSeed != null && multiRowSeed.booleanValue() == true)
				engine.setSeedsArray((ArrayList) httpSession.getAttribute("seedsArray"));
			
			if(bLogReport)
				logHelper.printSeeds(seeds, (ArrayList) httpSession.getAttribute("seedsArray"), systemLog, Boolean.TRUE.equals(multiRowSeed));
			
			dsXml = engine.run((String)attributes.get("project"));
		} 
		catch (QueryBuilderException e)
		{
			attributes = null;
			seeds = null;
			clearHttpSessionAttributes(httpSession);

			systemLog.append("Error thrown: " + e.toString());
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.FATALERROR);
			
			httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, systemLog.toString());

			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , getStackTrace(e));
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
				clearHttpSessionAttributes(httpSession);

				systemLog.append("Error thrown: " + e2.toString());
				logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.FATALERROR);
				
				httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, systemLog.toString());
				
				response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , getStackTrace(e2));
				return;
			}
		}
		
		//response.setContentType("text/xml");
		//out.print(dsXml);
		
		ExportType et = null;
		String mimeType = "text/html";
		if(((String)attributes.get("format")).equalsIgnoreCase("FP3"))
		{
			et = ExportType.FP3;
			mimeType = "text/xml";
			response.setHeader("Content-Disposition","inline; filename=report.xml");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("PDF"))
		{
			et = ExportType.PDF;
			mimeType = "application/pdf";
			response.setHeader("Content-Disposition","inline; filename=report.pdf");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("HTML"))
		{
			et = ExportType.HTML;
			mimeType = "text/html";
			response.setHeader("Content-Disposition","inline; filename=report.html");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("RTF"))
		{
			et = ExportType.RTF;
			mimeType = "application/msword";
			response.setHeader("Content-Disposition","inline; filename=report.rtf");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("CSV"))
		{
			et = ExportType.CSV;
			mimeType = "text/csv";
			response.setHeader("Content-Disposition","inline; filename=report.csv");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("DS"))
		{
			et = ExportType.DS;
			mimeType = "text/html";
			response.setHeader("Content-Disposition","inline; filename=report.txt");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("JPEG"))
		{
			et = ExportType.JPEG;
			mimeType = "image/jpeg";
			response.setHeader("Content-Disposition","inline; filename=report.jpg");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("TXT"))
		{
			et = ExportType.TXT;
			mimeType = "text/plain";
			response.setHeader("Content-Disposition","inline; filename=report.txt");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("XLS"))
		{
			et = ExportType.XLS;
			mimeType = "application/excel";
			response.setHeader("Content-Disposition","inline; filename=report.xls");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("DOC"))
		{
			et = ExportType.DOC;
			mimeType = "application/msword";
			response.setHeader("Content-Disposition","inline; filename=report.doc");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("DOCX"))
		{
			et = ExportType.DOCX;
			mimeType = "application/msword";
			response.setHeader("Content-Disposition","inline; filename=report.docx");
		}
		else if(((String)attributes.get("format")).equalsIgnoreCase("MHT"))
		{
			et = ExportType.MHT;
			mimeType = "text/html";
			response.setHeader("Content-Disposition","inline; filename=report.mht");
		}
		
		
		response.setContentType(mimeType);
		
		/*
		if(mimeType.equalsIgnoreCase("text/xml"))
			response.setHeader("Content-Disposition","inline; filename=report.xml");
		else if(mimeType.equalsIgnoreCase("application/pdf"))
			response.setHeader("Content-Disposition","inline; filename=report.pdf");
		else if(mimeType.equalsIgnoreCase("application/msword") && ExportType.RTF.equals(et))
			response.setHeader("Content-Disposition","inline; filename=report.rtf");
		else if(mimeType.equalsIgnoreCase("application/msword") && ExportType.DOC.equals(et))
			response.setHeader("Content-Disposition","inline; filename=report.doc");
		else if(mimeType.equalsIgnoreCase("application/msword") && ExportType.DOCX.equals(et))
			response.setHeader("Content-Disposition","inline; filename=report.docx");
		else if(mimeType.equalsIgnoreCase("text/csv"))
			response.setHeader("Content-Disposition","inline; filename=report.csv");
		else if(mimeType.equalsIgnoreCase("text/html"))
			response.setHeader("Content-Disposition","inline; filename=report.txt");
		else if(mimeType.equalsIgnoreCase("text/plain"))
			response.setHeader("Content-Disposition","inline; filename=report.txt");
		else if(mimeType.equalsIgnoreCase("application/excel"))
			response.setHeader("Content-Disposition","inline; filename=report.xls");
		*/

		if(et != null && et.equals(ExportType.DS))
		{
			if(dsXml instanceof String)
			{
				String ds = (String)dsXml;
				response.setHeader("Content-Length", String.valueOf(ds.getBytes().length));			
				response.getOutputStream().write(ds.getBytes());
			}
			else
			{
				byte[] ds = (byte[])dsXml;
				response.setHeader("Content-Length", String.valueOf(ds.length));			
				response.getOutputStream().write(ds);
			}
			
			response.getOutputStream().flush();
			
			attributes = null;
			seeds = null;
			
			clearHttpSessionAttributes(httpSession);
			return;
		}
		
		HttpReportClient client = new HttpReportClient();		
		byte[] result = null;
		try
		{
			if(dsXml instanceof String)
			{
				String ds = (String) dsXml;
				result = client.buildReport((String)attributes.get("urlServer"), ((String)attributes.get("template")).getBytes(), ds.getBytes(), et, (String)attributes.get("printTo"), Integer.valueOf((String)attributes.get("copies")).intValue());
			}
			else
			{
				byte[] ds = (byte[]) dsXml;
				result = client.buildReport((String)attributes.get("urlServer"), ((String)attributes.get("template")).getBytes(), ds, et, (String)attributes.get("printTo"), Integer.valueOf((String)attributes.get("copies")).intValue());
			}	
		} 
		catch (HttpReportClientException e2)
		{
			attributes = null;
			seeds = null;
			clearHttpSessionAttributes(httpSession);
			
			systemLog.append("Error thrown: " + e2.toString());
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport, SystemLogLevel.FATALERROR);

			httpSession.setAttribute(StringUtils.IMS_LAST_ERROR, systemLog.toString());
			
			response.setContentType("text/html");
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , getStackTrace(e2));
			return;
		}

		if(bReturnUrl == false)
		{
			response.setHeader("Content-Length", String.valueOf(result.length));			
			response.getOutputStream().write(result);
		}
		else
		{
			CRC32 crc = new CRC32();
			crc.reset();
			if(result != null)
				crc.update(result);

			key = String.valueOf(System.currentTimeMillis());
			key += httpSession.getId();
			key += String.valueOf(crc.getValue());
			
			ResultCollection.putResult(key, new ResultHolder(result, mimeType));
			
			String retVal = "/ReturnAsUrlServlet?action=getResult&id=" + key + "&srv=" + InetAddress.getLocalHost().getHostName();
			response.setContentType("text/html");
			response.getOutputStream().write(retVal.getBytes());
			
			if(log.isDebugEnabled())
			{
				log.debug("ReportBuilder Servlet >> report stored on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
			}
			
		}

		if(bLogReport)
		{
			long time2 = System.currentTimeMillis();

			systemLog.append("End time: " + dtf.format(new Date()));
			systemLog.append(CRNL);
			systemLog.append("Report execution time: " + (time2 - time1) + " ms");
			systemLog.append(CRNL);
			systemLog.append("Remote host: " + request.getRemoteAddr());
			
			/*
			if(httpSession != null && httpSession.getAttribute("USER") != null)
				systemLog.append("User: " + httpSession.getAttribute("USER"));
			*/	
			
			logHelper.createSystemLogEntry(systemLog.toString(), bLogReport);
		}

		response.getOutputStream().flush();
		
		attributes = null;
		seeds = null;
		
		clearHttpSessionAttributes(httpSession);
	}

	
	private void clearHttpSessionAttributes(HttpSession httpSession)
	{
		//httpSession.invalidate();
		httpSession.removeAttribute("attributes");
		httpSession.removeAttribute("seeds");
		httpSession.removeAttribute("seedsArray");
		httpSession.removeAttribute("multiRowSeed");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
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
					obj = new java.sql.Date(df.parse(value).getTime());
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
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void ParseRequestAttributes(HttpServletRequest request, HttpSession httpSession) throws IOException
	{
		String name;
		String value;
		int equalIndex;
		
		Map attributes = new HashMap();
		
		String stream = this.read(request);
		
		//System.out.println(stream);
		
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
				
				//System.out.println("( " + name + ", " + value + " )");
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
	
	private String getStackTrace(Throwable aThrowable)
	{
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	public String MD5(byte[] buffer) throws NoSuchAlgorithmException
	{
		MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	    digest.update(buffer);
	    byte[] md5 = digest.digest();	
	    
	    return asHex(md5);
	}
	
	public String asHex (byte hash[]) 
	{
        char buf[] = new char[hash.length * 2];
    
        for (int i = 0, x = 0; i < hash.length; i++) 
        {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        
        return (new String(buf)).toUpperCase();
    }	


}
