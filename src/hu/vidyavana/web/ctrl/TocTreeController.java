package hu.vidyavana.web.ctrl;

import hu.vidyavana.db.model.TocTree;
import hu.vidyavana.web.RequestInfo;

public class TocTreeController
{
	public void service(RequestInfo ri) throws Exception
	{
		// args[1] == get
		ri.ajax = true;
		int id = Integer.parseInt(ri.args[2]);
		ri.ajaxResult = TocTree.inst.treeNode(id);
	}
}
