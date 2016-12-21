/*
 * Created on Jan 28, 2005
 *
 */
package com.ims.query.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.hibernate.Session;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;
import bsh.TargetError;

import com.ims.query.builder.cache.CacheScript;
import com.ims.query.builder.exceptions.QueryBuilderException;
import com.ims.query.builder.parser.ImsHibernateQuery;
import com.ims.query.builder.parser.Param;
import com.ims.query.builder.parser.QueryBuilderParser;
import com.ims.query.builder.parser.VelocityGenerator;

/**
 * 
 * QueryBuilderEngine executes a QueryBuilder project and returns a datasource in XML format
 * 
 * @param session 	>> call <b>setSession</b>(yourHibernateSession)
 * @param seed		>> call <b>setSeed</b>(seedName, seedValue) once or several times in order to initialise 
 * 					   the input params for HQL queries	
 * 
 * @author vpurdila
 */
@SuppressWarnings("unchecked")
public class QueryBuilderEngine
{
	static final Logger log = Logger.getLogger(QueryBuilderEngine.class);
	
	static final String SYS_DATE_TIME = "SYS_DATE_TIME";
	static final String MULTISEED_PRFIX = "_ims_multi_";
	
	private Session session;
	private QueryBuilderParser parser;
	private Interpreter ip;
	@SuppressWarnings("rawtypes")
	private HashMap seeds; 
	@SuppressWarnings("rawtypes")
	private ArrayList seedsArray;
	private VelocityGenerator generator;
	private CRC32 crc;
	private StringBuffer systemLog;
	private boolean compressData;

