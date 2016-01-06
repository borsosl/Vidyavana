package hu.vidyavana.web;

import java.util.HashMap;
import hu.vidyavana.db.model.TocTree;
import hu.vidyavana.db.model.TocTreeItem;
import hu.vidyavana.util.Globals;

public class MainPage
{
	public void service(RequestInfo ri) throws Exception
	{
		TocTree toc = TocTree.getView(ri.user);
		TocTreeItem initialShortTree = toc.initialShortTree();
		HashMap<String, Object> res = PanditServlet.ajaxMap();
		res.put("toc", initialShortTree);
		res.put("maxTocId", toc.maxId);
		res.put("justRegistered", ri.ses.getAttribute("justRegistered"));
		res.put("downtime", Globals.downtime);
		ri.ses.removeAttribute("justRegistered");
		ri.ajaxResult = res;
		ri.ajaxText();
		ri.renderJsp("/mainPage.jsp");
	}
}
