<?xml version="1.0" encoding="UTF-8"?>
<%@page import="com.citrixosd.utils.SitemapHelper"%>
<%@include file="/libs/foundation/global.jsp"%>
<%@include file="/apps/citrixosd/global.jsp"%>

<%
	final int maxLevel = properties.get("level",0);
	final boolean hideChangeFreq = properties.get("hideChangeFreq",false);
	final boolean hidePriority = properties.get("hidePriority",false);
	final String defaultPriority = properties.get("defaultPriority","0.8");
	final String defaultChangeFreq = properties.get("defaultChangeFreq","monthly");
	
    final Page rootPage = currentPage.getAbsoluteParent(2); //'content/website/en_uS'
    SitemapHelper sitemapHelper = new SitemapHelper(resource, rootPage, defaultPriority, defaultChangeFreq, request);
    response.setContentType("text/xml");
    
    if(maxLevel != 0) {
    	sitemapHelper.drawXML(out, maxLevel, hidePriority, hideChangeFreq); 
    }else {
    	sitemapHelper.drawXML(out, hidePriority, hideChangeFreq); 
    }
%>
