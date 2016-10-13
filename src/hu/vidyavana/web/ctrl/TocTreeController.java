package hu.vidyavana.web.ctrl;

import hu.vidyavana.db.model.TocTree;
import hu.vidyavana.web.RequestInfo;

public class TocTreeController
{
	public void service(RequestInfo ri) throws Exception
	{
		ri.ajax = true;
		TocTree toc = TocTree.getView(ri.user);
		if("get".equals(ri.args[1])) {
			int id = Integer.parseInt(ri.args[2]);
			ri.ajaxResult = toc.treeNode(id);
		} else if("root".equals(ri.args[1])) {
			ri.ajaxResult = toc.initialShortTree();
		}
	}
}
