/*
 * Created on Jan 25, 2005
 */
package com.ims.query.builder.parser;

/**
 * @author vpurdila
 *
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class VelocityGenerator
{
	private static VelocityEngine engine = null;
	
	public VelocityGenerator() throws Exception
	{
		//engine.init();
		if(engine == null)
			configureVelocity(engine);
	}
	
	private synchronized void configureVelocity(VelocityEngine engine2) throws Exception
	{
		engine = new VelocityEngine();
		
		Properties props = new Properties();
	    props.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".class", ClasspathResourceLoader.class.getName());
	    
	    props.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".class", ClasspathResourceLoader.class.getName());
	    
	    engine.init(props);		
	}

	public synchronized String Generate(Object obj, String templateName) throws Exception
	{
        VelocityContext context = new VelocityContext();
        context.put("queries", obj);

        StringWriter writer = new StringWriter();
        String template = getFileContent(templateName);

        engine.evaluate(context, writer, "", template);
        
        writer.flush();
        writer.close();
        
        return writer.toString();
	}
	
	public String getFileContent(String fileName)
	{
		InputStream is = null;
		
		is = ClassLoader.getSystemResourceAsStream(fileName);
		
		if(is == null)
			is = this.getClass().getResourceAsStream(fileName);
		
		byte[] content = new byte[512];
		int nread = 0;
		StringBuffer sb = new StringBuffer(10000);
		
		try
		{
			while(is.available() > 0)
			{
				nread = is.read(content);
				
				if(nread > 0)
					sb.append(new String(content, 0, nread));
			}
			
			is.close();
		} catch (IOException e2)
		{
			e2.printStackTrace();
		}

		return sb.toString();
	}
}
