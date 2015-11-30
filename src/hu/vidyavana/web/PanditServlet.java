package hu.vidyavana.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hu.vidyavana.db.dao.ServerUtil;
import hu.vidyavana.db.dao.TextContent;
import hu.vidyavana.db.dao.TocTree;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;

public class PanditServlet extends HttpServlet
{
	private int errorRefNo;
	private boolean debug;
	private SimpleDateFormat dumpTimeFmt;
	
	public PanditServlet()
	{
		String debugStr = System.getProperties().getProperty("lnd.debug");
		if("1".equals(debugStr))
		{
			debug = true;
			dumpTimeFmt = new SimpleDateFormat("HH:mm");
		}
	}


	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		RequestInfo ri = RequestInfo.create();
		ri.req = req;
		ri.resp = resp;
		process(ri);
		RequestInfo.reset();
	}

	
	private void process(RequestInfo ri)
	{
        try
		{
    		ri.uri = ri.req.getPathInfo();
    		ri.resp.setContentType("text/html; charset=UTF-8");
    		ri.resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    		ri.resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
    		ri.resp.setHeader("Pragma", "no-cache");
    		ri.resp.setHeader("Expires", "-1");
    		
    		if(debug)
    			Log.debug(dumpTimeFmt.format(new Date())+": "+ri.uri);

			if(ri.uri.endsWith(".jsp"))
			{
				getServletContext().getNamedDispatcher("jsp").forward(ri.req, ri.resp);
				return;
			}
			
			String[] args = ri.args = ri.uri.substring(1).split("/");
    		
    		if(Globals.maintenance && !"util".equals(args[0]))
    		{
    			ri.renderJsp("/maintenance.jsp");
    			return;
    		}
			
			// no args
			if(args.length == 1 && args[0].isEmpty())
			{
				new MainPage().service(ri);
				return;
			}
			if("toc".equals(args[0]))
				TocTree.inst.service(ri);
			else if("txt".equals(args[0]))
				new TextContent().service(ri);
			else if("util".equals(args[0]))
				new ServerUtil().service(ri);
			else
				ri.resp.setStatus(404);
			if(ri.ajax && ri.ajaxResult != null)
			{
	    		ri.resp.setContentType("application/json; charset=UTF-8");
				writeJson(ri);
			}
		}
		catch(Exception ex)
		{
			++errorRefNo;
			Log.error("Error ref "+errorRefNo, ex);
			if(ri.ajax)
			{
				HashMap<String,Object> map = ajaxMap();
				map.put("error", refError());
				ri.ajaxResult = map;
				try
				{
					writeJson(ri);
				}
				catch(IOException ex1)
				{
					Log.error("Unable to send ajax error response.", ex1);
				}
			}
			else
			{
				try
				{
					PrintWriter wr = ri.resp.getWriter();
					wr.write(refError());
				}
				catch(IOException ex1)
				{
					Log.error("Unable to send page error response.", ex1);
				}
			}
		}
	}


	private String refError()
	{
		return "Hiba történt. Kérlek írj hibajelentést arról, hogy melyik funkciót használtad "
			+ "és hogyan. Hivatkozz erre a referenciaszámra: "+errorRefNo+". Email: lokanatha.srs@gmail.com";
	}


	private void writeJson(RequestInfo ri) throws IOException
	{
		ri.ajaxText();
		ri.resp.getWriter().write(ri.ajaxText);
	}
	
	
	public static HashMap<String, Object> ajaxMap()
	{
		return new HashMap<String, Object>();
	}
}
