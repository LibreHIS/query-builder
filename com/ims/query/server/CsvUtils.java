/*
 * Created on 16-Nov-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.server;

public class CsvUtils
{
	public String stringToCodedString(String val)
	{
		int nSpec = 0;
		char c;
		
		for(int i = 0; i < val.length(); i++)
		{
			c = val.charAt(i);
			
			if(c == '\r' || c == '\n' || c == '%' || c == 0)
				nSpec++;
		}
		
		if(nSpec == 0)
			return val;
		
		StringBuffer sb = new StringBuffer(val.length() + nSpec);
		
		for(int i = 0; i < val.length(); i++)
		{
			c = val.charAt(i);
			
			switch(c)
			{
				case '\r':
					sb.append('%');
					sb.append('c');
					break;
				case '\n':
					sb.append('%');
					sb.append('n');
					break;
				case 0:
					sb.append('%');
					sb.append('0');
					break;
				case '%':
					sb.append('%');
					sb.append('%');
					break;
				default:
					sb.append(c);
					break;
			}
		}
		
		return sb.toString();
	}

	public String ansiQuotedStr(String string, char q)
	{
		StringBuffer sb = new StringBuffer();
		char c;
		
		sb.append(q);
		for(int i = 0; i < string.length(); i++)
		{
			c = string.charAt(i);
			sb.append(c);
			
			if(c == q)
				sb.append(c);
		}
		sb.append(q);
		
		return sb.toString();
	}

	public String getParamTypeForDelphi(String type)
	{
		if(type.equalsIgnoreCase("java.lang.Integer"))
		{
			return "Integer";
		}
		else if(type.equalsIgnoreCase("java.math.BigInteger"))
		{
			return "Integer";
		}
		else if(type.equalsIgnoreCase("java.lang.Long"))
		{
			return "Integer";
		}
		else if(type.equalsIgnoreCase("java.lang.Short"))
		{
			return "Integer";
		}
		else if(type.equalsIgnoreCase("java.lang.Boolean"))
		{
			return "Boolean";
		}
		else if(type.equalsIgnoreCase("java.lang.String"))
		{
			return "String";
			//return "Memo";
		}
		else if(type.equalsIgnoreCase("java.math.BigDecimal"))
		{
			return "Float";
		}
		else if(type.equalsIgnoreCase("java.lang.Float"))
		{
			return "Float";
		}
		else if(type.equalsIgnoreCase("java.lang.Double"))
		{
			return "Float";
		}
		else if(type.equalsIgnoreCase("java.sql.Date"))
		{
			return "Date";
		}
		else if(type.equalsIgnoreCase("java.sql.Time"))
		{
			return "Time";
		}
		else if(type.equalsIgnoreCase("java.util.Date"))
		{
			return "DateTime";
		}
		else
		{
			return "Integer";
		}
	}
	
}
