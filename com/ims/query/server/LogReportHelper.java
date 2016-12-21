/*
 * Created on 3 Oct 2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.server;

import ims.domain.DomainSession;
import ims.domain.impl.DomainImplFlyweightFactory;
import ims.framework.enumerations.SystemLogLevel;
import ims.framework.enumerations.SystemLogType;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.ims.query.server.dummy.impl.IDummy;
import com.ims.query.server.ReportInfo;
import com.ims.query.builder.SeedHolder;

public class LogReportHelper
{
	static final String CRNL = "\n";
	static enum DBType {SQLSERVER, ORACLE, OTHER};
	
	//public static final int INFORMATION = 1;
	//public static final int WARNING = 2;
	//public static final int ERROR = 3;
	//public static final int FATALERROR = 4;
	
	// this is the field order for query that returns template/report info for logging purposes
	// if the query changes make sure that below constants reflect the correct field order
	static final int TEMPLATE_ID = 0; 
	static final int TEMPLATE_NAME = 1;
	static final int REPORT_NAME = 2;
	static final int REPORT_ID = 3;
	static final int IMS_ID = 4;
	
	@SuppressWarnings({"rawtypes"})
	public String getReportInfo(String template)
	{
		StringBuffer sb = new StringBuffer();
		StringBuffer sbQuery = new StringBuffer();
		
		String driverName = null;
		DBType dbType = DBType.OTHER;
		
		try
		{
			//find out DB server type
			driverName = HibernateUtil.getDialect().getClass().getName();
			
			if(driverName.contains("SQLServer"))
				dbType = DBType.SQLSERVER;
			else if(driverName.contains("Oracle"))
				dbType = DBType.ORACLE;
				
		}
		catch(Exception e)
		{
			//should never get here
			e.printStackTrace();
		}
		
		if(DBType.OTHER.equals(dbType))
		{
			sb.append("\n");
			sb.append("Getting template name for logging purposes is only implemented for MSSQLServer and Oracle...");
			return sb.toString();
		}
		else if(DBType.SQLSERVER.equals(dbType))
		{
			sbQuery.append("select c1.id tid, c1.name tname, c2.reportname rname, c2.id rid, c2.imsid \r\n" + 
					"from core_template c1 INNER JOIN core_report c2 ON c1.report = c2.id\r\n" + 
					"where (DATALENGTH(c1.templatexm) = :SIZE) ");
		}
		else if(DBType.ORACLE.equals(dbType))
		{
			sbQuery.append("select c1.id tid, c1.name tname, c2.reportname rname, c2.id rid, c2.imsid \r\n" + 
					"from core_template c1 INNER JOIN core_report c2 ON c1.report = c2.id\r\n" + 
					"where (LENGTH(c1.templatexm) = :SIZE) ");
		}
		
		/*
		The idea is to compute the length of template and try to match that length in the DB
		It is unlikely but not impossible to have more than 1 template with the same size in DB
		If the query returns 1 record then that is the template we are looking for
		Otherwise we get templates one by one and compare with the existing one, we take first match
		*/
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		Iterator it;
		try
		{
			Query q = session.createSQLQuery(sbQuery.toString());
			q.setParameter("SIZE", template.length());
			
			it = q.list().iterator();
			tx.commit();
		}
		catch (HibernateException e)
		{
			e.printStackTrace();
			
			sb.append("Error occurred getting template name for logging purposes...");
			return sb.toString();
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

		ArrayList<Object[]> rows = new ArrayList<Object[]>();
		
		while (it.hasNext())
		{
			Object[] row = makeObjectArray(it.next());
		
			rows.add(row);
		}	
		
		if(rows.size() == 1)
		{
			//this is the template we are looking for
			ReportInfo info = buildReportInfo((Object[])rows.get(0));
			printReportInfo(sb, info);
		}
		else
		{
			//need to compare templates one by one
			int size = rows.size();
			
			for(int i = 0; i < size; i++)
			{
				Object[] row = rows.get(i);
				
				try
				{
					String remoteTemplate = getTemplateById(row[TEMPLATE_ID]);
					
					if(remoteTemplate != null && remoteTemplate.equals(template))
					{
						ReportInfo info = buildReportInfo((Object[])rows.get(i));
						printReportInfo(sb, info);
						break;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					
					sb.append("Error occurred getting template name for logging purposes...");
					return sb.toString();
				}

			}
		}
		
		//System.out.println("Dialect: " + HibernateUtil.getDialect().getClass().getName());
		
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private String getTemplateById(Object object)
	{
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		Iterator it;
		try
		{
			Query q = session.createQuery("select t.templateXml from TemplateBo as t where t.id = :TID");
			q.setParameter("TID", ((BigDecimal)object).intValue());
			
			it = q.list().iterator();
			tx.commit();
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
		
		if (it.hasNext())
		{
			Object[] row = makeObjectArray(it.next());
		
			return row[0] == null ? null : row[0].toString();
		}			
		
		return null;
	}

	private void printReportInfo(StringBuffer sb, ReportInfo info)
	{
		if(info == null)
		{
			sb.append("Null info argument for printReportInfo(StringBuffer sb, ReportInfo info)");
			sb.append("\n");
			return;
		}
			
		sb.append("Template id: ");
		sb.append(info.getTemplateId());
		sb.append("\n");
		sb.append("Template name: ");
		sb.append(info.getTemplateName());
		sb.append("\n");
		sb.append("Report id: ");
		sb.append(info.getReportId());
		sb.append("\n");
		sb.append("Report name: ");
		sb.append(info.getReportName());
		sb.append("\n");
		sb.append("Report's IMS id: ");
		sb.append(info.getImsId());
		sb.append("\n");
		
	}

	private Object[] makeObjectArray(Object obj)
	{
		if(obj instanceof Object[])
			return (Object[])obj;
		
		Object[] res = new Object[1];
		
		res[0] = obj;
		
		return res;
	}
	

	public void createSystemLogEntry(String msg, boolean bLogReport, SystemLogLevel level)
	{
		IDummy impl;

		/*
		SystemLogLevel sll = SystemLogLevel.INFORMATION;
		
		switch(level)
		{
			case LogReportHelper.INFORMATION:
				sll = SystemLogLevel.INFORMATION;
				break;
			case LogReportHelper.WARNING:
				sll = SystemLogLevel.WARNING;
				break;
			case LogReportHelper.ERROR:
				sll = SystemLogLevel.ERROR;
				break;
			case LogReportHelper.FATALERROR:
				sll = SystemLogLevel.FATALERROR;
				break;
			default:
				break;
		}
		*/
		
		if(bLogReport)
		{
			try
			{
				impl = (IDummy) getDomainImpl("com.ims.query.server.dummy.impl.DummyImpl");
				impl.createSystemLogEntry(SystemLogType.REPORTS, level, msg);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void createSystemLogEntry(String msg, boolean bLogReport)
	{
		createSystemLogEntry(msg, bLogReport, SystemLogLevel.INFORMATION);
	}

	private Object getDomainImpl(String className) throws Exception
	{
		DomainSession sess = DomainSession.getSession();
		Class<?> implClass = Class.forName(className);
		DomainImplFlyweightFactory factory = DomainImplFlyweightFactory.getInstance();
		return factory.create(implClass, sess);
	}

	private ReportInfo buildReportInfo(Object[] object)
	{
		ReportInfo ri = null;
		
		if(object == null)
			return null;
		
		try
		{
			if(object[IMS_ID] instanceof Integer || object[IMS_ID] == null)
				ri = new ReportInfo((BigDecimal)object[TEMPLATE_ID], (String)object[TEMPLATE_NAME], (BigDecimal)object[REPORT_ID], (String)object[REPORT_NAME], (Integer)object[IMS_ID]);
			else if(object[IMS_ID] instanceof BigDecimal)
			{
				Integer imsId = new Integer(((BigDecimal)object[IMS_ID]).intValue());
				ri = new ReportInfo((BigDecimal)object[TEMPLATE_ID], (String)object[TEMPLATE_NAME], (BigDecimal)object[REPORT_ID], (String)object[REPORT_NAME], imsId);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return ri;
	}

	//displays seeds info in the log
	@SuppressWarnings("rawtypes")
	public void printSeeds(Map seeds, ArrayList seedsArray, StringBuffer systemLog, boolean bMultiSeed)
	{
		String lastSeed = "";
		String lastType = "";
		int count;
		StringBuffer seedVal = new StringBuffer();
		String key;
		
		if((seeds == null || seeds.isEmpty() && (seedsArray == null || seedsArray.isEmpty())))
			return;
		
		systemLog.append("Report seeds ------------------------------------------------------------");
		systemLog.append(CRNL);
		
		if(seedsArray != null && !seedsArray.isEmpty() && bMultiSeed)
		{
			count = seedsArray.size();
			
			for(int i = 0; i < count; i++)
			{
				SeedHolder sh = (SeedHolder) seedsArray.get(i);
				
				if(!lastSeed.equals(sh.getName()))
				{
					if(lastSeed.length() > 0)
					{
						systemLog.append("Value(s): ");
						systemLog.append(seedVal.toString());
						systemLog.append(CRNL);
					}
					
					lastSeed = sh.getName();
					lastType = sh.getType();
					seedVal.setLength(0);
					
					systemLog.append("Seed: ");
					systemLog.append(lastSeed);
					systemLog.append("; ");
					systemLog.append("Type: ");
					systemLog.append(lastType);
					systemLog.append("; ");
				}

				if(seedVal.length() > 0)
					seedVal.append(",");
					
				seedVal.append(formatSeedValue(sh));
			}
			
			if(lastSeed.length() > 0)
			{
				systemLog.append("Value(s): ");
				systemLog.append(seedVal.toString());
				systemLog.append(CRNL);
			}
		}
		else
		{
			Iterator keys = seeds.keySet().iterator();
			while(keys.hasNext())
			{
				key = (String)keys.next();
				SeedHolder sh = (SeedHolder)seeds.get(key);

				systemLog.append("Seed: ");
				systemLog.append(sh.getName());
				systemLog.append("; ");
				systemLog.append("Type: ");
				systemLog.append(sh.getType());
				systemLog.append("; ");
				systemLog.append("Value: ");
				systemLog.append(sh.getValue() == null ? "(null)" : formatSeedValue((SeedHolder)seeds.get(key)));
				systemLog.append(CRNL);
			}
		}
		
		systemLog.append("-----------------------------------------------------------------------------");
		systemLog.append(CRNL);
	}

	private String formatSeedValue(SeedHolder sh)
	{
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat tf = new SimpleDateFormat("HH:mm");
		DateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		String type = sh.getType();
		Object value = sh.getValue();
		
		if(value == null)
			return "(null)";
		
		if(type.equalsIgnoreCase("java.sql.Date"))
		{
			return df.format(value);
		}
		else if(type.equalsIgnoreCase("java.sql.Time"))
		{
			return tf.format(value);
		}
		else if(type.equalsIgnoreCase("java.util.Date"))
		{
			return dtf.format(value);			
		}
		else
		{
			return value.toString();
		}
	}
}
