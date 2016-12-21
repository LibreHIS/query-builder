/*
 * Created on 5 Feb 2013
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.builder;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class Compressor
{
	private ByteArrayOutputStream baos;
	private GZIPOutputStream gzos;
	private BufferedWriter writer;

	public Compressor() throws IOException
	{
		baos = new ByteArrayOutputStream();
		gzos = new GZIPOutputStream(baos);
		writer = new BufferedWriter(new OutputStreamWriter(gzos, "UTF-8"));
	}
	
	public void append(CharSequence seq) throws IOException
	{
		writer.append(seq);
	}
	
	public byte[] getContent() throws IOException
	{
		writer.close();
		gzos.finish();

		try
		{
			return baos.toByteArray();
		}
		finally
		{
			baos.close();
		}	
				
	}
}
