package hu.vidyavana.web;

import hu.vidyavana.db.dao.TocTree;
import hu.vidyavana.db.model.TocTreeItem;
import java.util.HashMap;

public class MainPage
{
	public void service(RequestInfo ri) throws Exception
	{
		TocTreeItem initialShortTree = TocTree.inst.initialShortTree();
		HashMap<String, Object> res = PanditServlet.ajaxMap();
		res.put("toc", initialShortTree);
		ri.ajaxResult = res;
		ri.ajaxText();
		ri.renderJsp("/mainPage.jsp");
	}
}
