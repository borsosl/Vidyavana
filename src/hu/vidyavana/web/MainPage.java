package hu.vidyavana.web;

import java.util.HashMap;
import hu.vidyavana.db.dao.TocTree;
import hu.vidyavana.db.model.TocTreeItem;

public class MainPage
{
	public void service(RequestInfo ri) throws Exception
	{
		TocTree toc = TocTree.getView(ri.user);
		TocTreeItem initialShortTree = toc.initialShortTree();
		HashMap<String, Object> res = PanditServlet.ajaxMap();
		res.put("toc", initialShortTree);
		res.put("maxTocId", toc.maxId);
		ri.ajaxResult = res;
		ri.ajaxText();
		ri.renderJsp("/mainPage.jsp");
	}
}
