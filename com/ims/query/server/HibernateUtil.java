/*
 * Created on Feb 14, 2005
 *
 */
package com.ims.query.server;

import ims.domain.hibernate3.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

/**
 * @author vpurdila
 *  
 */
public class HibernateUtil
{
	static final Logger log = Logger.getLogger(HibernateUtil.class);
	
	private static final SessionFactory	sessionFactory;

	public static final ThreadLocal		session	= new ThreadLocal();

	private static Map					classMetaData;

	private static ArrayList			classes;
	
	private static ArrayList<String>	persistentClasses;

	private static Map					collectionMetaData;

	private static Map					idClasses;
	static
	{
		sessionFactory = Registry.getInstance().getSessionFactory();
		/*
		try
		{
			sessionFactory = new Configuration().configure().buildSessionFactory();
		} 
		catch (HibernateException ex)
		{
			throw new RuntimeException("Exception building SessionFactory: "
					+ ex.getMessage(), ex);
		}
		*/
	}

	public static Session currentSession() throws HibernateException
	{
		Session s = (Session) session.get();
		if (s == null)
		{
			s = sessionFactory.openSession();
			session.set(s);
		}
		return s;
	}

	public static void closeSession() throws HibernateException
	{
		Session s = (Session) session.get();
		session.set(null);
		if (s != null)
			s.close();
	}

