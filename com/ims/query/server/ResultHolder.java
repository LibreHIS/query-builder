/*
 * Created on Apr 18, 2005
 *
 */
package com.ims.query.server;

/**
 * @author vpurdila
 *
 */
public class ResultHolder
{
	private byte[] result = null;
	private String mimeType = "";
	
	public ResultHolder()
	{
	}
	
	public ResultHolder(byte[] result, String mimeType)
	{
		this.result = result;
		this.mimeType = mimeType;
	}
	
	public String getMimeType()
	{
		return mimeType;
	}
	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}
	public byte[] getResult()
	{
		return result;
	}
	public void setResult(byte[] result)
	{
		this.result = result;
	}
}
