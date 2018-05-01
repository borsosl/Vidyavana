package hu.vidyavana.web.ctrl;

import hu.vidyavana.db.model.Storage;
import hu.vidyavana.db.model.TocTree;
import hu.vidyavana.service.TextContentService;
import hu.vidyavana.web.RequestInfo;

public class TextContentController {

    public void service(RequestInfo ri) throws Exception
    {
        ri.ajax = true;
        synchronized(Storage.SYSTEM)
        {
            Storage.SYSTEM.openForRead();
        }
        ri.toc = TocTree.getView(ri.user);

        TextContentService service = new TextContentService(ri);
        if("search".equals(ri.args[1]))
            if(ri.args.length > 2 && "hit".equals(ri.args[2]))
                service.hit();
            else
                service.search();
        else if("section".equals(ri.args[1]))
            service.section();
        else if("filter".equals(ri.args[1]))
            service.filter();
        else if("follow".equals(ri.args[1]))
            service.follow();
        else
            ri.resp.setStatus(404);
    }
}
