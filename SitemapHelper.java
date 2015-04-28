package com.citrixosd.utils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.servlet.http.HttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;

public class SitemapHelper {
	private static final String W3CDTF_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	private LinkedList<Link> links = new LinkedList<Link>();
	private StringBuffer sitemapXML = null;

	/**
	 * @author sugupta,vishal.gupta@citrix.com
	 * inner class to hold page details
	 */
	public class Link {
		private String path;
		private String title;
		private int level;
		private String lastmod;
		private String changefreq;
		private String priority;

		public Link(String path, String title, String lastmod, String changefreq, String priority, int level) {
			this.path = path;
			this.level = level;
			this.setTitle(title);
			this.setLastmod(lastmod);
			this.setChangefreq(changefreq);
			this.setPriority(priority);
		}

		public String getPath() {
			return path;
		}

		public int getLevel() {
			return level;
		}

		public String getLastmod() {
			return lastmod;
		}

		public void setLastmod(String lastmod) {
			this.lastmod = lastmod;
		}

		public String getChangefreq() {
			return changefreq;
		}

		public void setChangefreq(String changefreq) {
			this.changefreq = changefreq;
		}

		public String getPriority() {
			return priority;
		}

		public void setPriority(String priority) {
			this.priority = priority;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	/**
	 * @param rootPage
	 * @throws LoginException 
	 * @throws RepositoryException 
	 * @throws PathNotFoundException 
	 * @throws ValueFormatException 
	 */
	public SitemapHelper(Resource resource, Page rootPage, String defaultPriority, String defaultChangeFreq, HttpServletRequest request) throws LoginException, ValueFormatException, PathNotFoundException, RepositoryException {
		buildLinkAndChildren(resource, rootPage, 0, defaultPriority, defaultChangeFreq, request);
	}
	
	/**
	 * @param resource.getResourceResolver().getResource 
	 * @param page
	 * @param level
	 * This method generate Page and children details  
	 * @param defaultChangeFreq 
	 * @param defaultPriority 
	 * @throws RepositoryException 
	 * @throws PathNotFoundException 
	 * @throws ValueFormatException 
	 */
	private void buildLinkAndChildren(Resource resource, Page page, int level, String defaultPriority, String defaultChangeFreq, HttpServletRequest request) throws ValueFormatException, PathNotFoundException, RepositoryException {
		if(page != null) {
			Node node = resource.getResourceResolver().getResource(page.getPath()).adaptTo(Node.class).getNode("jcr:content");
			String lastmod = getW3CDTFDate(page.getLastModified().getTime()); //This date should be in W3C Datetime format
			String hideInSiteMap = node.hasProperty("hideInSiteMap") ? node.getProperty("hideInSiteMap").getString() : "false";
			String priority = node.hasProperty("priority") ? node.getProperty("priority").getString() : defaultPriority;
			String changefreq = node.hasProperty("changefreq") ? node.getProperty("changefreq").getString() : defaultChangeFreq;
			String template = node.hasProperty("cq:template") ? node.getProperty("cq:template").getString() : "";
			String path = ContextRootTransformUtil.transformedPath(page.getPath(), request);
			
			if(!hideInSiteMap.equals("true") && !template.contains("sitemap") && !path.contains("redirects") && !path.contains("references")) {
				links.add(new Link(path, page.getTitle() != null ? page.getTitle() : page.getName(), lastmod , changefreq, priority, level));
				Iterator<Page> children = page.listChildren(new PageFilter());
				
				while(children.hasNext()) {
					Page child = children.next();
					buildLinkAndChildren(resource, child, level+1, defaultPriority, defaultChangeFreq, request);
				}
			}
		}
	}
	
	/**
	 * @param w
	 * @param request
	 * This method generate Sitemap XML
	 * @throws RepositoryException 
	 */
	public void drawXML(Writer w , boolean hidePriority, boolean hideChangeFreq) throws IOException, RepositoryException{
		PrintWriter out = new PrintWriter(w);
		sitemapXML = new StringBuffer("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

		for(Link aLink: links){
			sitemapXML.append("<url>\n");
			if(aLink.getPath() != null) {
				sitemapXML.append("<loc>" + aLink.getPath() + "</loc>\n");
			}
			if(aLink.getLastmod() != null) {
				sitemapXML.append("<lastmod>" + aLink.getLastmod() + "</lastmod>\n");
			}
			if(!hideChangeFreq) {
				sitemapXML.append("<changefreq>" + aLink.getChangefreq() + "</changefreq>\n");
			}
			if(!hidePriority) {
				sitemapXML.append("<priority>" + aLink.getPriority() + "</priority>\n");
			}
			sitemapXML.append("</url>\n");
		}
		sitemapXML.append("</urlset>");
		out.print(sitemapXML.toString());
	}
	
	/**
	 * @param w
	 * @param maxlevel
	 * This method generate Sitemap XML for the pages those page level is less than provide Max page level  
	 * @throws RepositoryException 
	 */
	public void drawXML(Writer w, int maxlevel , boolean hidePriority, boolean hideChangeFreq) throws IOException, RepositoryException{
		PrintWriter out = new PrintWriter(w);
		sitemapXML = new StringBuffer("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

		for(Link aLink: links) {
			if(aLink.getLevel() > maxlevel) continue; 
			sitemapXML.append("<url>\n");
			if(aLink.getPath() != null) {
				sitemapXML.append("<loc>" + aLink.getPath() + "</loc>\n");
			}
			if(aLink.getLastmod() != null) {
				sitemapXML.append("<lastmod>" + aLink.getLastmod() + "</lastmod>\n");
			}
			if(!hideChangeFreq) {
				sitemapXML.append("<changefreq>" + aLink.getChangefreq() + "</changefreq>\n");
			}
			if(!hidePriority) {
				sitemapXML.append("<priority>" + aLink.getPriority() + "</priority>\n");
			}
			sitemapXML.append("</url>\n");
		}
		sitemapXML.append("</urlset>");
		out.print(sitemapXML.toString()); 
	}
	
	/**
	 * @param date
	 * @return W3C Datetime format String
	 */
	public static String getW3CDTFDate(Date date) {
		String str = new SimpleDateFormat(W3CDTF_FORMAT).format(date);
		str = str.substring(0, str.length() - 2) + ":" + str.substring(str.length() - 2);
		return str;
	}
	
	/**
	 * @return Links
	 */
	public LinkedList<Link> getLinks() {
		return links;
	}
}
