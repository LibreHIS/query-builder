/*
 * Created on Feb 14, 2005
 *
 */
package com.ims.query.server;

import ims.configuration.Configuration;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

/**
 * @author vpurdila
 *  
 */
public class QueryServlet extends HttpServlet
{
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		String[] val = null;
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/xml");
		
		val = request.getParameterValues("action");
		if(val != null)
		{
			if(val[0].equalsIgnoreCase("list"))
			{
				try
				{
					out.print(getMappedClasses());
				} catch (HibernateException e1)
				{
					e1.printStackTrace();
				}
			}
			else if(val[0].equalsIgnoreCase("listPC"))
			{
				try
				{
					out.print(getPersistentClasses());
				} catch (HibernateException e1)
				{
					e1.printStackTrace();
				}
			}
			else if(val[0].equalsIgnoreCase("listTables"))
			{
				try
				{
					out.print(getRegisteredTables());
				} catch (HibernateException e1)
				{
					e1.printStackTrace();
				}
			}
			else if(val[0].equalsIgnoreCase("get"))
			{
				if((val = request.getParameterValues("class")) != null)
				{
					try
					{
						out.print(getClassMetadata(val[0]));
					} catch (ClassNotFoundException e)
					{
						e.printStackTrace();
					} catch (HibernateException e)
					{
						e.printStackTrace();
					}
				}
				else if((val = request.getParameterValues("bo")) != null)
				{
					if(val != null)
					{
						try
						{
							out.print(getPersistentClassInfo(val[0]));
						} 
						catch (HibernateException e)
						{
							e.printStackTrace();
						}
					}
				}
				else if((val = request.getParameterValues("table")) != null)
				{
					if(val != null)
					{
						try
						{
							out.print(getTableInfo(val[0]));
						} 
						catch (HibernateException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			else if(val[0].equalsIgnoreCase("login"))
			{
				String user = "";
				String pass = "";
				String answer = "";
				
				val = request.getParameterValues("user");
				if(val != null)
				{
					user = val[0];
				}

				val = request.getParameterValues("pass");
				if(val != null)
				{
					pass = val[0];
				}
				
				HttpSession session = request.getSession(true);
				answer = checkLoginParams(user, pass);
				
				if(session != null && "OK".equalsIgnoreCase(answer))
				{
					session.setAttribute("USER", user);
					
					//System.out.println("Query builder login: JSESSIONID=" + session.getId() + ", USER=" + user);
				}
				
				out.print(answer);
			}
			else if(val[0].equalsIgnoreCase("getLastError"))
			{
				String answer = "";
				HttpSession session = request.getSession(true);
				
				if(session != null)
				{
					Object obj = session.getAttribute(StringUtils.IMS_LAST_ERROR);
					
					session.removeAttribute(StringUtils.IMS_LAST_ERROR);
					
					if(obj != null)
						answer = obj.toString();
					
					out.print(answer);
				}
				else
				{
					out.print(answer);
				}
			}
			
		}
	}

	private String getPersistentClasses()
	{
		StringBuilder sb = new StringBuilder(1000);
		
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<classes>");
			ArrayList<String> list = HibernateUtil.getPersistentClasses();

			for (int i = 0; i < list.size(); i++)
			{
				sb.append("<c>");
				sb.append(StringUtils.encodeXML(list.get(i)));
				sb.append("</c>");
			}
		sb.append("</classes>");
		
		return sb.toString();
	}

	private String getRegisteredTables()
	{
		StringBuilder sb = new StringBuilder(1000);
		
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<tables>");
			ArrayList<String> list = HibernateUtil.getRegisteredTables();

			for (int i = 0; i < list.size(); i++)
			{
				sb.append("<t>");
				sb.append(StringUtils.encodeXML(list.get(i)));
				sb.append("</t>");
			}
		sb.append("</tables>");
		
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private String getTableInfo(String tableName)
	{
		StringBuilder sb = new StringBuilder(1000);
		
		org.hibernate.mapping.Table t = HibernateUtil.getTable(tableName);
		
		if(t == null)
			return "";
		
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<table name=\"");
		sb.append(StringUtils.encodeXML(tableName));
		sb.append("\">");
		sb.append("<fields>");
		
		Iterator it = t.getColumnIterator();
		while(it.hasNext())
		{
			org.hibernate.mapping.Column col = (org.hibernate.mapping.Column) it.next();
			
			getFieldInfo(sb, col, t, null);
		}

		sb.append("</fields>");
		sb.append(getForeignKeysInfo(t, null));
		sb.append("</table>");
		
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String getPersistentClassInfo(String entityName)
	{
		StringBuilder sb = new StringBuilder(1000);
		ArrayList<String> props = new ArrayList<String>();
		ArrayList<String> mappedCols = new ArrayList<String>();
		
		org.hibernate.mapping.PersistentClass pc = HibernateUtil.getPersistentClass(entityName);

		if(pc == null)
			return "";
		
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<pc name=\"");
		sb.append(StringUtils.encodeXML(entityName));
		sb.append("\" table=\"");
		sb.append(StringUtils.encodeXML(pc.getTable().getName()));
		sb.append("\"");
		sb.append(">");
		sb.append("<props>");
		
		org.hibernate.mapping.Property p = pc.getIdentifierProperty();

		if(p != null)
		{
			sb.append(getPropertyInfo(p, 0, AttrType.ID, mappedCols));
			props.add(p.getName());
		}

		p = pc.getVersion();
		if(p != null && !props.contains(p.getName()))
		{
			sb.append(getPropertyInfo(p, 0, AttrType.VERSION, mappedCols));
			props.add(p.getName());
		}
		
		Value v = pc.getDiscriminator();
		if(v != null && v.getClass().getName().equalsIgnoreCase("org.hibernate.mapping.SimpleValue"))
		{
			sb.append("<p name=\"discriminator\" hibtype=\"0\">");
				Iterator it = v.getColumnIterator();
				while(it.hasNext())
				{
					org.hibernate.mapping.Column col = (org.hibernate.mapping.Column) it.next();
					
					getFieldInfo(sb, col, p.getPersistentClass() != null ? p.getPersistentClass().getTable() : null, mappedCols);
				}
			sb.append("</p>");
		}
		
		PersistentClass sup = pc.getSuperclass();
		if(sup != null)
		{
			Iterator it = sup.getPropertyIterator();
			while(it.hasNext())
			{
				p = (Property) it.next();
				
				if(!props.contains(p.getName()))
				{
					sb.append(getPropertyInfo(p, 0, null, mappedCols));
					props.add(p.getName());
				}
			}
		}
		
		Iterator it = pc.getPropertyIterator();
		while(it.hasNext())
		{
			p = (Property) it.next();
			
			if(!props.contains(p.getName()))
			{
				sb.append(getPropertyInfo(p, 0, null, mappedCols));
				props.add(p.getName());
			}
		}
		
		sb.append("</props>");
		sb.append(getForeignKeysInfo(pc.getTable(), mappedCols));
		sb.append(getUnmappedFields(pc.getTable(), mappedCols));
		sb.append("</pc>");
		
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String getPropertyInfo(Property p, int level, AttrType type, ArrayList<String> mappedCols)
	{
		StringBuilder sb = new StringBuilder(200);
		
		if(level == 0)
		{
			sb.append("<p name=\"");
			sb.append(StringUtils.encodeXML(p.getName()));
			sb.append("\""); 

			if(p.getValue().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.List"))
			{
				org.hibernate.mapping.List l = (org.hibernate.mapping.List) p.getValue(); 
				
				org.hibernate.mapping.Table t = l.getCollectionTable();

				if(t != null)
				{
					sb.append(" collTable=\"");
					sb.append(StringUtils.encodeXML(t.getName()));
					sb.append("\"");
				}
				
				if(l.getElement() != null && l.getElement().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.Component"))
				{
					org.hibernate.mapping.Component c = (Component) l.getElement();
					
					sb.append(" type=\"");
					sb.append(StringUtils.encodeXML(c.getComponentClassName()));
					sb.append("\"");
				}
				else if(l.getElement() != null && l.getElement().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.OneToMany"))
				{
					org.hibernate.mapping.OneToMany o = (org.hibernate.mapping.OneToMany) l.getElement();
					
					sb.append(" type=\"");
					sb.append(StringUtils.encodeXML(o.getReferencedEntityName()));
					sb.append("\"");
				}

			}
			else if(p.getValue().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.Set"))
			{
				org.hibernate.mapping.Set s = (org.hibernate.mapping.Set) p.getValue(); 
				
				org.hibernate.mapping.Table t = s.getCollectionTable();

				if(t != null)
				{
					sb.append(" collTable=\"");
					sb.append(StringUtils.encodeXML(t.getName()));
					sb.append("\"");
				}
				
				if(s.getElement() != null && s.getElement().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.Component"))
				{
					org.hibernate.mapping.Component c = (Component) s.getElement();
					
					sb.append(" type=\"");
					sb.append(StringUtils.encodeXML(c.getComponentClassName()));
					sb.append("\"");
				}
				else if(s.getElement() != null && s.getElement().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.OneToMany"))
				{
					org.hibernate.mapping.OneToMany o = (org.hibernate.mapping.OneToMany) s.getElement();
					
					sb.append(" type=\"");
					sb.append(StringUtils.encodeXML(o.getReferencedEntityName()));
					sb.append("\"");
				}
				
			}
			else if(p.getValue().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.OneToOne"))
			{
				org.hibernate.mapping.OneToOne o = (org.hibernate.mapping.OneToOne) p.getValue(); 

				sb.append(" type=\"");
				sb.append(StringUtils.encodeXML(o.getReferencedEntityName()));
				sb.append("\"");
			}
			
			sb.append(" hibtype=\"");
			if(type == null)
				sb.append(HibernateUtil.getIconForType(p.getValue().getType()));
			else
				sb.append(type);
			sb.append("\"");
			
			sb.append(">");
		}
		
		if(p.getValue().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.SimpleValue"))
		{
			org.hibernate.mapping.SimpleValue v = (org.hibernate.mapping.SimpleValue) p.getValue(); 
			
			Iterator it = v.getColumnIterator();
			while(it.hasNext())
			{
				org.hibernate.mapping.Column col = (org.hibernate.mapping.Column) it.next();
				
				getFieldInfo(sb, col, p.getPersistentClass() != null ? p.getPersistentClass().getTable() : null, mappedCols);
			}
		}
		else if(p.getValue().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.Component"))
		{
			org.hibernate.mapping.Component c = (org.hibernate.mapping.Component) p.getValue(); 
			
			Iterator it = c.getPropertyIterator();

			while(it.hasNext())
			{
				Property pc = (Property) it.next();
				
				sb.append(getPropertyInfo(pc, level + 1, null, mappedCols));
			}
			
		}
		else if(p.getValue().getClass().getName().equalsIgnoreCase("org.hibernate.mapping.ManyToOne"))
		{
			org.hibernate.mapping.ManyToOne m = (org.hibernate.mapping.ManyToOne) p.getValue(); 
			
			Iterator it = m.getColumnIterator();
			while(it.hasNext())
			{
				org.hibernate.mapping.Column col = (org.hibernate.mapping.Column) it.next();
				
				getFieldInfo(sb, col, p.getPersistentClass() != null ? p.getPersistentClass().getTable() : null, mappedCols);
			}
		}
		else
		{
			sb.append("");
		}

		if(level == 0)
		{
			sb.append("</p>");
		}
		
		return sb.toString();
	}

	private void getFieldInfo(StringBuilder sb, org.hibernate.mapping.Column col, Table t, ArrayList<String> mappedCols)
	{
		org.hibernate.mapping.Value val = col.getValue();
		
		sb.append("<f name=\"");
		sb.append(StringUtils.encodeXML(col.getName()));
		sb.append("\" type=\"");
		sb.append(StringUtils.encodeXML(val.getType().getName()));
		sb.append("\"");
		if(t != null && isPrimaryKey(t, col))
		{
			sb.append(" pk=\"true\"");
		}
		sb.append("/>");
		
		if(mappedCols != null)
		{
			mappedCols.add(col.getName());
		}
	}

	@SuppressWarnings("rawtypes")
	private String checkLoginParams(String user, String pass)
	{
		Transaction tx = null;
		Iterator it = null;
		Session session = null;

		String hql = "select a1_1.id from AppUser as a1_1 where (a1_1.username = :USER and a1_1.password = :PASS)";
		
		session = HibernateUtil.currentSession();

		tx = session.beginTransaction();
		try
		{
			Query q = session.createQuery(hql);
			q.setParameter("USER", user, Hibernate.STRING);
			q.setParameter("PASS", Configuration.getHash(pass), Hibernate.STRING);

			it = q.list().iterator();
			tx.commit();
		}
		catch (HibernateException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
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
		
		if(!it.hasNext())
			return "";
		
		//Object[] row = (Object[]) it.next();
		
		return "OK";
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException
	{
		doGet(request, response);
	}
	
	private String getClassMetadata(String className) throws HibernateException, ClassNotFoundException
	{
		StringBuffer sb = new StringBuffer(1000);
		String strType;
		String strTypeId;
		String strId;
		
		ClassMetadata md = null;
		
		md = HibernateUtil.getClassMetadata(className);
		
		if(md == null)
			return getUnregisteredClassMetadata(className);
		
		CollectionMetadata colmd = null;

		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<metadata>");
		
		//get the ID
		strId = md.getIdentifierPropertyName();
		Type typeId = md.getIdentifierType();
		
		strType = StringUtils.encodeXML(typeId.getReturnedClass().getName());
		
		if(strType.startsWith("java."))
		{
			strTypeId = "0";
		}
		else
		{
			strTypeId = HibernateUtil.getClassId(strType);
		}
		
		if(strTypeId == null)
			strTypeId = "0";
		
		FieldInfo fieldInfo = HibernateUtil.getFieldProperties(className, strId);
		
		sb.append("<field>");
			sb.append("<name>");
				sb.append(StringUtils.encodeXML(strId));
			sb.append("</name>");
			sb.append("<type>");
				sb.append(strType);
			sb.append("</type>");
			sb.append("<typeid>");
				sb.append(strTypeId);
			sb.append("</typeid>");
			sb.append("<iscollection>false</iscollection>");
			sb.append("<hibtype>" + AttrType.ID.toString() + "</hibtype>");
			
			sb.append("<allowNull>" + fieldInfo.isAllowNull() + "</allowNull>");
			sb.append("<isUnique>" + fieldInfo.isUnique() + "</isUnique>");
			sb.append("<isIndexed>" + fieldInfo.isIndexed() + "</isIndexed>");
			sb.append("<indexNames>" + fieldInfo.getIndexNames() + "</indexNames>");
		sb.append("</field>");
		
		
		String[] names = md.getPropertyNames();
		for (int i = 0; i < names.length; i++)
		{
			Type type = md.getPropertyTypes()[i];
			
			if (type.isCollectionType())
			{
				colmd = HibernateUtil.getCollectionMetadata(((CollectionType)type).getRole());

				strType = StringUtils.encodeXML(colmd.getElementType().getReturnedClass().getName());
				if(strType.startsWith("java."))
				{
					//strType = strType.substring(10);
					strTypeId = "0";
				}
				/*else if(strType.startsWith("java.util."))
				{
					strType = strType.substring(10);
					strTypeId = "0";
				}*/
				else
				{
					strTypeId = HibernateUtil.getClassId(strType);
				}
				
				if(strTypeId == null)
					strTypeId = "0";

				fieldInfo = HibernateUtil.getFieldProperties(className, names[i]);
				
				sb.append("<field>");
					sb.append("<name>");
						sb.append(StringUtils.encodeXML(names[i]));
					sb.append("</name>");
					sb.append("<type>");
						sb.append(strType);
					sb.append("</type>");
					sb.append("<typeid>");
						sb.append(strTypeId);
					sb.append("</typeid>");
					sb.append("<iscollection>true</iscollection>");
					sb.append("<hibtype>" + HibernateUtil.getIconForType(type).toString() + "</hibtype>");
					
					sb.append("<allowNull>" + fieldInfo.isAllowNull() + "</allowNull>");
					sb.append("<isUnique>" + fieldInfo.isUnique() + "</isUnique>");
					sb.append("<isIndexed>" + fieldInfo.isIndexed() + "</isIndexed>");
					sb.append("<indexNames>" + fieldInfo.getIndexNames() + "</indexNames>");
					
				sb.append("</field>");
			} 
			else
			{
				strType = StringUtils.encodeXML(type.getReturnedClass().getName());
				if(strType.startsWith("java."))
				{
					//strType = strType.substring(10);
					strTypeId = "0";
				}
				/*
				else if(strType.startsWith("java.util."))
				{
					strType = strType.substring(10);
					strTypeId = "0";
				}*/
				else
				{
					strTypeId = HibernateUtil.getClassId(strType);
				}
				
				if(strTypeId == null)
					strTypeId = "0";
				
				fieldInfo = HibernateUtil.getFieldProperties(className, names[i]);
				
				sb.append("<field>");
					sb.append("<name>");
						sb.append(StringUtils.encodeXML(names[i]));
					sb.append("</name>");
					sb.append("<type>");
						sb.append(StringUtils.encodeXML(strType));
					sb.append("</type>");
					sb.append("<typeid>");
						sb.append(strTypeId);
					sb.append("</typeid>");
					sb.append("<iscollection>false</iscollection>");
					sb.append("<hibtype>" + HibernateUtil.getIconForType(type).toString() + "</hibtype>");
					
					sb.append("<allowNull>" + fieldInfo.isAllowNull() + "</allowNull>");
					sb.append("<isUnique>" + fieldInfo.isUnique() + "</isUnique>");
					sb.append("<isIndexed>" + fieldInfo.isIndexed() + "</isIndexed>");
					sb.append("<indexNames>" + fieldInfo.getIndexNames() + "</indexNames>");
					
				sb.append("</field>");
			}
		}
		
		sb.append("</metadata>");
		
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String getUnregisteredClassMetadata(String className) throws ClassNotFoundException, HibernateException
	{
		StringBuffer sb = new StringBuffer(1000);
		Class clazz = Class.forName(className);
		
		Field fieldlist[] = clazz.getDeclaredFields();
		
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<metadata component=\"true\">");
		
		String strTypeId = "";
		String strType = "";
		
		for (int i = 0; i < fieldlist.length; i++) 
		{
			Field fld = fieldlist[i];
			
			strType = StringUtils.encodeXML(fld.getType().getName());

			if(strType.equalsIgnoreCase("int"))
				continue;
			else if(strType.equalsIgnoreCase("long"))
				continue;
			else if(strType.equalsIgnoreCase("short"))
				continue;
			else if(strType.equalsIgnoreCase("float"))
				continue;
			else if(strType.equalsIgnoreCase("double"))
				continue;
			else if(strType.equalsIgnoreCase("boolean"))
				continue;
			
			if(strType.startsWith("java."))
			{
				strTypeId = "0";
			}
			else
			{
				strTypeId = HibernateUtil.getClassId(strType);
			}
			
			if(strTypeId == null)
				strTypeId = "0";
			
			sb.append("<field>");
				sb.append("<name>");
					sb.append(StringUtils.encodeXML(fld.getName()));
				sb.append("</name>");
				sb.append("<type>");
					sb.append(StringUtils.encodeXML(strType));
				sb.append("</type>");
				sb.append("<typeid>");
					sb.append(strTypeId);
				sb.append("</typeid>");
				sb.append("<iscollection>false</iscollection>");
				if(fld.getName().equalsIgnoreCase("id"))
					sb.append("<hibtype>" + AttrType.ID.toString() + "</hibtype>");
				else
					sb.append("<hibtype>" + AttrType.ANY.toString() + "</hibtype>");
			sb.append("</field>");
		}
		
		sb.append("</metadata>");
		
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String getMappedClasses() throws HibernateException
	{
		String strClass;
		StringBuffer sb = new StringBuffer(20000);

		ArrayList classes = null;
		classes = HibernateUtil.getRegisteredClasses();

		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<mappedclasses>");
		for (int i = 0; i < classes.size(); i++)
		{
			strClass = StringUtils.encodeXML(((ClassHolder) classes.get(i)).getClazz().getName());
			
			sb.append("<mappedclass>");
				sb.append("<id>");
					sb.append(HibernateUtil.getClassId(strClass));
				sb.append("</id>");
				sb.append("<name>");
					sb.append(strClass);
				sb.append("</name>");
			sb.append("</mappedclass>");
		}
		sb.append("</mappedclasses>");
		
		return sb.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private String getForeignKeysInfo(Table t, ArrayList<String> mappedCols)
	{
		StringBuilder sb = new StringBuilder(300);
		
		sb.append("<fkinfo>");
			Iterator it = t.getForeignKeyIterator();
			while(it.hasNext())
			{
				ForeignKey fk = (ForeignKey) it.next();
	
				sb.append("<fk name=\"");
				sb.append(StringUtils.encodeXML(fk.getName()));
				sb.append("\"");
				if(fk.getReferencedTable() != null)
				{
					sb.append(" refTable=\"");
					sb.append(StringUtils.encodeXML(fk.getReferencedTable().getName()));
					sb.append("\"");
				}
				sb.append(" refToPK=\"");
				sb.append(fk.isReferenceToPrimaryKey());
				sb.append("\"");
				sb.append(" type=\"");
				sb.append(StringUtils.encodeXML(fk.getReferencedEntityName()));
				sb.append("\"");
				sb.append(">");
					sb.append("<cols>");
					Iterator iCol = fk.getColumnIterator();
					while (iCol.hasNext())
					{
						Column c = (Column) iCol.next();
						sb.append("<c name=\"");
						sb.append(StringUtils.encodeXML(c.getName()));
						sb.append("\"");
						sb.append("/>");
						
						if(mappedCols != null)
							mappedCols.add(c.getName());
					}
					sb.append("</cols>");
		
					sb.append("<refCols>");
					iCol = fk.getReferencedColumns().iterator();
					while (iCol.hasNext())
					{
						Column c = (Column) iCol.next();
					
						sb.append("<c name=\"");
						sb.append(StringUtils.encodeXML(c.getName()));
						sb.append("\"");
						sb.append("/>");
					}
					sb.append("</refCols>");
				sb.append("</fk>");
			}
		sb.append("</fkinfo>");
		
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String getUnmappedFields(Table t, ArrayList<String> mappedCols)
	{
		StringBuilder sb = new StringBuilder(300);
		
		sb.append("<unmapped>");
			Iterator iCol = t.getColumnIterator();
			while(iCol.hasNext())
			{
				Column c = (Column) iCol.next();
				
				if(!mappedCols.contains(c.getName()))
				{
					sb.append("<c name=\"");
					sb.append(StringUtils.encodeXML(c.getName()));
					sb.append("\"");
					sb.append(" type=\"");
					sb.append(StringUtils.encodeXML(c.getValue().getType().getName()));
					sb.append("\"");
					sb.append("/>");
					
					mappedCols.add(c.getName());
				}
			}
		sb.append("</unmapped>");
		
		return sb.toString();
	}
	
	private boolean isPrimaryKey(Table t, Column c)
	{
		if(t.getPrimaryKey() != null && t.getPrimaryKey().containsColumn(c))
			return true;
			
		return false;
	}
}