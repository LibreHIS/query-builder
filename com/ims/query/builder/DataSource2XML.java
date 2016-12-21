package com.ims.query.builder;
/*
 * Created on Jan 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
import java.util.ArrayList;

/**
 * @author vpurdila
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DataSource2XML
{
	public static final String JL_INTEGER       = "java.lang.Integer";
	public static final String JM_BIGINTEGER    = "java.math.BigInteger";
	public static final String JL_LONG          = "java.lang.Long";
	public static final String JL_SHORT         = "java.lang.Short";
	public static final String JL_BOOLEAN       = "java.lang.Boolean";
	public static final String JL_STRING        = "java.lang.String";
	public static final String JM_BIGDECIMAL    = "java.math.BigDecimal";
	public static final String JL_FLOAT         = "java.lang.Float";
	public static final String JL_DOUBLE        = "java.lang.Double";
	public static final String JS_DATE          = "java.sql.Date";
	public static final String JS_TIME          = "java.sql.Time";
	public static final String JU_DATE          = "java.util.Date";
	public static final String JL_OBJECT        = "java.lang.Object";
	
	private ArrayList datasets = null;
	
	public DataSource2XML(ArrayList datasets)
	{
		super();
		this.datasets = datasets;
	}
	
	public String toXml()
	{
		String type;
		char c = 'c';
		
		if(datasets == null)
			return null;
		
		StringBuffer sb = new StringBuffer(16*1024);
		
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<ds>");
			for (int i = 0; i < datasets.size(); i++)
			{
				DataSource ds = (DataSource)datasets.get(i);
				if(ds != null)
				{
					sb.append("<d name=\"" + ds.getName() + "\" hasFields=\"" + (ds.hasFields() ? "true" : "false") + "\">");
						for (int j = 0; j < ds.getFields().size(); j++)
						{
							DataField df = (DataField)ds.getFields().get(j);
							
							type = df.getType();
							
							if(type.equalsIgnoreCase(JL_INTEGER))
								c = '0';
							else if(type.equalsIgnoreCase(JM_BIGINTEGER))
								c = '1';
							else if(type.equalsIgnoreCase(JL_LONG))
								c = '2';
							else if(type.equalsIgnoreCase(JL_SHORT))
								c = '3';
							else if(type.equalsIgnoreCase(JL_BOOLEAN))
								c = '4';
							else if(type.equalsIgnoreCase(JL_STRING))
								c = '5';
							else if(type.equalsIgnoreCase(JM_BIGDECIMAL))
								c = '6';
							else if(type.equalsIgnoreCase(JL_FLOAT))
								c = '7';
							else if(type.equalsIgnoreCase(JL_DOUBLE))
								c = '8';
							else if(type.equalsIgnoreCase(JS_DATE))
								c = '9';
							else if(type.equalsIgnoreCase(JS_TIME))
								c = 'a';
							else if(type.equalsIgnoreCase(JU_DATE))
								c = 'b';
							else if(type.equalsIgnoreCase(JL_OBJECT))
								c = 'c';
							
							sb.append("<f n=\"" + df.getName() + "\" t=\"" + c + "\">");
								//sb.append("<n>" + df.getName() + "</n>");
								for(int k=0; k < df.getValues().size(); k++)
								{
									sb.append("<v>" + verifyAttribute((String)df.getValues().get(k)) + "</v>");
								}
								//sb.append("<t>" + c + "</t>");
							sb.append("</f>");
						}
					sb.append("</d>");
				}
			}
		sb.append("</ds>");
		
		return sb.toString();
	}
	
	public String verifyAttribute(String attr)
	{
		StringBuffer sb = new StringBuffer(attr.length() * 2);
		char c;

		for(int i = 0; i < attr.length(); i++)
		{
			c = attr.charAt(i);
			
			if(c == '&')
				sb.append("&amp;");
			else if(c == '<')
				sb.append("&lt;");
			else if(c == '>')
				sb.append("&gt;");
			else if(c == '\'')
				sb.append("&apos;");
			else if(c == '"')
				sb.append("&quot;");
			else
				sb.append(c);
		}
		
		return sb.toString();
	}
}
