/*
 * Created on 2 Nov 2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.server;

import ims.domain.DomainSession;
import ims.domain.impl.DomainImplFlyweightFactory;
import ims.framework.enumerations.SystemLogLevel;
import ims.framework.enumerations.SystemLogType;

import com.ims.query.server.dummy.impl.IDummy;

public class LogDeployReportHelper
{
	public void createSystemLogEntry(String msg, boolean bLogReport, SystemLogLevel level)
	{
		IDummy impl;

		if(bLogReport)
		{
			try
			{
				impl = (IDummy) getDomainImpl("com.ims.query.server.dummy.impl.DummyImpl");
				impl.createSystemLogEntry(SystemLogType.DEPLOY_REPORTS, level, msg);
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
}
