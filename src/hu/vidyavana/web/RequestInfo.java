package hu.vidyavana.web;

import com.google.gson.GsonBuilder;
import hu.vidyavana.db.model.TocTree;
import hu.vidyavana.db.model.User;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RequestInfo
{
	private static ThreadLocal<RequestInfo> instance = new ThreadLocal<RequestInfo>();
	
	public HttpServletRequest req;
	public HttpServletResponse resp;
	public HttpSession ses;
	public boolean admin;
	public User user;
	public TocTree toc;
	public String uri;
	public String[] args;
	public boolean ajax;
	public Object ajaxResult;
	public String ajaxText;

	
	public static RequestInfo create()
	{
		RequestInfo o = new RequestInfo();
		instance.set(o);
		return o;
	}

	
	public static RequestInfo get()
	{
		 RequestInfo o = instance.get();
		 if(o == null)
			 throw new IllegalStateException("Thread local not created");
		 return o;
	}

	
	public static void reset()
	{
		 instance.remove();
	}
	
	
	public void check()
	{
		// that the object exists
	}
	
	
	public void ajaxText()
	{
		ajaxText = new GsonBuilder().disableHtmlEscaping().create().toJson(ajaxResult);
	}
	
	
	public void renderJsp(String path) throws ServletException, IOException
	{
		// dispatcher may run on another thread
		req.setAttribute("_ri", this);
		req.getRequestDispatcher(path).forward(req, resp);
	}

	
	public void renderAjaxTemplate(String path, Object data) throws Exception
	{
		String html = getTemplate(path);
		renderAjaxTemplateString(html, data);
	}

	public String getTemplate(String path) throws IOException {
		ServletContext ctx = req.getServletContext();
		return new String(Files.readAllBytes(Paths.get(ctx.getRealPath(path))), "UTF-8");
	}

	public void renderAjaxTemplateString(String html, Object data) throws Exception
	{
		Pattern rex = Pattern.compile("[\\r\\n\\t]+");
		html = rex.matcher(html).replaceAll("");
		Map<String, Object> res = new HashMap<>();
		res.put("html", html);
		if(data != null)
			res.put("data", data);
		ajaxResult = res;
		ajaxText();
	}


	/**
	 * @deprecated Produces empty response.
	 */
	@Deprecated
	public void renderAjaxJsp(String path, Object data) throws Exception
	{
		req.setAttribute("_ri", this);
		CaptureJspOutput respWrapper = new CaptureJspOutput(resp);
		req.getRequestDispatcher(path).forward(req, respWrapper);
		resp.reset();
		String html = respWrapper.toString();
		Pattern rex = Pattern.compile("[\\r\\n\\t]+");
		html = rex.matcher(html).replaceAll("");
		Map<String, Object> res = new HashMap<>();
		res.put("html", html);
		if(data != null)
			res.put("data", data);
		ajaxResult = res;
		ajaxText();
	}
}
