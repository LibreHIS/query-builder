/*
 * Created on Apr 18, 2005
 *
 */
package com.ims.query.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.zip.CRC32;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;

/**
 * @author vpurdila
 *
 */
public class ReturnAsUrlServlet extends HttpServlet
{
	static final Logger log = Logger.getLogger(ReturnAsUrlServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException
	{
		String[] val = request.getParameterValues("action");
		
		if(val != null && val[0].equalsIgnoreCase("getResult"))
		{
			val = request.getParameterValues("id");
			
			if(val != null)
			{
				ResultHolder result = ResultCollection.getResult(val[0]);
				
				if(result != null)
				{
					response.setContentType(result.getMimeType());
					
					if(result.getMimeType().equalsIgnoreCase("text/xml"))
						response.setHeader("Content-Disposition","inline; filename=report.xml");
					else if(result.getMimeType().equalsIgnoreCase("application/pdf"))
						response.setHeader("Content-Disposition","inline; filename=report.pdf");
					else if(result.getMimeType().equalsIgnoreCase("application/msword"))
						response.setHeader("Content-Disposition","inline; filename=report.rtf");
					else if(result.getMimeType().equalsIgnoreCase("text/csv"))
						response.setHeader("Content-Disposition","inline; filename=report.csv");
					else if(result.getMimeType().equalsIgnoreCase("image/jpeg"))
						response.setHeader("Content-Disposition","inline; filename=report.jpeg");
					
					response.setHeader("Content-Length", String.valueOf(result.getResult().length));
					response.getOutputStream().write(result.getResult());
					response.getOutputStream().flush();
					
					ResultCollection.clearResult(val[0]);
				}
				
				if(log.isDebugEnabled())
				{
					log.debug("ReturnAsUrlServlet >> getResult called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
				}
			}
		}
		else if(val != null && val[0].equalsIgnoreCase("putResult"))
		{
			val = request.getParameterValues("result");
			
			if(val != null)
			{
				BASE64Decoder dec = new BASE64Decoder();
				byte[] report = dec.decodeBuffer(val[0]);
				
				val = request.getParameterValues("type");
				
				if(val != null)
				{
					String mimeType = "text/html";
					if(val[0].equalsIgnoreCase("FP3"))
					{
						mimeType = "text/xml";
					}
					else if(val[0].equalsIgnoreCase("PDF"))
					{
						mimeType = "application/pdf";
					}
					else if(val[0].equalsIgnoreCase("HTML"))
					{
						mimeType = "text/html";
					}
					else if(val[0].equalsIgnoreCase("RTF"))
					{
						mimeType = "application/msword";
					}
					else if(val[0].equalsIgnoreCase("CSV"))
					{
						mimeType = "text/csv";
					}
					else if(val[0].equalsIgnoreCase("JPEG"))
					{
						mimeType = "image/jpeg";
					}
					
					CRC32 crc = new CRC32();
					crc.reset();
					if(report != null)
						crc.update(report);

					String key = String.valueOf(System.currentTimeMillis());
					HttpSession httpSession = request.getSession(true);
					key += httpSession.getId();
					invalidateHttpSession(httpSession);
					key += String.valueOf(crc.getValue());
					
					ResultCollection.putResult(key, new ResultHolder(report, mimeType));
					
					String retVal = "/ReturnAsUrlServlet?action=getResult&id=" + key;
					response.setContentType("text/html");
					response.getOutputStream().write(retVal.getBytes());
					
					if(log.isDebugEnabled())
					{
						log.debug("ReturnAsUrlServlet >> putResult called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
					}
				}
			}
		}

	}

	private void invalidateHttpSession(HttpSession httpSession)
	{
		//httpSession.invalidate();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException
	{
		doGet(request, response);
	}
}