	private DateFormat qbDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private DateFormat qbTimeFormat = new SimpleDateFormat("HH:mm");
	private DateFormat qbDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	@SuppressWarnings("rawtypes")
	public QueryBuilderEngine() throws QueryBuilderException
	{
		compressData = false;
		parser = new QueryBuilderParser();
		seeds = new HashMap();
		try
		{
			generator = new VelocityGenerator();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			throw new QueryBuilderException("The Velocity engine could not be initialised !");
		}
		crc = new CRC32();
		
		if(log.isDebugEnabled())
		{
			try
			{
				System.out.println("QueryBuilderEngine() called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param session >> pass down your hibernate session
	 */
	public void setSession(Session session)
	{
		this.session = session;
	}
	
	/**
	 * 
	 * @param seedName
	 * @param seedVal >> this parameter can be: Integer, Boolean, String, BigDecimal, Date, Time 
	 */
	public void setSeed(String seedName, Object seedVal)
	{
		seeds.put(seedName, seedVal);
	}
	
	@SuppressWarnings("rawtypes")
	public ArrayList getSeedsArray()
	{
		return seedsArray;
	}

	@SuppressWarnings("rawtypes")
	public void setSeedsArray(ArrayList seedsArray)
	{
		this.seedsArray = seedsArray;
	}
	
	public StringBuffer getSystemLog()
	{
		return systemLog;
	}

	public void setSystemLog(StringBuffer systemLog)
	{
		this.systemLog = systemLog;
	}
	
	public boolean isCompressData()
	{
		return compressData;
	}

	public void setCompressData(boolean compressData)
	{
		this.compressData = compressData;
	}

	/**
	 * 
	 * @param xmlProject in XML format
	 * @return the datasource in XML format
	 * @throws QueryBuilderException
	 */
	@SuppressWarnings("rawtypes")
	public Object run(String xmlProject) throws QueryBuilderException
	{
		Object o = null;
		String java;
		long mili1 = 0;
		long mili2 = 0;
		
		if(log.isDebugEnabled())
		{
			log.debug("Running query builder...");
			log.debug("Parameter @xmlProject = " + xmlProject);
			
			mili1 = System.currentTimeMillis();
		}
		
		try
		{
			parser.parseQueryBuilderProject(xmlProject);
			
			if(log.isDebugEnabled())
			{
				mili2 = System.currentTimeMillis();
				log.debug("Parsing query builder project took " + (mili2 - mili1) + " miliseconds.");
			}
		} 
		catch (DocumentException e)
		{
			log.error("Error parsing the query builder project: " + e);
			throw new QueryBuilderException("I could not parse the QueryBuilder project !\r\n" + e.toString());
		}
		
		boolean bHasValidators = false;
		boolean bHasMultiSeedValues = false;
		for(int i = 0; i < parser.getHibQuery().size();i++)
		{
			ImsHibernateQuery query = (ImsHibernateQuery)parser.getHibQuery().get(i);
			
			if(query.getStartIndex() != -1 && query.getEndIndex() != -1)
			{
				bHasValidators = true;
				break;
			}
		}

		if(log.isDebugEnabled())
		{
			mili1 = System.currentTimeMillis();
		}
		
		crc.reset();
		crc.update(xmlProject.getBytes());
		
		if(log.isDebugEnabled())
		{
			mili2 = System.currentTimeMillis();
			log.debug("Calculating CRC32 for query builder project took " + (mili2 - mili1) + " miliseconds.");
		}
		
		Long lkey = new Long(crc.getValue());
		
		Set set = seeds.keySet();
		Iterator iter = set.iterator();
		String key;
		DataSource dsSeeds = null;
		ArrayList alSeeds = null;
		String val;
		String type;
		
		if(iter.hasNext())
		{
			alSeeds = new ArrayList();
			dsSeeds = new DataSource("SEEDS", alSeeds);
			
			if(seedsArray != null)
			{
				for (int i = 0; i < seedsArray.size(); i++)
				{
					SeedHolder sh = (SeedHolder) seedsArray.get(i);
					
					type = sh.getType();
					
					if(type.equalsIgnoreCase("java.sql.Date"))
						val = sh.getValue() == null ? "" : qbDateFormat.format(sh.getValue());
					else if(type.equalsIgnoreCase("java.sql.Time"))
						val = sh.getValue() == null ? "" : qbTimeFormat.format(sh.getValue());
					else if(type.equalsIgnoreCase("java.util.Date"))
						val = sh.getValue() == null ? "" : qbDateTimeFormat.format(sh.getValue());
					else
						val = sh.getValue() == null ? "" : sh.getValue().toString();
	
					DataField df = dsSeeds.getDataFieldByName(sh.getName());
					
					if(df == null)
						alSeeds.add(new DataField(sh.getName(), val, sh.getType()));
					else
						df.getValues().add(val);
				}
			}
		}
		
		while(iter.hasNext())
		{
			key = (String)iter.next();

			//populate the SEEDS datasource
			if(seedsArray == null)
			{	
				type = ((SeedHolder)seeds.get(key)).getType();
				
				if(type.equalsIgnoreCase("java.sql.Date"))
					val = ((SeedHolder)seeds.get(key)).getValue() == null ? "" : qbDateFormat.format(((SeedHolder)seeds.get(key)).getValue());
				else if(type.equalsIgnoreCase("java.sql.Time"))
					val = ((SeedHolder)seeds.get(key)).getValue() == null ? "" : qbTimeFormat.format(((SeedHolder)seeds.get(key)).getValue());
				else if(type.equalsIgnoreCase("java.util.Date"))
					val = ((SeedHolder)seeds.get(key)).getValue() == null ? "" : qbDateTimeFormat.format(((SeedHolder)seeds.get(key)).getValue());
				else
					val = ((SeedHolder)seeds.get(key)).getValue() == null ? "" : ((SeedHolder)seeds.get(key)).getValue().toString();

				if(log.isDebugEnabled())
				{
					log.debug("Passing down pairs of (key, value) >> (" + key + ", " + val + ")");
				}
				
				alSeeds.add(new DataField(key, val, ((SeedHolder)seeds.get(key)).getType()));
			}
		}	
		
		//check multi value seeds
		for(int i = 0; i < parser.getHibQuery().size();i++)
		{
			ImsHibernateQuery query = (ImsHibernateQuery)parser.getHibQuery().get(i);

			if(query.getParams() != null)
			{
				ArrayList newParams = new ArrayList();
				
				for (int j = 0; j < query.getParams().size(); j++)
				{
					Param param = (Param) query.getParams().get(j);
					
					if(seedsArray != null && alSeeds != null)
					{
						for (int k = 0; k < alSeeds.size(); k++)
						{
							DataField df = (DataField)alSeeds.get(k);
							
							if(df.getValues() != null && df.getValues().size() > 1 && df.getName().equalsIgnoreCase(param.getField()))
							{
								String seedCommaString = "";
								for (int index = 0; index < df.getValues().size(); index++)
								{
									if(seedCommaString.length() > 0)
										seedCommaString += ", ";
									
									String newSeedName = MULTISEED_PRFIX + df.getName() + "_" + (index + 1) + "_";
									seedCommaString += ":" + newSeedName;
									
									Param newParam = new Param(newSeedName, param.getIndex(), param.getFieldType(), newSeedName);
									newParams.add(newParam);
								}
								
								if(seedCommaString.length() > 0)
								{
									String hql = query.getHqlString();
									String xmlClauses = query.getXmlClauses();
									
									int len1 = hql.length();
									
									hql = hql.replaceAll(":" + param.getField(), seedCommaString);
									
									int len2 = hql.length();
									
									query.setHqlString(hql);
									
									bHasMultiSeedValues = true;
									
									if(query.getStartIndex() != -1 && query.getEndIndex() != -1)
									{
										query.setEndIndex(query.getEndIndex() + len2 - len1);
										
										xmlClauses = xmlClauses.replaceAll(":" + param.getField(), seedCommaString);
										xmlClauses = xmlClauses.replaceAll("<validator>" + param.getField() + "</validator>", "<validator>" + MULTISEED_PRFIX + param.getField() + "_1_" + "</validator>");
										query.setXmlClauses(xmlClauses);
									}
								}
							}
						}
					}
				}
				
				if(newParams.size() > 0)
				{
					if(query.getParams() == null)
						query.setParams(newParams);
					else
					{
						for (int j = 0; j < newParams.size(); j++)
						{
							query.getParams().add(newParams.get(j));
						}
					}
				}
			}
		}
		
		
		try
		{
			if(CacheScript.hasKey(lkey) && bHasValidators == false && bHasMultiSeedValues == false)
			{
				java = (String)CacheScript.getCachedObject(lkey);

				if(log.isDebugEnabled())
				{
					log.debug("Dynamic java code already cached...");
					log.debug(java);
				}
			}
			else
			{
				if(log.isDebugEnabled())
				{
					mili1 = System.currentTimeMillis();
				}
				
				java = generator.Generate(parser.getHibQuery(), "/QueryBuilderCsvGzip.vm");
				
				/*
				if(compressData)
					java = generator.Generate(parser.getHibQuery(), "/QueryBuilderCsvGzip.vm");
				else
					java = generator.Generate(parser.getHibQuery(), "/QueryBuilderCsv.vm");
				*/
				
				//we don't cache the java code if we have validators because that code might change dynamically depending on SEEDS values
				if(bHasValidators == false && bHasMultiSeedValues == false)
					CacheScript.cacheObject(lkey, java);
				
				if(log.isDebugEnabled())
				{
					log.debug("Generating dynamic java code...");
					log.debug(java);
					mili2 = System.currentTimeMillis();
					log.debug("Generating dynamic java code took " + (mili2 - mili1) + " miliseconds.");
				}
			}
		} 
		catch (Exception e)
		{
			log.error("Error generating java code from Velocity template: " + e);
			throw new QueryBuilderException("Error running the Velocity template !");
		}
		
		try
		{
			ip = new Interpreter();
			ip.set("session",session);
			
			if(log.isDebugEnabled())
			{
				log.debug("Using session: " + session);
			}
			
			iter = set.iterator();
			while(iter.hasNext())
			{
				key = (String)iter.next();
				ip.set(key, ((SeedHolder)seeds.get(key)).getValue());
			}
			
			ip.set("IMS_SEEDS_123", dsSeeds != null ? dsSeeds.toCsv() : "");

			//system date time seed
			java.util.Date now = new Date();
			ip.set(SYS_DATE_TIME, now);
			
			//system date time derived params
			for(int i = 0; i < parser.getHibQuery().size();i++)
			{
				ImsHibernateQuery query = (ImsHibernateQuery)parser.getHibQuery().get(i);

				if(query.getParams() != null)
				{
					for (int j = 0; j < query.getParams().size(); j++)
					{
						Param param = (Param) query.getParams().get(j);
						
						if(param.getField() != null && param.getField().startsWith(SYS_DATE_TIME) && !param.getField().equalsIgnoreCase(SYS_DATE_TIME))
						{
							ip.set(param.getField(), getModifiedTime(now, param.getField()));
						}
					}
				}
			}
			
			
			/*multiseed handling - will only work for "IN" and "NOT IN" HQL statements
			
			if we have an HQL statement like: "from Patient p where p.id IN (:PID)" and the seed PID has 3 values, the query will become
			"from Patient p where p.id IN (:_ims_multi_PID_1_, :_ims_multi_PID_2_, :_ims_multi_PID_3_)" and 3 new HQL query params will be created:
			_ims_multi_PID_1_, _ims_multi_PID_2_, _ims_multi_PID_3_ 
			*/
			
			//ip.set("MULTI_SEEDS", alSeeds);

			int index = 0;
			String lastSeed = "";
			if(seedsArray != null && alSeeds != null)
			{
				for (int i = 0; i < alSeeds.size(); i++)
				{
					DataField df = (DataField)alSeeds.get(i);
					
					if(!lastSeed.equalsIgnoreCase(df.getName()))
					{
						index = 1;
						lastSeed = df.getName();
					}
					
					if(df.getValues() != null && df.getValues().size() > 1)
					{
						for (int j = 0; j < seedsArray.size(); j++)
						{
							SeedHolder sh = (SeedHolder) seedsArray.get(j);
							
							if(df.getName().equalsIgnoreCase(sh.getName()))
							{
								String seedKey = MULTISEED_PRFIX + df.getName() + "_" + index + "_";
								ip.set(seedKey, sh.getValue());
								index++;
							}
						}
					}
				}
			}

			if(log.isDebugEnabled())
			{
				mili1 = System.currentTimeMillis();
			}
			
			//WDEV-10113
			ip.set("systemLog", systemLog);
			
			//WDEV-15182
			ip.set("compressData", compressData);
			
			o = ip.eval(java);
			
			if(log.isDebugEnabled() && o instanceof String)
			{
				String dsxml = (String)o;
				
				log.debug("The java interpreter returned:");
				
				if(dsxml.length() < 20000)
					log.debug(dsxml);
				else
				{
					log.debug("The buffer size was " + String.valueOf(dsxml.length()));

					writeToFile("ds.xml", dsxml.getBytes());
				}

				mili2 = System.currentTimeMillis();
				log.debug("Running the script code took " + (mili2 - mili1) + " miliseconds.");
			}
			else if(log.isDebugEnabled() && o instanceof byte[])
			{
				byte[] dsxml = (byte[])o;
				
				log.debug("The dataset was compressed and its size is: " + dsxml.length);
				
				writeToFile("ds.csv.zip", dsxml);

				mili2 = System.currentTimeMillis();
				log.debug("Running the script code took " + (mili2 - mili1) + " miliseconds.");
			}

		} 
		catch (TargetError e)
		{
			Throwable t = e.getTarget();
			
			if(t instanceof TargetError)
			{
				if(((TargetError)t).getTarget() instanceof org.hibernate.JDBCException)
				{
					org.hibernate.JDBCException je = (org.hibernate.JDBCException)((TargetError)t).getTarget();
					
					log.error("Error running the script: " + je.getSQLException());
					throw new QueryBuilderException("Error running the script !\r\n" + je.getSQLException().toString() + "\r\n\r\n" + t.toString());
				}
				else
				{
					log.error("Error running the script: " + ((TargetError)t).getTarget());
					throw new QueryBuilderException("Error running the script !\r\n" + (((TargetError)t).getTarget()).toString());
				}
			}
			else
			{
				log.error("Error running the script: " + t);
				throw new QueryBuilderException("Error running the script !\r\n" + t.toString());
			}
		} 
		catch (ParseException e)
		{
			log.error("Error running the script: " + e);
			throw new QueryBuilderException("Error running the script !\r\n" + e.toString());
		} 
		catch (EvalError e)
		{
			log.error("Error running the script: " + e);
			throw new QueryBuilderException("Error running the script !\r\n" + e.toString());
		} 
		catch (Exception e)
		{
			log.error("Error running the script: " + e);
			throw new QueryBuilderException("Error running the script !\r\n" + e.toString());
		} 
		
		if(o instanceof String)
			return (String)o;
		else if(o instanceof byte[])
		{
			System.out.println("Compressed data size: " + ((byte[])o).length);
			return o;
		}
		
		return "";
	}
	
	private Date getModifiedTime(Date now, String field)
	{
		// if we have a param called SYS_DATE_TIME_PLUS_4_DAYS it will return 'now' + 4 days
		
		StringTokenizer st = new StringTokenizer(field, "_");
		
		int tokens = st.countTokens();
		
		if (tokens < 3)
			return now;
		
		String sign = "";
		String val = "";
		String interval = "";

		// we need the last 3 tokens
		int i = 0;
		while(st.hasMoreTokens())
		{
			if(i == tokens - 3)
			{
				sign = st.nextToken();
			}
			else if(i == tokens - 2)
			{
				val = st.nextToken();
			}
			else if(i == tokens - 1)
			{
				interval = st.nextToken();
			}
			else
				st.nextToken();
			
			i++;
		}
		
		int jInterval = 0;
		int jVal = 0;
		
		if("DAYS".equalsIgnoreCase(interval))
			jInterval = Calendar.DAY_OF_MONTH;
		else if("MONTHS".equalsIgnoreCase(interval))
			jInterval = Calendar.MONTH;
		else if("YEARS".equalsIgnoreCase(interval))
			jInterval = Calendar.YEAR;
		else if("HOURS".equalsIgnoreCase(interval))
			jInterval = Calendar.HOUR;
		else if("MINUTES".equalsIgnoreCase(interval))
			jInterval = Calendar.MINUTE;
		else if("SECONDS".equalsIgnoreCase(interval))
			jInterval = Calendar.SECOND;
		
		try
		{
			jVal = "PLUS".equalsIgnoreCase(sign) ? Integer.parseInt(val) : 0 - Integer.parseInt(val);
		}
		catch(Exception e)
		{
			
		}
		
		java.util.GregorianCalendar cal = new GregorianCalendar();		
		
		cal.setTime(now);
		
		cal.add(jInterval, jVal);
		
		//System.out.println(field + ": " + now.toString() + " -> " + cal.getTime().toGMTString());
		
		return cal.getTime();
	}

	public boolean writeToFile(String fileName, byte[] buffer)
	{
		if (System.getProperty("catalina.home") == null)
		{
			return false;				
		}
		
		String fullFileName = System.getProperty("catalina.home") + "/common/classes/" + fileName;				
		
		File newFile = new File(fullFileName);
		FileOutputStream fw;
		try
		{
			fw = new FileOutputStream(newFile);
			fw.write(buffer);
			fw.close();
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		catch (IOException e)
		{
			return false;
		}

		return true;
	}
}
