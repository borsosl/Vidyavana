package hu.vidyavana.web;

import hu.vidyavana.db.model.TocTree;
import hu.vidyavana.db.model.TocTreeItem;
import hu.vidyavana.util.Globals;

import java.util.HashMap;

public class MainPage
{
	public void service(RequestInfo ri) throws Exception
	{
		boolean redirect = false;
		HashMap<String, Object> res = PanditServlet.ajaxMap();
		if(ri.req.getParameter("username") != null) {
			res.put("afterLogin", true);
			redirect = true;
		} else {
			TocTree toc = TocTree.getView(ri.user);
			TocTreeItem initialShortTree = toc.initialShortTree();
			res.put("toc", initialShortTree);
			res.put("maxTocId", toc.maxId);
			res.put("justRegistered", ri.ses.getAttribute("justRegistered"));
			res.put("downtime", Globals.downtime);
			ri.ses.removeAttribute("justRegistered");
		}
		ri.ajaxResult = res;
		ri.ajaxText();
		ri.renderJsp(redirect ? "/redirect.jsp" : "/mainPage.jsp");
	}
}
