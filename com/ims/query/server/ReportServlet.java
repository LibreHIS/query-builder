/*
 * Created on Mar 23, 2005
 *
 */
package com.ims.query.server;

import ims.core.admin.domain.objects.ReportBo;
import ims.core.admin.domain.objects.ReportQuery;
import ims.core.admin.domain.objects.ReportSeedBo;
import ims.core.admin.domain.objects.ReportsCategory;
import ims.core.admin.domain.objects.TemplateBo;
import ims.framework.utils.DateTime;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.ims.query.builder.ReportTemplateManifest1;

/**
 * @author vpurdila
 *
 */
public class ReportServlet extends HttpServlet
{
	static final Logger log = Logger.getLogger(ReportServlet.class);

	//constants for flags
	static final int	CAN_EDIT_QUERY		= 0x01;
	static final int	CAN_EDIT_TEMPLATE	= 0x02;
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException
	{
		String[] val = null;
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/xml");
		
		val = request.getParameterValues("action");
		if(val != null)
		{
			if(val[0].equalsIgnoreCase("listReports"))
			{
				try
				{
					out.print(listReports(response));
				} 
				catch (HibernateException e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				} 
				catch (IOException e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				}
			}
			if(val[0].equalsIgnoreCase("listQueries"))
			{
				try
				{
					out.print(listQueries(response));
				} 
				catch (HibernateException e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				} 
				catch (IOException e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				}
			}
			if(val[0].equalsIgnoreCase("listCategoriesReportsTemplates"))
			{
				try
				{
					out.print(listCategoriesReportsTemplates(response));
				} 
				catch (HibernateException e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				} 
				catch (IOException e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				}
			}
			else if(val[0].equalsIgnoreCase("listCategories"))
			{
				try
				{
					out.print(listCategories(response));
				} 
				catch (HibernateException e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				} 
				catch (IOException e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				}
			}
			else if(val[0].equalsIgnoreCase("listTemplates"))
			{
				val = request.getParameterValues("reportID");
				
				if(val != null)
				{
					try
					{
						out.print(listTemplates(response, val[0]));
					} 
					catch (Exception e1)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					}
				}
				else
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "Bad input data, reportID not found.");
				}
			}
			else if(val[0].equalsIgnoreCase("getReport"))
			{
				val = request.getParameterValues("reportID");
				
				if(val != null)
				{
					try
					{
						out.print(getReport(val[0], request, response));
					} 
					catch (Exception e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					}
				}
			}
			else if(val[0].equalsIgnoreCase("getFullReport"))
			{
				val = request.getParameterValues("reportID");
				
				if(val != null)
				{
					try
					{
						out.print(getFullReport(val[0], request, response));
					} 
					catch (Exception e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					}
				}
			}
			else if(val[0].equalsIgnoreCase("putFullReport"))
			{
				String reportId = "";
				String reportXml = "";
				
				val = request.getParameterValues("reportID");
				if(val != null)
				{
					reportId = val[0];
				}
				val = request.getParameterValues("reportXml");
				if(val != null)
				{
					reportXml = val[0];
				}

				if(reportId == null || reportId.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report id was not passed down !");
					return;
				}

				if(reportXml == null || reportXml.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report xml project was not passed down !");
					return;
				}
				
				try
				{
					putFullReport(reportId, reportXml, request, response);
				} 
				catch (Exception e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				}
			}
			else if(val[0].equalsIgnoreCase("getReportCategory"))
			{
				val = request.getParameterValues("categoryID");
				
				if(val != null)
				{
					try
					{
						out.print(getReportCategory(val[0], request, response));
					} 
					catch (Exception e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					}
				}
			}
			else if(val[0].equalsIgnoreCase("getHqlQuery"))
			{
				val = request.getParameterValues("queryID");
				
				if(val != null)
				{
					try
					{
						out.print(getHqlQuery(val[0], request, response));
					} 
					catch (Exception e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					}
				}
			}
			else if(val[0].equalsIgnoreCase("changeReportParent"))
			{
				try
				{
					changeReportParent(request, response);
				} 
				catch (Exception e)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				}
			}
			else if(val[0].equalsIgnoreCase("getTemplate"))
			{
				val = request.getParameterValues("templateID");
				
				if(val != null)
				{
					try
					{
						out.print(getTemplate(val[0], request, response));
					} 
					catch (Exception e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					}
				}
			}
			else if(val[0].equalsIgnoreCase("insertReport"))
			{
				String reportName = "";
				String reportDescription = "";
				String reportXml = "";
				String seedsXml = "";
				String active = "";
				String parentId = "";
				
				val = request.getParameterValues("name");
				if(val != null)
					reportName = val[0];
				
				val = request.getParameterValues("description");
				if(val != null)
					reportDescription = val[0];

				val = request.getParameterValues("reportXml");
				if(val != null)
					reportXml = val[0];

				val = request.getParameterValues("active");
				if(val != null)
					active = val[0];

				val = request.getParameterValues("parentId");
				if(val != null)
					parentId = val[0];
				

				val = request.getParameterValues("seedsXml");
				if(val != null)
					seedsXml = val[0];
				
				if(reportName.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report name was not passed down !");
					return;
				}

				if(reportDescription.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report description was not passed down !");
					return;
				}

				if(reportXml.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report xml was not passed down !");
					return;
				}
				
				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				ReportBo report = new ReportBo();
				report.setReportName(reportName);
				report.setReportDescription(reportDescription);
				report.setReportXml(reportXml);
				report.setIsActive(active.equalsIgnoreCase("f") ? Boolean.FALSE : Boolean.TRUE);
				report.setQueryEditable(Boolean.TRUE);
				report.setTemplateEditable(Boolean.TRUE);
				
				HttpSession httpSession = request.getSession(true);
				java.util.Date now = new Date();
				report.setLastUpdated(now);
				report.getSystemInformation().setCreationDateTime(now);
				report.getSystemInformation().setCreationUser((String)httpSession.getAttribute("USER"));
				
				
				HashSet seeds = null;
				try
				{
					seeds = updateReportSeeds(report, seedsXml);
				} 
				catch (DocumentException e3)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e3.toString());
					return;
				}
				
				report.getSeeds().addAll(seeds);
				
				ReportsCategory newParentCategory = null;
				try
				{
					tx = session.beginTransaction();
						session.saveOrUpdate(report);
					
						if(parentId != null && parentId.length() > 0)
						{
							newParentCategory = (ReportsCategory)session.load(ReportsCategory.class, Integer.valueOf(parentId));

							if(newParentCategory.getReports() == null)
								newParentCategory.setReports(new HashSet());
							newParentCategory.getReports().add(report);
							
							session.saveOrUpdate(newParentCategory);
						}
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
				
				out.print(report.getId());
			}
			else if(val[0].equalsIgnoreCase("updateReport"))
			{
				String reportID = "";
				String reportName = "";
				String reportDescription = "";
				String reportXml = "";
				//String templateName = "";
				//String templateDescription = "";
				//String templateXml = "";
				String seedsXml = "";
				String active = "";

				val = request.getParameterValues("id");
				if(val != null)
					reportID = val[0];
				
				val = request.getParameterValues("name");
				if(val != null)
					reportName = val[0];
				
				val = request.getParameterValues("description");
				if(val != null)
					reportDescription = val[0];

				val = request.getParameterValues("reportXml");
				if(val != null)
					reportXml = val[0];

				val = request.getParameterValues("active");
				if(val != null)
					active = val[0];
				
				/*
				val = request.getParameterValues("templateName");
				if(val != null)
					templateName = val[0];
				
				val = request.getParameterValues("templateDescription");
				if(val != null)
					templateDescription = val[0];

				val = request.getParameterValues("templateXml");
				if(val != null)
					templateXml = val[0];
				*/

				val = request.getParameterValues("seedsXml");
				if(val != null)
					seedsXml = val[0];
				
				if(reportID.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report ID was not passed down !");
					return;
				}

				if(reportName.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report name was not passed down !");
					return;
				}
				
				if(reportDescription.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report description was not passed down !");
					return;
				}

				if(reportXml.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report xml was not passed down !");
					return;
				}
				
				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				HttpSession httpSession = request.getSession(true);
				
				if(log.isDebugEnabled())
				{
					log.debug("updateReport(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
				}
								
				ReportBo report = (ReportBo)httpSession.getAttribute("report");

				if(report == null)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The session has expired or getReport() was not called before updateReport().");
					return;
				}
				
				java.util.Date now = new Date();
				
				report.setReportName(reportName);
				report.setReportDescription(reportDescription);
				report.setReportXml(reportXml);
				report.setIsActive(active.equalsIgnoreCase("f") ? Boolean.FALSE : Boolean.TRUE);
				report.setLastUpdated(now);
				report.getSystemInformation().setLastUpdateDateTime(now);
				report.getSystemInformation().setLastUpdateUser((String)httpSession.getAttribute("USER"));
				
				//System.out.println("Update report: JSESSIONID=" + httpSession.getId() + ", user: " + httpSession.getAttribute("USER"));				
				
				HashSet seeds = null;
				try
				{
					seeds = updateReportSeeds(report, seedsXml);
				} 
				catch (DocumentException e3)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e3.toString());
					return;
				}
				
				try
				{
					tx = session.beginTransaction();
						//delete the old seeds first
						Iterator it = report.getSeeds().iterator();
						while(it.hasNext())
						{
							ReportSeedBo s = (ReportSeedBo)it.next();
							session.delete(s);
						}
						
						report.getSeeds().clear();
						report.getSeeds().addAll(seeds);
						session.saveOrUpdate(report);
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
			}
			else if(val[0].equalsIgnoreCase("insertQuery"))
			{
				String queryName = "";
				String queryDescription = "";
				String queryXml = "";
				String seedsXml = "";
				
				val = request.getParameterValues("name");
				if(val != null)
					queryName = val[0];
				
				val = request.getParameterValues("description");
				if(val != null)
					queryDescription = val[0];

				val = request.getParameterValues("queryXml");
				if(val != null)
					queryXml = val[0];

				val = request.getParameterValues("seedsXml");
				if(val != null)
					seedsXml = val[0];
				
				if(queryName.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The query name was not passed down !");
					return;
				}

				if(queryDescription.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The query description was not passed down !");
					return;
				}

				if(queryXml.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The query xml was not passed down !");
					return;
				}
				
				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				ReportQuery query = new ReportQuery();
				query.setName(queryName);
				query.setDescription(queryDescription);
				query.setQueryXML(queryXml);
				
				HashSet seeds = null;
				try
				{
					seeds = updateQuerySeeds(query, seedsXml);
				} 
				catch (DocumentException e3)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e3.toString());
					return;
				}
				
				query.getSeeds().addAll(seeds);
				
				try
				{
					tx = session.beginTransaction();
						session.saveOrUpdate(query);
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
				
				out.print(query.getId());
			}
			else if(val[0].equalsIgnoreCase("updateQuery"))
			{
				String queryID = "";
				String queryName = "";
				String queryDescription = "";
				String queryXml = "";
				String seedsXml = "";

				val = request.getParameterValues("id");
				if(val != null)
					queryID = val[0];
				
				val = request.getParameterValues("name");
				if(val != null)
					queryName = val[0];
				
				val = request.getParameterValues("description");
				if(val != null)
					queryDescription = val[0];

				val = request.getParameterValues("queryXml");
				if(val != null)
					queryXml = val[0];

				val = request.getParameterValues("seedsXml");
				if(val != null)
					seedsXml = val[0];
				
				if(queryID.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The query ID was not passed down !");
					return;
				}

				if(queryName.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The query name was not passed down !");
					return;
				}
				
				if(queryDescription.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The query description was not passed down !");
					return;
				}

				if(queryXml.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The query xml was not passed down !");
					return;
				}
				
				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				HttpSession httpSession = request.getSession(true);
				
				if(log.isDebugEnabled())
				{
					log.debug("updateQuery(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
				}
								
				ReportQuery query = (ReportQuery)httpSession.getAttribute("hql_query");

				if(query == null)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The session has expired or getHqlQuery() was not called before updateReport().");
					return;
				}
				
				query.setName(queryName);
				query.setDescription(queryDescription);
				query.setQueryXML(queryXml);
				
				HashSet seeds = null;
				try
				{
					seeds = updateQuerySeeds(query, seedsXml);
				} 
				catch (DocumentException e3)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e3.toString());
					return;
				}
				
				try
				{
					tx = session.beginTransaction();
						//delete the old seeds first
						Iterator it = query.getSeeds().iterator();
						while(it.hasNext())
						{
							ReportSeedBo s = (ReportSeedBo)it.next();
							session.delete(s);
						}
						
						query.getSeeds().clear();
						query.getSeeds().addAll(seeds);
						session.saveOrUpdate(query);
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
			}
			else if(val[0].equalsIgnoreCase("deleteQuery"))
			{
				String queryID = "";

				val = request.getParameterValues("id");
				if(val != null)
					queryID = val[0];
				
				if(queryID.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The query ID was not passed down !");
					return;
				}

				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				HttpSession httpSession = request.getSession(true);
				
				if(log.isDebugEnabled())
				{
					log.debug("deleteQuery(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
				}
								
				ReportQuery query = (ReportQuery)httpSession.getAttribute("hql_query");

				if(query == null)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The session has expired or getHqlQuery() was not called before updateReport().");
					return;
				}
				
				try
				{
					tx = session.beginTransaction();
						//delete the old seeds first
/*						Iterator it = query.getSeeds().iterator();
						while(it.hasNext())
						{
							ReportSeedBo s = (ReportSeedBo)it.next();
							session.delete(s);
						}
						
						query.getSeeds().clear();
						session.saveOrUpdate(query);
*/					
					session.delete(query);
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
			}
			else if(val[0].equalsIgnoreCase("insertCategory"))
			{
				String categoryName = "";
				String parentId = "";
				
				val = request.getParameterValues("name");
				if(val != null)
					categoryName = val[0];
				
				val = request.getParameterValues("parentId");
				if(val != null)
					parentId = val[0];

				if(categoryName.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The category name was not passed down !");
					return;
				}

				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				ReportsCategory parentCategory = null;
				ReportsCategory category = new ReportsCategory();
				category.setName(categoryName);

				try
				{
					tx = session.beginTransaction();
						if(parentId.length() > 0)
						{
							parentCategory = (ReportsCategory)session.load(ReportsCategory.class, Integer.valueOf(parentId));
							category.setParentCategory(parentCategory);
						}
					
						if(parentCategory == null)
							parentCategory = category;
						else
						{
							if(parentCategory.getSubCategories() == null)
								parentCategory.setSubCategories(new HashSet());
							
							parentCategory.getSubCategories().add(category);
						}
						
						session.saveOrUpdate(parentCategory);
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
				out.print(category.getId());
			}
			else if(val[0].equalsIgnoreCase("updateCategory"))
			{
				String categoryName = "";
				String categoryId = "";
				String newParentId = "";
				String oldParentId = "";
				
				val = request.getParameterValues("name");
				if(val != null)
					categoryName = val[0];
				
				val = request.getParameterValues("categoryId");
				if(val != null)
					categoryId = val[0];

				val = request.getParameterValues("parentId");
				if(val != null)
					newParentId = val[0];

				val = request.getParameterValues("oldParentId");
				if(val != null)
					oldParentId = val[0];
				
				if(categoryName.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The category name was not passed down !");
					return;
				}

				if(categoryId.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The category id was not passed down !");
					return;
				}
				
				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				ReportsCategory newParentCategory = null;
				ReportsCategory category = null;

				HttpSession httpSession = request.getSession(true);
				
				if(log.isDebugEnabled())
				{
					log.debug("updateCategory(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
				}
								
				category = (ReportsCategory)httpSession.getAttribute("reportCategory");
				
				if(category == null)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The session has expired or getReportCategory() was not called before updateCategory().");
					return;
				}
				
				category.setName(categoryName);

				try
				{
					tx = session.beginTransaction();

						if(newParentId.length() > 0 && !newParentId.equals(oldParentId))
						{
							newParentCategory = (ReportsCategory)session.load(ReportsCategory.class, Integer.valueOf(newParentId));

							if(newParentCategory.getSubCategories() == null)
								newParentCategory.setSubCategories(new HashSet());
							
							newParentCategory.getSubCategories().add(category);
							category.setParentCategory(newParentCategory);
						}
						else if(!newParentId.equals(oldParentId))
							category.setParentCategory(null);
						
						if(newParentCategory != null)
							session.saveOrUpdate(newParentCategory);
						else
							session.saveOrUpdate(category);
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
			}
			else if(val[0].equalsIgnoreCase("insertTemplate"))
			{
				String templateName = "";
				String templateDescription = "";
				String templateXml = "";
				String active = "";

				val = request.getParameterValues("templateName");
				if(val != null)
					templateName = val[0];
				
				val = request.getParameterValues("templateDescription");
				if(val != null)
					templateDescription = val[0];

				val = request.getParameterValues("templateXml");
				if(val != null)
					templateXml = val[0];

				val = request.getParameterValues("active");
				if(val != null)
					active = val[0];
				
				if(templateName.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The template name was not passed down !");
					return;
				}
				
				if(templateDescription.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The template description was not passed down !");
					return;
				}

				if(templateXml.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The template xml was not passed down !");
					return;
				}

				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				HttpSession httpSession = request.getSession(true);
				
				if(log.isDebugEnabled())
				{
					log.debug("insertTemplate(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
				}
				
				ReportBo report = (ReportBo)httpSession.getAttribute("report");

				if(report == null)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The session has expired or getReport() was not called before updateReport().");
					return;
				}

				TemplateBo template = new TemplateBo();
				
				template.setName(templateName);
				template.setDescription(templateDescription);
				template.setTemplateXml(templateXml);
				template.setIsActive(active.equalsIgnoreCase("f") ? Boolean.FALSE : Boolean.TRUE);
				template.setReport(report);

				java.util.Date now = new Date();
				template.setLastUpdated(now);
				template.getSystemInformation().setCreationDateTime(now);
				template.getSystemInformation().setCreationUser((String)httpSession.getAttribute("USER"));
				
				try
				{
					tx = session.beginTransaction();
						report.getTemplates().add(template);
						//session.saveOrUpdate(template);
						session.saveOrUpdate(report);
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
				
				out.print(template.getId());
			}
			else if(val[0].equalsIgnoreCase("updateTemplate"))
			{
				String templateID = "";
				String templateName = "";
				String templateDescription = "";
				String templateXml = "";
				String active = "";

				val = request.getParameterValues("id");
				if(val != null)
					templateID = val[0];
				
				val = request.getParameterValues("templateName");
				if(val != null)
					templateName = val[0];
				
				val = request.getParameterValues("templateDescription");
				if(val != null)
					templateDescription = val[0];

				val = request.getParameterValues("templateXml");
				if(val != null)
					templateXml = val[0];

				val = request.getParameterValues("active");
				if(val != null)
					active = val[0];

				if(templateID.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The template ID was not passed down !");
					return;
				}
				
				if(templateName.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The template name was not passed down !");
					return;
				}
				
				if(templateDescription.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The template description was not passed down !");
					return;
				}

				if(templateXml.length() == 0)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The template xml was not passed down !");
					return;
				}

				Transaction tx = null;
				Session session = null;
				try
				{
					session = HibernateUtil.currentSession();
				} 
				catch (HibernateException e1)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
					return;
				}

				HttpSession httpSession = request.getSession(true);
				
				if(log.isDebugEnabled())
				{
					log.debug("updateTemplate(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
				}
				
				ReportBo report = (ReportBo)httpSession.getAttribute("report");

				if(report == null)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The session has expired or getReport() was not called before updateReport().");
					return;
				}

				TemplateBo template = null;
				
				Iterator it = report.getTemplates().iterator();
				while(it.hasNext())
				{
					template = (TemplateBo)it.next();
					
					if(template.getId().intValue() == Integer.parseInt(templateID))
						break;
					else
						template = null;
				}

				if(template == null)
				{
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The Template was not found in the collection.");
					return;
				}
				
				template.setName(templateName);
				template.setDescription(templateDescription);
				template.setTemplateXml(templateXml);
				template.setIsActive(active.equalsIgnoreCase("f") ? Boolean.FALSE : Boolean.TRUE);
				
				java.util.Date now = new Date();
				template.setLastUpdated(now);
				template.getSystemInformation().setLastUpdateDateTime(now);
				template.getSystemInformation().setLastUpdateUser((String)httpSession.getAttribute("USER"));
				
				try
				{
					tx = session.beginTransaction();
						session.saveOrUpdate(template);
						session.saveOrUpdate(report);
					tx.commit();
				} 
				catch (HibernateException e)
				{
					if (tx != null)
					{
						try
						{
							tx.rollback();
						} 
						catch (HibernateException e2)
						{
						}
					}
					
					response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					return;
				}
				finally
				{
					try
					{
						HibernateUtil.closeSession();
					} 
					catch (HibernateException e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
						return;
					}
				}
			}
			else if(val[0].equalsIgnoreCase("verifyReport"))
			{
				val = request.getParameterValues("reportID");
				
				if(val != null)
				{
					try
					{
						out.print(verifyReport(val[0], request, response));
					} 
					catch (Exception e)
					{
						response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
					}
				}
			}
			
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private HashSet updateQuerySeeds(ReportQuery query, String seedsXml) throws DocumentException
	{
		Document maindoc = DocumentHelper.parseText(seedsXml);
		
		HashSet seeds = new HashSet();
		
		List list = maindoc.selectNodes("/seeds/seed");
		
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			ReportSeedBo seed = new ReportSeedBo();

			DefaultElement field = (DefaultElement) iter.next();
				
			seed.setBOName(field.valueOf("BOName"));
			seed.setBOFieldName(field.valueOf("BOField"));
			seed.setDataType(field.valueOf("Type"));
			seed.setCanBeNull(field.valueOf("CanBeNull").equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE);
			
			seeds.add(seed);
		}
		
		return seeds;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private HashSet updateReportSeeds(ReportBo report, String seedsXml) throws DocumentException
	{
		Document maindoc = DocumentHelper.parseText(seedsXml);
		
		HashSet seeds = new HashSet();
		
		//report.getSeeds().clear();
		
		List list = maindoc.selectNodes("/seeds/seed");
		
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			ReportSeedBo seed = new ReportSeedBo();

			DefaultElement field = (DefaultElement) iter.next();
				
			seed.setBOName(field.valueOf("BOName"));
			seed.setBOFieldName(field.valueOf("BOField"));
			seed.setDataType(field.valueOf("Type"));
			seed.setCanBeNull(field.valueOf("CanBeNull").equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE);
			
			seeds.add(seed);
			//report.getSeeds().add(seed);
		}
		
		return seeds;
	}

	private String getReport(String reportID, HttpServletRequest request, HttpServletResponse response) throws HibernateException
	{
		HttpSession httpSession = request.getSession(true);
		
		if(log.isDebugEnabled())
		{
			try
			{
				log.debug("getReport(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
		}
		
		ReportBo report = null;

		StringBuffer sb = new StringBuffer(16*1024);
		Transaction tx = null;
		//Iterator it = null;
		//Query q = null;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				report = (ReportBo)session.load(ReportBo.class, Integer.valueOf(reportID));
				report.getTemplates().size();
				report.getSeeds().size();
			tx.commit();
			
			sb.append("<reports>");
				sb.append("<report>");
					sb.append("<id>" + (report.getId() == null ? "" : report.getId().toString()) + "</id>");
					sb.append("<name>" + (report.getReportName() == null ? "" : StringUtils.encodeXML(report.getReportName().toString())) + "</name>");
					sb.append("<description>" + (report.getReportDescription() == null ? "" : StringUtils.encodeXML(report.getReportDescription().toString())) + "</description>");
					sb.append("<active>" + (report.isIsActive() == null ? "" : report.isIsActive().toString()) + "</active>");
					sb.append("<xml>" + (report.getReportXml() == null ? "" : StringUtils.encodeXML(report.getReportXml().toString())) + "</xml>");
				sb.append("</report>");
			sb.append("</reports>");
			
			httpSession.setAttribute("report", report);
		}
		finally
		{
			HibernateUtil.closeSession();
		}
		
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String getFullReport(String reportID, HttpServletRequest request, HttpServletResponse response) throws HibernateException
	{
		ReportBo report = null;

		StringBuffer sb = new StringBuffer(32*1024);
		Transaction tx = null;
		//Iterator it = null;
		//Query q = null;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			Iterator it = null;
			tx = session.beginTransaction();
				report = (ReportBo)session.load(ReportBo.class, Integer.valueOf(reportID));
				report.getTemplates().size();
				report.getSeeds().size();
				
				Query q = session.createQuery("select f1_1.form.id from FormReportBo as f1_1 where (f1_1.report.id = :RID and f1_1.form is not null)");
				q.setInteger("RID", Integer.parseInt(reportID));
				it = q.list().iterator();
			tx.commit();
			
			sb.append("<fullreport>");
				sb.append("<name>" + (report.getReportName() == null ? "" : StringUtils.encodeXML(report.getReportName().toString())) + "</name>");
				sb.append("<description>" + (report.getReportDescription() == null ? "" : StringUtils.encodeXML(report.getReportDescription().toString())) + "</description>");
				sb.append("<file>" + (report.getExportFileName() == null ? "" : StringUtils.encodeXML(report.getExportFileName())) + "</file>");
				sb.append("<imsid>" + (report.getImsId() == null ? "" : StringUtils.encodeXML(report.getImsId().toString())) + "</imsid>");
				sb.append("<isactive>" + (report.isIsActive() == null ? "" : report.isIsActive().toString()) + "</isactive>");
				sb.append("<reportxml>" + (report.getReportXml() == null ? "" : StringUtils.encodeXML(report.getReportXml().toString())) + "</reportxml>");
			
				sb.append("<templates>");
					for (Iterator iter = report.getTemplates().iterator(); iter.hasNext();)
					{
						TemplateBo template = (TemplateBo) iter.next();
	
						sb.append("<template>");
							sb.append("<name>" + (template.getName() == null ? "" : StringUtils.encodeXML(template.getName())) + "</name>");
							sb.append("<description>" + (template.getDescription() == null ? "" : StringUtils.encodeXML(template.getDescription())) + "</description>");
							sb.append("<isactive>" + (template.isIsActive() == null ? "" : template.isIsActive().toString()) + "</isactive>");
							sb.append("<templatexml>" + (template.getTemplateXml() == null ? "" : StringUtils.encodeXML(template.getTemplateXml())) + "</templatexml>");
						sb.append("</template>");
					}
				sb.append("</templates>");

				sb.append("<seeds>");
					for (Iterator iter = report.getSeeds().iterator(); iter.hasNext();)
					{
						ReportSeedBo seed = (ReportSeedBo) iter.next();
	
						sb.append("<seed>");
							sb.append("<boname>" + (seed.getBOName() == null ? "" : StringUtils.encodeXML(seed.getBOName())) + "</boname>");
							sb.append("<bofieldname>" + (seed.getBOFieldName() == null ? "" : StringUtils.encodeXML(seed.getBOFieldName())) + "</bofieldname>");
							sb.append("<datatype>" + (seed.getDataType() == null ? "" : seed.getDataType().toString()) + "</datatype>");
							sb.append("<canbenull><![CDATA[" + (seed.isCanBeNull() == null ? "" : seed.isCanBeNull().toString()) + "]]></canbenull>");
						sb.append("</seed>");
					}
				sb.append("</seeds>");

				sb.append("<forms>");
					while ( it.hasNext() ) 
					{
						Integer formId = (Integer) it.next();
						sb.append("<form>" + (formId == null ? "" : formId.toString()) + "</form>");
					}
				sb.append("</forms>");
				
			sb.append("</fullreport>");
		}
		finally
		{
			HibernateUtil.closeSession();
		}
		
		return sb.toString();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void putFullReport(String reportID, String reportXml, HttpServletRequest request, HttpServletResponse response) throws HibernateException, DocumentException
	{
		//WDEV-16060
		StringBuilder sb = new StringBuilder();
		DateTime now = new DateTime();
		ReportTemplateManifest1 manifest = new ReportTemplateManifest1();
		boolean deployedQuery = false;
		boolean deployedTemplate = false;
		LogDeployReportHelper logHelper = new LogDeployReportHelper();
		HttpSession httpSession = request.getSession(true);
		
		ReportBo reportFromXml = DeserializeReportFromXml(reportXml);

		Transaction tx = null;
		
		Session session = HibernateUtil.currentSession();
		
		try
		{
			tx = session.beginTransaction();
			ReportBo report = (ReportBo)session.load(ReportBo.class, Integer.valueOf(reportID));

				if(Boolean.FALSE.equals(report.isQueryEditable()) || report.getImsId() == null)
					report.getSeeds().clear();
				
				//WDEV-12252
				//if(Boolean.FALSE.equals(report.isTemplateEditable()) || report.getImsId() == null)
					//report.getTemplates().clear();
				
				session.save(report);
				session.flush();
				
				//WDEV-16060
				sb.append("Report deployment: " + report.getReportName());
				sb.append("\n----------------------------------------------------------------------\n");
				sb.append("Deployment type: QueryBuilder");
				sb.append("\n");
				sb.append("Deployment user: " + httpSession.getAttribute("USER"));
				sb.append("\n");
				sb.append("Remote host: " + request.getRemoteAddr());
				sb.append("\n");
				sb.append("Report's IMS ID: " + report.getImsId() + "");
				sb.append("\n");
				sb.append("Original report manifest: ");
				manifest.extractReportInfoFromXml(report.getReportXml(), sb);
				
				report.setReportName(reportFromXml.getReportName());
				report.setReportDescription(reportFromXml.getReportDescription());
				
				if(report.getReportXml() == null || Boolean.FALSE.equals(report.isQueryEditable()) || report.getImsId() == null)
				{
					report.setReportXml(reportFromXml.getReportXml());
					report.getSeeds().addAll(reportFromXml.getSeeds());
					report.setIsActive(reportFromXml.isIsActive());
					
					deployedQuery = true;
					
					sb.append("Deployed report manifest: ");
					manifest.extractReportInfoFromXml(reportFromXml.getReportXml(), sb);
				}

				if(report.getTemplates().size() == 0)
				{
					report.getTemplates().addAll(reportFromXml.getTemplates());
					
					deployedTemplate = true;
					
					Iterator itFromFile = reportFromXml.getTemplates().iterator();
					while(itFromFile.hasNext())
					{
						TemplateBo tFile = (TemplateBo)itFromFile.next();
					
    					sb.append("\nDeployed template (new template added from file): " + tFile.getName() + "");
    					sb.append("\n----------------------------------------------------------------------\n");
    					sb.append("Deployed template type: " + manifest.getTemplateType(tFile.getTemplateXml()) + "");
    					sb.append("\n");
    					sb.append("Deployed template manifest: " + manifest.extractTemplateInfoFromXml(tFile.getTemplateXml()));
    					
						tFile.getSystemInformation().setCreationDateTime(now.getJavaDate());
						tFile.getSystemInformation().setCreationUser((String)httpSession.getAttribute("USER"));
					}					
				}
				else if(Boolean.FALSE.equals(report.isTemplateEditable()) || report.getImsId() == null)
				{
					//WDEV-12252 - we try to match the template names, if they match we do an update
					//otherwise we just add the template from .REP file to the report's template collection
					Iterator itFromFile = reportFromXml.getTemplates().iterator();
					while(itFromFile.hasNext())
					{
						TemplateBo tFile = (TemplateBo)itFromFile.next();

						boolean found = false;
						Iterator itDom = report.getTemplates().iterator();
						while(itDom.hasNext())
						{
							TemplateBo tDom = (TemplateBo)itDom.next();
							
							String origTemplateType = manifest.getTemplateType(tDom.getTemplateXml());
							String origTemplateManifest = manifest.extractTemplateInfoFromXml(tDom.getTemplateXml());
							
							if(tDom.getName() != null && tDom.getName().equalsIgnoreCase(tFile.getName()))
							{
								//template names do match, we do an update
								tDom.setReport(report);
								tDom.setDescription(tFile.getDescription());
								tDom.setIsActive(tFile.isIsActive());
								tDom.setName(tFile.getName());
								tDom.setTemplateXml(tFile.getTemplateXml());
								
								tDom.setLastDeployment(now.getJavaDate());
								tDom.setLastUpdated(now.getJavaDate());
								tDom.getSystemInformation().setLastUpdateDateTime(now.getJavaDate());
								tDom.getSystemInformation().setLastUpdateUser((String)httpSession.getAttribute("USER"));
								
								found = true;
								deployedTemplate = true;
								
								sb.append("\nDeployed template: " + tFile.getName() + "");
								sb.append("\n----------------------------------------------------------------------\n");
								sb.append("Original template type: " + origTemplateType);
								sb.append("\n");
								sb.append("Original template manifest: " + origTemplateManifest);
								sb.append("Deployed template type: " + manifest.getTemplateType(tFile.getTemplateXml()) + "");
								sb.append("\n");
								sb.append("Deployed template manifest: " + manifest.extractTemplateInfoFromXml(tFile.getTemplateXml()));
								
								break;
							}
						}

						if(!found)
						{
							//template names don't match, we do an insert
							report.getTemplates().add(tFile);
							
							tFile.getSystemInformation().setCreationDateTime(now.getJavaDate());
							tFile.getSystemInformation().setCreationUser((String)httpSession.getAttribute("USER"));
							
							deployedTemplate = true;
							
							sb.append("\nDeployed template (new template added from file): " + tFile.getName() + "");
							sb.append("\n----------------------------------------------------------------------\n");
							sb.append("Deployed template type: " + manifest.getTemplateType(tFile.getTemplateXml()) + "");
							sb.append("\n");
							sb.append("Deployed template manifest: " + manifest.extractTemplateInfoFromXml(tFile.getTemplateXml()));
						}
					}
					
				}
				
				Iterator it = report.getTemplates().iterator();
				while(it.hasNext())
				{
					TemplateBo t = (TemplateBo)it.next();
					t.setReport(report);
					
					if(deployedTemplate)
					{
						t.setLastDeployment(now.getJavaDate());
						t.setLastUpdated(now.getJavaDate());
					}
				}
				
				/*
			
				Iterator it = report.getSeeds().iterator();
				//report.setSeeds(null);
				while(it.hasNext())
				{
					ReportSeedBo s = (ReportSeedBo)it.next();
					session.delete(s);
					report.getSeeds().remove(s);
				}

				it = report.getTemplates().iterator();
				report.setTemplates(null);
				while(it.hasNext())
				{
					TemplateBo t = (TemplateBo)it.next();
					session.delete(t);
				}
				session.flush();

				report.setTemplates(new HashSet());
				
				report.setReportName(reportFromXml.getReportName());
				report.setReportDescription(reportFromXml.getReportDescription());
				report.setReportXml(reportFromXml.getReportXml());
				report.setIsActive(reportFromXml.isIsActive());

				it = reportFromXml.getSeeds().iterator();
				while(it.hasNext())
				{
					ReportSeedBo s = (ReportSeedBo)it.next();
					report.getSeeds().add(s);
				}

				it = reportFromXml.getTemplates().iterator();
				while(it.hasNext())
				{
					TemplateBo t = (TemplateBo)it.next();
					t.setReport(report);
					report.getTemplates().add(t);
				}
				*/
				
				if(deployedQuery)
				{
					report.setLastDeployment(now.getJavaDate());
					report.setLastUpdated(now.getJavaDate());
					
					report.getSystemInformation().setLastUpdateDateTime(now.getJavaDate());
					report.getSystemInformation().setLastUpdateUser((String)httpSession.getAttribute("USER"));
				}
				
				session.saveOrUpdate(report);
				
				if(deployedQuery || deployedTemplate)
				{
					logHelper.createSystemLogEntry(sb.toString(), true);					
				}
				
			tx.commit();
		}
		finally
		{
			HibernateUtil.closeSession();
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private ReportBo DeserializeReportFromXml(String reportXml) throws DocumentException
	{
		ReportBo report = new ReportBo();
		
		Document maindoc = getXmlDocument(reportXml);

		//parse seeds
		Node root = maindoc.selectSingleNode("fullreport");

		report.setReportName(root.valueOf("name"));
		report.setReportDescription(root.valueOf("description"));
		if(root.valueOf("isactive") != null && root.valueOf("isactive").length() > 0)
			report.setIsActive("true".equalsIgnoreCase(root.valueOf("isactive")) ? Boolean.TRUE : Boolean.FALSE);
		report.setReportXml(root.valueOf("reportxml"));
		
		report.setTemplates(new HashSet());
		report.setSeeds(new HashSet());
		
		List templates = maindoc.selectNodes("fullreport/templates/template");
		for (Iterator iter = templates.iterator(); iter.hasNext();)
		{
			TemplateBo template = new TemplateBo();
			
			DefaultElement element = (DefaultElement) iter.next();

			template.setName(element.valueOf("name"));
			template.setDescription(element.valueOf("description"));
			if(element.valueOf("isactive") != null && element.valueOf("isactive").length() > 0)
				template.setIsActive("true".equalsIgnoreCase(element.valueOf("isactive")) ? Boolean.TRUE : Boolean.FALSE);
			template.setTemplateXml(element.valueOf("templatexml"));
			
			report.getTemplates().add(template);
		}

		List seeds = maindoc.selectNodes("fullreport/seeds/seed");
		for (Iterator iter = seeds.iterator(); iter.hasNext();)
		{
			ReportSeedBo seed = new ReportSeedBo();
			
			DefaultElement element = (DefaultElement) iter.next();

			seed.setBOName(element.valueOf("boname"));
			seed.setBOFieldName(element.valueOf("bofieldname"));
			seed.setDataType(element.valueOf("datatype"));
			if(element.valueOf("canbenull") != null && element.valueOf("canbenull").length() > 0)
				seed.setCanBeNull("true".equalsIgnoreCase(element.valueOf("canbenull")) ? Boolean.TRUE : Boolean.FALSE);
			
			report.getSeeds().add(seed);
		}

		return report;
	}

	private String getHqlQuery(String queryID, HttpServletRequest request, HttpServletResponse response) throws HibernateException
	{
		HttpSession httpSession = request.getSession(true);
		
		if(log.isDebugEnabled())
		{
			try
			{
				log.debug("getHqlQuery(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
		}
		
		ReportQuery query = null;

		StringBuffer sb = new StringBuffer(16*1024);
		Transaction tx = null;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				query = (ReportQuery)session.load(ReportQuery.class, Integer.valueOf(queryID));
				query.getSeeds().size();
			tx.commit();
			
			sb.append("<queries>");
				sb.append("<query>");
					sb.append("<id>" + (query.getId() == null ? "" : query.getId().toString()) + "</id>");
					sb.append("<name>" + (query.getName() == null ? "" : StringUtils.encodeXML(query.getName().toString())) + "</name>");
					sb.append("<description>" + (query.getDescription() == null ? "" : StringUtils.encodeXML(query.getDescription().toString())) + "</description>");
					sb.append("<xml>" + (query.getQueryXML() == null ? "" : StringUtils.encodeXML(query.getQueryXML().toString())) + "</xml>");
				sb.append("</query>");
			sb.append("</queries>");
			
			httpSession.setAttribute("hql_query", query);
		}
		finally
		{
			HibernateUtil.closeSession();
		}
		
		return sb.toString();
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException
	{
		doGet(request, response);
	}
	
	@SuppressWarnings("rawtypes")
	private String listTemplates(HttpServletResponse response, String reportID) throws IOException, HibernateException
	{
		StringBuffer sb = new StringBuffer(1000);
		Transaction tx = null;
		Iterator it = null;
		Query q = null;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				q = session.createQuery("select r1_1.id, t1_1.id, t1_1.name, t1_1.description, t1_1.isActive from ReportBo as r1_1 left join r1_1.templates as t1_1 where (r1_1.id = :RID) order by t1_1.name asc");
				q.setInteger("RID", Integer.parseInt(reportID));
				it = q.list().iterator();
			tx.commit();
			
			sb.append("<templates>");
			while ( it.hasNext() ) 
			{
				Object[] row = (Object[]) it.next();
				
				sb.append("<template>");
					sb.append("<id>" + (row[1] == null ? "" : row[1].toString()) + "</id>");
					sb.append("<name>" + (row[2] == null ? "" : StringUtils.encodeXML(row[2].toString())) + "</name>");
					sb.append("<description>" + (row[3] == null ? "" : StringUtils.encodeXML(row[3].toString())) + "</description>");
					sb.append("<active>" + (row[4] == null ? "" : row[4].toString()) + "</active>");
				sb.append("</template>");
			}	
			sb.append("</templates>");
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			} 
			catch (HibernateException e)
			{
				response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
			}
		}
		
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String listCategories(HttpServletResponse response) throws IOException, HibernateException
	{
		StringBuffer sb = new StringBuffer(1000);
		Transaction tx = null;
		Iterator it = null;
		Query q = null;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				q = session.createQuery("select r1_1.id, r1_1.name, r1_1.parentCategory.id from ReportsCategory as r1_1 where r1_1.isRIE is null order by r1_1.name asc");
				it = q.list().iterator();
			tx.commit();
			
			sb.append("<categories>");
			while ( it.hasNext() ) 
			{
				Object[] row = (Object[]) it.next();
				
				sb.append("<category>");
					sb.append("<id>" + (row[0] == null ? "" : row[0].toString()) + "</id>");
					sb.append("<name>" + (row[1] == null ? "" : StringUtils.encodeXML(row[1].toString())) + "</name>");
					sb.append("<parentId>" + (row[2] == null ? "" : row[2].toString()) + "</parentId>");
				sb.append("</category>");
			}	
			sb.append("</categories>");
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			} 
			catch (HibernateException e)
			{
				response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
			}
		}
		
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private String listReports(HttpServletResponse response) throws IOException, HibernateException
	{
		StringBuffer sb = new StringBuffer(1000);
		Transaction tx = null;
		Iterator it = null;
		Query q = null;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				q = session.createQuery("select r1_1.id, r1_1.reportName, r1_1.reportDescription, r1_1.isActive from ReportBo as r1_1 order by r1_1.reportName asc");
				it = q.list().iterator();
			tx.commit();
			
			sb.append("<reports>");
			while ( it.hasNext() ) 
			{
				Object[] row = (Object[]) it.next();
				
				sb.append("<report>");
					sb.append("<id>" + (row[0] == null ? "" : row[0].toString()) + "</id>");
					sb.append("<name>" + (row[1] == null ? "" : StringUtils.encodeXML(row[1].toString())) + "</name>");
					sb.append("<description>" + (row[2] == null ? "" : StringUtils.encodeXML(row[2].toString())) + "</description>");
					sb.append("<active>" + (row[3] == null ? "" : row[3].toString()) + "</active>");
				sb.append("</report>");
			}	
			sb.append("</reports>");
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			} 
			catch (HibernateException e)
			{
				response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
			}
		}
		
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String listQueries(HttpServletResponse response) throws IOException, HibernateException
	{
		StringBuffer sb = new StringBuffer(1000);
		Transaction tx = null;
		Iterator it = null;
		Query q = null;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				q = session.createQuery("select r1_1.id, r1_1.name, r1_1.description from ReportQuery as r1_1 order by r1_1.name asc");
				it = q.list().iterator();
			tx.commit();
			
			sb.append("<queries>");
			while ( it.hasNext() ) 
			{
				Object[] row = (Object[]) it.next();
				
				sb.append("<query>");
					sb.append("<id>" + (row[0] == null ? "" : row[0].toString()) + "</id>");
					sb.append("<name>" + (row[1] == null ? "" : StringUtils.encodeXML(row[1].toString())) + "</name>");
					sb.append("<description>" + (row[2] == null ? "" : StringUtils.encodeXML(row[2].toString())) + "</description>");
				sb.append("</query>");
			}	
			sb.append("</queries>");
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			} 
			catch (HibernateException e)
			{
				response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
			}
		}
		
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private String listCategoriesReportsTemplates(HttpServletResponse response) throws IOException, HibernateException
	{
		StringBuffer sb = new StringBuffer(1000);
		Transaction tx = null;
		Iterator it = null;
		Query q = null;
		Iterator it0 = null;
		Query q0 = null;
		int flags;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				q0 = session.createQuery("select r1_1.id, r2_1.id, r2_1.reportName, r2_1.reportDescription, r2_1.isActive, r2_1.imsId, r2_1.exportFileName, t1_1.id, t1_1.name, t1_1.description, t1_1.isActive, r2_1.queryEditable, r2_1.templateEditable from ReportsCategory as r1_1 left join r1_1.reports as r2_1 left join r2_1.templates as t1_1	where (r2_1.id is not null ) and r1_1.isRIE is null	order by r2_1.reportName asc, t1_1.name asc");
				it0 = q0.list().iterator();
			
				q = session.createQuery("select r1_1.id, r1_1.reportName, r1_1.reportDescription, r1_1.isActive, r1_1.imsId, r1_1.exportFileName, t1_1.id, t1_1.name, t1_1.description, t1_1.isActive, r1_1.queryEditable, r1_1.templateEditable from ReportBo as r1_1 left join r1_1.templates as t1_1 where (r1_1.id not in (select r2_1.id from ReportsCategory as r3_1 join r3_1.reports as r2_1 where (r2_1.id is not null ))) order by r1_1.reportName asc, t1_1.name asc");
				it = q.list().iterator();
			tx.commit();
			
			sb.append("<items>");
				while ( it0.hasNext() ) 
				{
					Object[] row = (Object[]) it0.next();
					
					flags = 0;
					
					//WDEV-8502
					if(Boolean.TRUE.equals(row[11]))
						flags |= CAN_EDIT_QUERY;
					if(Boolean.TRUE.equals(row[12]))
						flags |= CAN_EDIT_TEMPLATE;
					
					sb.append("<i>");
						sb.append("<c>" + (row[0] == null ? "" : row[0].toString()) + "</c>");
						sb.append("<r1>" + (row[1] == null ? "" : row[1].toString()) + "</r1>");
						sb.append("<r2>" + (row[2] == null ? "" : StringUtils.encodeXML(row[2].toString())) + "</r2>");
						sb.append("<r3>" + (row[3] == null ? "" : StringUtils.encodeXML(row[3].toString())) + "</r3>");
						sb.append("<r4>" + (row[4] == null ? "" : ((((Boolean)(row[4])).booleanValue() ? "t" : "f"))) + "</r4>");
						sb.append("<r5>" + (row[5] == null ? "" : StringUtils.encodeXML(row[5].toString())) + "</r5>");
						sb.append("<r6>" + (row[6] == null ? "" : StringUtils.encodeXML(row[6].toString())) + "</r6>");
						sb.append("<t1>" + (row[7] == null ? "" : row[7].toString()) + "</t1>");
						sb.append("<t2>" + (row[8] == null ? "" : StringUtils.encodeXML(row[8].toString())) + "</t2>");
						sb.append("<t3>" + (row[9] == null ? "" : StringUtils.encodeXML(row[9].toString())) + "</t3>");
						sb.append("<t4>" + (row[10] == null ? "" : ((((Boolean)(row[10])).booleanValue() ? "t" : "f"))) + "</t4>");
						sb.append("<f>" + flags + "</f>");
					sb.append("</i>");
				}	
				while ( it.hasNext() ) 
				{
					Object[] row = (Object[]) it.next();
					
					flags = 0;
					
					//WDEV-8502
					if(Boolean.TRUE.equals(row[10]))
						flags |= CAN_EDIT_QUERY;
					if(Boolean.TRUE.equals(row[11]))
						flags |= CAN_EDIT_TEMPLATE;
					
					sb.append("<i>");
						sb.append("<c></c>");
						sb.append("<r1>" + (row[0] == null ? "" : row[0].toString()) + "</r1>");
						sb.append("<r2>" + (row[1] == null ? "" : StringUtils.encodeXML(row[1].toString())) + "</r2>");
						sb.append("<r3>" + (row[2] == null ? "" : StringUtils.encodeXML(row[2].toString())) + "</r3>");
						sb.append("<r4>" + (row[3] == null ? "" : ((((Boolean)(row[3])).booleanValue() ? "t" : "f"))) + "</r4>");
						sb.append("<r5>" + (row[4] == null ? "" : StringUtils.encodeXML(row[4].toString())) + "</r5>");
						sb.append("<r6>" + (row[5] == null ? "" : StringUtils.encodeXML(row[5].toString())) + "</r6>");
						sb.append("<t1>" + (row[6] == null ? "" : row[6].toString()) + "</t1>");
						sb.append("<t2>" + (row[7] == null ? "" : StringUtils.encodeXML(row[7].toString())) + "</t2>");
						sb.append("<t3>" + (row[8] == null ? "" : StringUtils.encodeXML(row[8].toString())) + "</t3>");
						sb.append("<t4>" + (row[9] == null ? "" : ((((Boolean)(row[9])).booleanValue() ? "t" : "f"))) + "</t4>");
						sb.append("<f>" + flags + "</f>");
					sb.append("</i>");
				}	
			sb.append("</items>");
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			} 
			catch (HibernateException e)
			{
				response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
			}
		}
		
		return sb.toString();
	}
	
	
	@SuppressWarnings("rawtypes")
	private String getTemplate(String templateID, HttpServletRequest request, HttpServletResponse response) throws HibernateException, IOException
	{
		HttpSession httpSession = request.getSession(true);
		
		if(log.isDebugEnabled())
		{
			log.debug("getTemplate(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
		}
		
		ReportBo report = (ReportBo)httpSession.getAttribute("report");

		if(report == null)
		{
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The session has expired or getReport() was not called before getTemplate().");
			return "";
		}

		StringBuffer sb = new StringBuffer(16*1024);
		Transaction tx = null;
		Iterator it = null;
		//Query q = null;
		
		sb.append("<?xml version=\"1.0\"?>");
		
		Session session = HibernateUtil.currentSession();
		TemplateBo template = null;
	
		try
		{
			tx = session.beginTransaction();
				it = report.getTemplates().iterator();
				
				while(it.hasNext())
				{
					template = (TemplateBo)it.next();
					
					if(template.getId().intValue() == Integer.parseInt(templateID))
						break;

					template = null;
				}
			tx.commit();
			
			sb.append("<templates>");
				sb.append("<template>");
					sb.append("<id>" + (template.getId() == null ? "" : template.getId().toString()) + "</id>");
					sb.append("<name>" + (template.getName() == null ? "" : StringUtils.encodeXML(template.getName().toString())) + "</name>");
					sb.append("<description>" + (template.getDescription() == null ? "" : StringUtils.encodeXML(template.getDescription().toString())) + "</description>");
					sb.append("<active>" + (template.isIsActive() == null ? "" : template.isIsActive().toString()) + "</active>");
					sb.append("<xml>" + (template.getTemplateXml() == null ? "" : StringUtils.encodeXML(template.getTemplateXml().toString())) + "</xml>");
				sb.append("</template>");
			sb.append("</templates>");
		}
		finally
		{
			HibernateUtil.closeSession();
		}
		
		return sb.toString();
	}

	private String getReportCategory(String reportID, HttpServletRequest request, HttpServletResponse response) throws HibernateException
	{
		HttpSession httpSession = request.getSession(true);
		
		if(log.isDebugEnabled())
		{
			try
			{
				log.debug("getReportCategory(): JSESSIONID=" + httpSession.getId() + " >> called on server: " + InetAddress.getLocalHost().getHostName() + " at " + System.getProperty("catalina.home"));
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
		}
		
		ReportsCategory reportCategory = null;
		Transaction tx = null;
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				reportCategory = (ReportsCategory)session.load(ReportsCategory.class, Integer.valueOf(reportID));
				reportCategory.getReports().size();
				reportCategory.getSubCategories().size();
			tx.commit();
			
			httpSession.setAttribute("reportCategory", reportCategory);
		}
		finally
		{
			HibernateUtil.closeSession();
		}
		
		return "OK";
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void changeReportParent(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String reportId = "";
		String oldParentId = "";
		String newParentId = "";

		String[] val = request.getParameterValues("reportId");
		if(val != null)
			reportId = val[0];
		
		val = request.getParameterValues("oldParentId");
		if(val != null)
			oldParentId = val[0];

		val = request.getParameterValues("newParentId");
		if(val != null)
			newParentId = val[0];
		
		if(reportId.length() == 0)
		{
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The report id was not passed down !");
			return;
		}
		
		if(oldParentId != null && oldParentId.equals(newParentId))
			return;
		
		Transaction tx = null;
		Session session = null;
		try
		{
			session = HibernateUtil.currentSession();
		} 
		catch (HibernateException e1)
		{
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e1.toString());
			return;
		}
		
		ReportsCategory oldParentCategory = null;
		ReportsCategory newParentCategory = null;
		ReportBo report = null;

		try
		{
			tx = session.beginTransaction();
				report = (ReportBo)session.load(ReportBo.class, Integer.valueOf(reportId));
				
				if(oldParentId.length() > 0)
				{
					oldParentCategory = (ReportsCategory)session.load(ReportsCategory.class, Integer.valueOf(oldParentId));
					oldParentCategory.getReports().remove(report);
					
					session.saveOrUpdate(oldParentCategory);
				}
				if(newParentId.length() > 0)
				{
					newParentCategory = (ReportsCategory)session.load(ReportsCategory.class, Integer.valueOf(newParentId));
					
					if(newParentCategory.getReports() == null)
						newParentCategory.setReports(new HashSet());
					
					newParentCategory.getReports().add(report);
					
					session.saveOrUpdate(newParentCategory);
				}
			tx.commit();
		} 
		catch (HibernateException e)
		{
			if (tx != null)
			{
				try
				{
					tx.rollback();
				} 
				catch (HibernateException e2)
				{
				}
			}
			
			response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
			return;
		}
		finally
		{
			try
			{
				HibernateUtil.closeSession();
			} 
			catch (HibernateException e)
			{
				response.sendError(HttpServletResponse.	SC_INTERNAL_SERVER_ERROR , "The server has thrown the following error: " + e.toString());
				return;
			}
		}
	}
	
	private String verifyReport(String id, HttpServletRequest request, HttpServletResponse response) throws DocumentException
	{
		ReportBo report = null;
		Transaction tx = null;
		
		String reportXml = null;
		
		Session session = HibernateUtil.currentSession();
	
		try
		{
			tx = session.beginTransaction();
				report = (ReportBo)session.load(ReportBo.class, Integer.valueOf(id));
				reportXml = report.getReportXml();
			tx.commit();
		}
		finally
		{
			HibernateUtil.closeSession();
		}
		
		return validateReport(reportXml);
	}
	

	@SuppressWarnings({"unchecked", "rawtypes"})
	private String validateReport(String xmlReport) throws DocumentException
	{
		String queryName = "";
		ArrayList queryParams = new ArrayList();
		String hql = "";
		boolean isNative = false;
		
		// parse xml project
		Document maindoc = getXmlDocument(xmlReport);

		List list = maindoc.selectNodes("/Project/Queries/HQL");
		
		for (Iterator iter = list.iterator(); iter.hasNext();)
		{
			DefaultElement query = (DefaultElement) iter.next();

			Node node = query.selectSingleNode("Content");
			String content = node.getStringValue();
			
			Document doc = getXmlDocument(content);
			
			node = doc.selectSingleNode("Query/Name");
			queryName = node.getStringValue();
			
			node = doc.selectSingleNode("Query/Native");
			if(node != null && node.getStringValue() != null && node.getStringValue().length() > 0)
			{
				isNative = node.getStringValue().toLowerCase().equals("true");
			}
			
			queryParams.clear();

			node = doc.selectSingleNode("Query/HibernateQuery");
			hql = this.replaceNewLineSeparators(node.getStringValue());
			
			List listParams = doc.selectNodes("/Query/Params/Param");
			for (Iterator iter_params = listParams.iterator(); iter_params.hasNext();)
			{
				DefaultElement field = (DefaultElement) iter_params.next();
				
				QueryParam param = new QueryParam();
				param.setType(field.valueOf("@nFieldType"));
				param.setField(field.valueOf("@asField"));
				
				if(hasNamedParam(hql, param.getField()))
					queryParams.add(param);
			}
			
			
			//run HQL or SQL query
			String[] aParams = new String[queryParams.size()];
			Object[] aValues = new Object[queryParams.size()];

			for (int i = 0; i < queryParams.size(); i++)
			{
				aParams[i] = ((QueryParam)queryParams.get(i)).getField();
				aValues[i] = getDummyParamValue(((QueryParam)queryParams.get(i)).getType());
			}
			
			try
			{
				if(isNative)
					validateFindSQL(hql, aParams, aValues);
				else
					validateFind(hql, aParams, aValues);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return "Executing query '" + queryName + "' has thrown the following exception: \r\n" + getStackTrace(e);
			}
		}			
		
		return "OK";
	}
	
	public static String replaceNewLineSeparators(String val)
	{
		StringBuffer sb = new StringBuffer(val.length());
		char c;
		
		for(int i = 0; i < val.length(); i++)
		{
			c = val.charAt(i);
			
			if(c == '\r' || c == '\n')
				c = ' ';
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	private boolean hasNamedParam(String hql, String param)
	{
		if(hql == null || hql.length() == 0 || param == null || param.length() == 0)
			return false;
		
		int index = hql.indexOf(":" + param);
		
		if(index < 0)
			return false;
		
		if(index + param.length() + 1 == hql.length() - 1)
			return true;
		
		if(index + param.length() + 1 < hql.length() - 1 && (hql.charAt(index + param.length() + 1) == ' ' 
			|| hql.charAt(index + param.length() + 1) == '\t' 
				|| hql.charAt(index + param.length() + 1) == '\n' 
					|| hql.charAt(index + param.length() + 1) == '\r') 
						|| hql.charAt(index + param.length() + 1) == '(' 
							|| hql.charAt(index + param.length() + 1) == ')')
		{
			return true;
		}
		
		
		return false;
	}
	
	private Object getDummyParamValue(String type)
	{
		//param of type BO, the query expects a param of type Integer
		if(!type.startsWith("java."))
			return new java.lang.Integer(0);
		if(type.equalsIgnoreCase("java.lang.Integer"))
			return new java.lang.Integer(0);
		if(type.equalsIgnoreCase("java.math.BigInteger"))
			return java.math.BigInteger.ZERO;
		if(type.equalsIgnoreCase("java.lang.Short"))
			return new java.lang.Short("0");
		if(type.equalsIgnoreCase("java.lang.Long"))
			return new java.lang.Long(0);
		if(type.equalsIgnoreCase("java.lang.Boolean"))
			return java.lang.Boolean.FALSE;
		if(type.equalsIgnoreCase("java.lang.String"))
			return "*";
		if(type.equalsIgnoreCase("java.math.BigDecimal"))
			return new java.math.BigDecimal(0);
		if(type.equalsIgnoreCase("java.lang.Float"))
			return new java.lang.Float(0);
		if(type.equalsIgnoreCase("java.lang.Double"))
			return new java.lang.Double(0);
		if(type.equalsIgnoreCase("java.util.Date"))
			return new java.util.Date();
		if(type.equalsIgnoreCase("java.sql.Date"))
			return new java.sql.Date(System.currentTimeMillis());
		if(type.equalsIgnoreCase("java.sql.Time"))
			return new java.sql.Time(System.currentTimeMillis());
		
		return new java.lang.Integer(0);		
	}
	
	public void validateFind(final String hql, final String[] paramNames, final Object[] paramValues)
	{
		Transaction tx = null;
		Session session = null;
		
		long mili1 = System.currentTimeMillis();

		session = HibernateUtil.currentSession();
		
		tx = session.beginTransaction();
		try
		{
			Query q = session.createQuery(hql);
			for (int i = 0; i < paramNames.length; i++)
			{
				q.setParameter(paramNames[i], paramValues[i]);
			}
			q.setMaxResults(0);
			q.list();
	
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
		
		if(log.isDebugEnabled())
		{
			long mili2 = System.currentTimeMillis();
			
			try
			{
				StringBuilder sb = new StringBuilder();
				sb.append("validateFind():  >> called on server: ");
				sb.append(InetAddress.getLocalHost().getHostName());
				sb.append(" at ");
				sb.append(System.getProperty("catalina.home"));
				sb.append(" took ");
				sb.append(mili2 - mili1);
				sb.append(" ms");
				
				log.debug(sb.toString());
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
		}
		
		long mili2 = System.currentTimeMillis();
		System.out.println("validateFind() took " + (mili2 - mili1) + " ms.");
	}
	
	public void validateFindSQL(final String sql, final String[] paramNames, final Object[] paramValues)
	{
		Transaction tx = null;
		Session session = null;

		long mili1 = System.currentTimeMillis();
		
		session = HibernateUtil.currentSession();
		
		tx = session.beginTransaction();
		try
		{
			Query q = session.createSQLQuery(sql);
			for (int i = 0; i < paramNames.length; i++)
			{
				q.setParameter(paramNames[i], paramValues[i]);
			}
			q.setMaxResults(0);
			q.list();
	
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
		
		if(log.isDebugEnabled())
		{
			long mili2 = System.currentTimeMillis();
			
			try
			{
				StringBuilder sb = new StringBuilder();
				sb.append("validateFind():  >> called on server: ");
				sb.append(InetAddress.getLocalHost().getHostName());
				sb.append(" at ");
				sb.append(System.getProperty("catalina.home"));
				sb.append(" took ");
				sb.append(mili2 - mili1);
				sb.append(" ms");
				
				log.debug(sb.toString());
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
		}
		
		long mili2 = System.currentTimeMillis();
		System.out.println("validateFindSQL() took " + (mili2 - mili1) + " ms.");
	}
	
	public String getStackTrace(Throwable t)
	{
		StringWriter stringWritter = new StringWriter();
		PrintWriter printWritter = new PrintWriter(stringWritter, true);
		t.printStackTrace(printWritter);
		printWritter.flush();
		stringWritter.flush();

		return stringWritter.toString();
	}   	
	
	private static Document getXmlDocument(String xmlBuffer) throws DocumentException
	{
		return DocumentHelper.parseText(xmlBuffer);
	}
	
	private class QueryParam
	{
		String field;
		String type;
		
		public QueryParam()
		{
			
		}
		public QueryParam(String field, String type)
		{
			this.field = field;
			this.type = type;
		}

		public String getField()
		{
			return field;
		}
		public void setField(String field)
		{
			this.field = field;
		}
		public String getType()
		{
			return type;
		}
		public void setType(String type)
		{
			this.type = type;
		}
	}
	
}
