/*
 * Created on 3 Oct 2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.server;

import java.math.BigDecimal;

public class ReportInfo
{
	BigDecimal templateId;
	String templateName;
	BigDecimal reportId;
	String reportName;
	Integer imsId;

	public ReportInfo()
	{
	}

	public ReportInfo(BigDecimal templateId, String templateName, BigDecimal reportId, String reportName, Integer imsId)
	{
		this.templateId = templateId;
		this.templateName = templateName;
		this.reportId = reportId;
		this.reportName = reportName;
		this.imsId = imsId;
	}

	public BigDecimal getTemplateId()
	{
		return templateId;
	}

	public void setTemplateId(BigDecimal templateId)
	{
		this.templateId = templateId;
	}

	public String getTemplateName()
	{
		return templateName;
	}

	public void setTemplateName(String templateName)
	{
		this.templateName = templateName;
	}

	public BigDecimal getReportId()
	{
		return reportId;
	}

	public void setReportId(BigDecimal reportId)
	{
		this.reportId = reportId;
	}

	public String getReportName()
	{
		return reportName;
	}

	public void setReportName(String reportName)
	{
		this.reportName = reportName;
	}

	public Integer getImsId()
	{
		return imsId;
	}

	public void setImsId(Integer imsId)
	{
		this.imsId = imsId;
	}

}