	public static synchronized ArrayList getRegisteredClasses()
			throws HibernateException
	{
		if (classes == null)
		{
			classMetaData = sessionFactory.getAllClassMetadata();
			
			collectionMetaData = sessionFactory.getAllCollectionMetadata();
			Set keyset = classMetaData.keySet();
			classes = new ArrayList();

			Iterator iterator = keyset.iterator();
			while (iterator.hasNext())
			{
				String className = null;
				try
				{
					className = (String)iterator.next();
					classes.add(new ClassHolder(Class.forName(className)));
					
					if(log.isDebugEnabled())
					{
						log.debug("Add registered class - " + className);
					}

				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
			
			//hardcoded classes, due to a bug in hibernate
			/*
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.core.generic.domain.objects.PersonName")));
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.core.generic.domain.objects.Address")));
			} 
			catch (ClassNotFoundException e1)
			{
				e1.printStackTrace();
			}
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.core.patient.domain.objects.Insurance")));
			} 
			catch (ClassNotFoundException e2)
			{
				e2.printStackTrace();
			}
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.core.patient.domain.objects.PatientId")));
			} 
			catch (ClassNotFoundException e3)
			{
				e3.printStackTrace();
			}
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.domain.SystemInformation")));
			} 
			catch (ClassNotFoundException e3)
			{
				e3.printStackTrace();
			}
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.domain.lookups.LookupInstanceRef")));
			} 
			catch (ClassNotFoundException e3)
			{
				e3.printStackTrace();
			}
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.core.clinical.domain.objects.TaxonomyMap")));
			} 
			catch (ClassNotFoundException e3)
			{
				e3.printStackTrace();
			}
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.domain.lookups.LookupMapping")));
			} 
			catch (ClassNotFoundException e3)
			{
				e3.printStackTrace();
			}
			try
			{
				classes.add(new ClassHolder(Class.forName("ims.framework.utils.Color")));
			} 
			catch (ClassNotFoundException e3)
			{
				e3.printStackTrace();
			}
			*/

			iterator = keyset.iterator();
			ArrayList extraClasses = new ArrayList();
			while (iterator.hasNext())
			{
				String className = (String)iterator.next();
				
				ClassMetadata classMetadata2 = getClassMetadata(className);
				if(classMetadata2 != null)
				{
					Type props[] = classMetadata2.getPropertyTypes();
					String[] names = classMetadata2.getPropertyNames();
					
					if(log.isDebugEnabled())
					{
						log.debug("Checking Metadata for class '" + className);
					}
					
					if(props != null)
					{
						for (int i = 0; i < props.length; i++)
						{
							if(log.isDebugEnabled())
							{
								log.debug("Property: " + names[i] + ", Type: " + props[i].getReturnedClass().getName());
							}

							String strType = "";
							Class classType = null;
							if (props[i].isCollectionType())
							{
								CollectionMetadata colmd = HibernateUtil.getCollectionMetadata(((CollectionType)props[i]).getRole());

								strType = StringUtils.encodeXML(colmd.getElementType().getReturnedClass().getName());
								classType = colmd.getElementType().getReturnedClass();
							}
							else
							{
								strType = props[i].getReturnedClass().getName();
								classType = props[i].getReturnedClass();
							}
							
							if(classType != null && !strType.startsWith("java.") && !classMetaData.containsKey(strType) && !extraClasses.contains(classType))
							{
								extraClasses.add(classType);
								if(log.isDebugEnabled())
								{
									log.debug("  >>  Property: " + strType + " added.");
								}
							}
						}
					}
				}
				
				if(log.isDebugEnabled())
				{
					log.debug("-------------------------------------------------------------");
				}
				
			}
			for (int i = 0; i < extraClasses.size(); i++)
			{
				classes.add(new ClassHolder((Class) extraClasses.get(i)));
				
				if(log.isDebugEnabled())
				{
					log.debug("Metadata for class '" + ((Class) extraClasses.get(i)).getName() + "' added.");
				}
			}
			
			Collections.sort(classes);
			idClasses = new HashMap();
			CRC32 crc = new CRC32();
			ClassHolder clazz;
			for (int i = 0; i < classes.size(); i++)
			{
				clazz = (ClassHolder) classes.get(i);
				crc.reset();
				crc.update(clazz.getClazz().getName().getBytes());
				idClasses.put(clazz.getClazz().getName(), String.valueOf(crc
						.getValue()));
			}
		}
		return classes;
	}

	public static synchronized ClassMetadata getClassMetadata(String className)
			throws HibernateException
	{
		if (className == null)
			return null;
		if (classes == null)
			getRegisteredClasses();
		return (ClassMetadata) classMetaData.get(className);
	}

	public static synchronized CollectionMetadata getCollectionMetadata(
			String type) throws HibernateException
	{
		if (type == null)
			return null;
		if (classes == null)
			getRegisteredClasses();
		return (CollectionMetadata) collectionMetaData.get(type);
	}

	public static synchronized String getClassId(String className)
			throws HibernateException
	{
		if (className == null)
			return null;
		if (classes == null)
			getRegisteredClasses();
		return (String) idClasses.get(className);
	}

	public static synchronized AttrType getIconForType(Type type)
	{
		AttrType result = AttrType.UNKNOWN;
		
		if (type.isEntityType())
		{
			EntityType et = (EntityType) type;
			if (!et.isOneToOne())
			{
				result = AttrType.MANY2ONE;
			} 
			else
			{
				result = AttrType.ONE2ONE;
			}
		} 
		else if (type.isAnyType())
		{
			result = AttrType.ANY;
		} 
		else if (type.isComponentType())
		{
			result = AttrType.COMPONENT;
		} 
		else if (type.isCollectionType())
		{
			result = AttrType.ONE2MANY; //could also be values/collecionts?
		} else
		{
			result = AttrType.PROPERTY;
		}
		
		return result;
	}
	
	public static synchronized String tooltipForType(Type type) 
	{
		if(type == null) 
			return null;
		
		StringBuffer sb = new StringBuffer(300);
		sb.append("TypeClass: " + type.getClass() + "<br>");
		sb.append("TypeName:" + type.getName() + "<br>");
		sb.append("Association: " + type.isAssociationType() + "<br>");
		sb.append("Component: " + type.isComponentType() + "<br>");
		sb.append("Entity: " + type.isEntityType() + "<br>");
		sb.append("Mutable: " + type.isMutable() + "<br>");
		sb.append("ObjectType: " + type.isAnyType() + "<br>");
		sb.append("PersistentCollection: " + type.isCollectionType());

		if (type.isCollectionType()) 
		{
			CollectionType ptype = (CollectionType) type;
			sb.append("<br>");
			sb.append("Role: " + ptype.getRole() + "<br>");
			sb.append("ForeignKeyDirection:" + ptype.getForeignKeyDirection());
		}
		
		return sb.toString();
	}
	
	public static synchronized FieldInfo getFieldProperties(String entityName, String fieldName)
	{
		FieldInfo ret = new FieldInfo();
		
		org.hibernate.mapping.PersistentClass pc = Registry.getInstance().getConfiguration().getClassMapping(entityName);
		org.hibernate.mapping.Property prop = pc.getProperty(fieldName);

		ret.setFieldName(fieldName);
		ret.setAllowNull(prop.isOptional());
		ret.setUnique(false);
		ret.setIndexed(false);
		ret.setIndexNames("");
		
		org.hibernate.mapping.Value val = prop.getValue();
		if (!(val instanceof org.hibernate.mapping.Component) && !(val instanceof org.hibernate.mapping.Collection))
		{
			org.hibernate.mapping.Table table = val.getTable();
			if (table != null && val.getColumnSpan() == 1)
			{
				StringBuffer sb = new StringBuffer();
				org.hibernate.mapping.Column col = (org.hibernate.mapping.Column)val.getColumnIterator().next();
				
				ret.setUnique(col.isUnique());
				ret.setIndexed(isColInTableIndexes(table, col, sb));				
				ret.setIndexNames(sb.toString());				
			}
		}
		return ret;
	}

	public static synchronized org.hibernate.mapping.PersistentClass getPersistentClass(String entityName)
	{
		return Registry.getInstance().getConfiguration().getClassMapping(entityName);
	}
	
	public static synchronized ArrayList<String> getPersistentClasses()
	{
		if(persistentClasses == null)
		{
			persistentClasses = new ArrayList<String>();
			
			Iterator it = Registry.getInstance().getConfiguration().getClassMappings();
			
			while(it.hasNext())
			{
				PersistentClass pc = (PersistentClass) it.next();
				
				persistentClasses.add(pc.getEntityName());
			}
			
			Collections.sort(persistentClasses);
		}
		
		return persistentClasses;
	}
	
	public static synchronized org.hibernate.mapping.Table getTable(String tableName)
	{
		Iterator it = Registry.getInstance().getConfiguration().getTableMappings();
		
		while(it.hasNext())
		{
			org.hibernate.mapping.Table t = (org.hibernate.mapping.Table)it.next();
			
			if(t.getName().equalsIgnoreCase(tableName))
				return t;
		}

		return null;
	}
	
	private static boolean isColInTableIndexes(org.hibernate.mapping.Table table, Column col, StringBuffer sb)
	{
		boolean ret = false;
		String comma = "";

		Iterator idxIter = table.getUniqueKeyIterator();
		while (idxIter.hasNext())
		{
			org.hibernate.mapping.UniqueKey unq = (org.hibernate.mapping.UniqueKey)idxIter.next();
			if (isColInIdxCols(unq.getColumnIterator(), col))
			{
				ret = true;
				sb.append(comma + unq.getName());
				comma = ", ";
			}
		}		
		idxIter = table.getIndexIterator();
		while (idxIter.hasNext())
		{
			org.hibernate.mapping.Index idx = (org.hibernate.mapping.Index)idxIter.next();
			if (isColInIdxCols(idx.getColumnIterator(), col))
			{
				ret = true;
				sb.append(comma + idx.getName());				
				comma = ", ";
			}
		}		
		return ret;
	}
	
	

	private static boolean isColInIdxCols(Iterator colIter, Column col)
	{
		boolean ret = false;
		while (colIter.hasNext())
		{
			org.hibernate.mapping.Column idxCol = (org.hibernate.mapping.Column)colIter.next();
			if (idxCol.getName().equals(col.getName()))
			{
				ret = true;
				break;
			}					
		}
		return ret;
	}	

	public static synchronized ArrayList<String> getRegisteredTables()
	{
		ArrayList<String> tables = new ArrayList<String>();
		
		Iterator it = Registry.getInstance().getConfiguration().getTableMappings();
		
		while(it.hasNext())
		{
			org.hibernate.mapping.Table t = (org.hibernate.mapping.Table)it.next();

			tables.add(t.getName());
		}

		return tables;
	}
	
	public static Dialect getDialect()
	{
		SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) sessionFactory;
		return sessionFactoryImplementor.getSettings().getDialect();
	}
}