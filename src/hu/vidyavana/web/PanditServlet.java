package hu.vidyavana.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.web.ctrl.*;

public class PanditServlet extends HttpServlet
{
	private int errorRefNo;
	
	public PanditServlet()
	{
	}


	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			RequestInfo ri = (RequestInfo) req.getAttribute("_ri");
			if(ri == null)
			{
				ri = RequestInfo.create();
			}
			ri.req = req;
			ri.resp = resp;
			process(ri);
		}
		finally
		{
			RequestInfo.reset();
		}
	}

	
	private void process(RequestInfo ri)
	{
        try
		{
    		ri.uri = ri.req.getPathInfo();
			String[] args = ri.args = ri.uri.substring(1).split("/");
			boolean ctxRootReq = args.length == 1 && args[0].isEmpty();
    		ri.ses = ri.req.getSession();
    		if(ri.user == null && ri.ses != null)
    			ri.user = (User) ri.ses.getAttribute("user");
			
    		ri.resp.setContentType("text/html; charset=UTF-8");
    		ri.resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
    		ri.resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
    		ri.resp.setHeader("Pragma", "no-cache");
    		ri.resp.setHeader("Expires", "-1");

    		String userInfo = ri.user == null ? "" : "["+ri.user.email+"] ";
    		Log.activity(userInfo + ri.uri);
    		// Log.finest("Requested Session Id: " + ri.req.getRequestedSessionId() + " / " + ri.ses.getId());

    		if(Globals.maintenance && !"util".equals(args[0]))
    		{
    			if(args.length > 1)
    			{
	    			ri.ajaxText = "{\"error\": \"maintenance\"}";
	    			ajaxResponse(ri);
	    			return;
    			}
    			if(!"maintenance.jsp".equals(args[0]))
    			{
        			ri.renderJsp("/maintenance.jsp");
        			return;
    			}
    		}

    		// make sure session pages call ri.check(), so that direct no-session access would fail
    		// when ri has not been bound to request
			if(ri.uri.endsWith(".jsp"))
			{
				getServletContext().getNamedDispatcher("jsp").forward(ri.req, ri.resp);
				return;
			}
			
			// no-session requests
			if("auth".equals(args[0]))
				new AuthController().service(ri);
			else {
	    		// check session
	    		if(ri.user == null)
	    		{
	    			if(ctxRootReq || ri.args.length == 1 && "admin".equals(args[0]))
	    			{
	    				new AuthController().login(ri);
	    				return;
	    			}
	    			ri.ajax = true;
	    			ri.ajaxText = "{\"error\": \"expired\"}";
	    		}
	    		else
	    		{
					// session requests
					if(ctxRootReq)
					{
						new MainPage().service(ri);
						return;
					}
					else if("toc".equals(args[0]))
						new TocTreeController().service(ri);
					else if("txt".equals(args[0]))
						new TextContentController().service(ri);
					else if("util".equals(args[0]))
						new ServerUtilController().service(ri);
					else if("admin".equals(args[0]))
						new AdminController().service(ri);
					else
						ri.resp.setStatus(404);
	    		}
			}
			if(ri.ajaxResult != null || ri.ajaxText != null)
	    		ajaxResponse(ri);
		}
		catch(Exception ex)
		{
			++errorRefNo;
			Log.error("Error ref "+errorRefNo, ex);
			if(ri.ajax)
			{
				HashMap<String,Object> map = ajaxMap();
				map.put("error", errorRefNo);
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
					wr.write("Hiba történt. <a href=\"mailto:dev@pandit.hu?subject=Hibajelentés ("
						+errorRefNo+")\">Beszámolok róla</a>");
				}
				catch(IOException ex1)
				{
					Log.error("Unable to send page error response.", ex1);
				}
			}
		}
	}


	protected void ajaxResponse(RequestInfo ri) throws IOException
	{
		ri.resp.setContentType("application/json; charset=UTF-8");
		writeJson(ri);
	}


	private void writeJson(RequestInfo ri) throws IOException
	{
		if(ri.ajaxText == null)
		{
			if(ri.ajaxResult != null)
				ri.ajaxText();
			else
				ri.ajaxText = "{}";
		}
		ri.resp.getWriter().write(ri.ajaxText);
	}
	
	
	public static HashMap<String, Object> ajaxMap()
	{
		return new HashMap<String, Object>();
	}
	
	
	public static void okResult(RequestInfo ri)
	{
		ri.ajaxText = "{\"ok\": true}";
	}
	
	
	public static void failResult(RequestInfo ri)
	{
		ri.ajaxText = "{\"fail\": true}";
	}
	
	
	public static void messageResult(RequestInfo ri, String msg)
	{
		ri.ajaxText = "{\"message\": \"" + msg + "\"}";
	}
}
