package hu.vidyavana.db.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import hu.vidyavana.db.AddBook;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.web.RequestInfo;

public class ServerUtil
{
	static class ServiceResponse
	{
		int serviceTag;
		boolean finished;
		String text;
	}
	
	private static Map<Integer, BlockingQueue<Object>> serviceMap = new HashMap<>();

	public void service(final RequestInfo ri) throws Exception
	{
		if("rebuild".equals(ri.args[1]))
		{
			int serviceTag = (int) System.currentTimeMillis();
			final BlockingQueue<Object> responseQueue = new ArrayBlockingQueue<>(100);
			serviceMap.put(serviceTag, responseQueue);
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						String user = ri.args.length > 2 ? ri.args[2] : null;
						AddBook.rebuildOnServer(user, responseQueue);
					}
					catch(Exception ex)
					{
						responseQueue.add(ex);
					}
				}
			}).start();
			ServiceResponse res = new ServiceResponse();
			res.serviceTag = serviceTag;
			ri.ajaxResult = res;
			ri.ajaxText();
			ri.renderJsp("/server-monitor.jsp");
		}
		else if("monitor".equals(ri.args[1]))
		{
			Integer serviceTag = Integer.valueOf(ri.args[2]);
			BlockingQueue<Object> resp = serviceMap.get(serviceTag);
			ServiceResponse res = new ServiceResponse();
			res.text = "";
			Object data = resp.poll();
			if(data == null)
				// wait for one next
				data = resp.poll(30, TimeUnit.SECONDS);
			while(data != null)
			{
				if(data == Boolean.TRUE)
				{
					res.finished = true;
					break;
				}
				else if(data instanceof Exception)
				{
					res.text += Log.stackTrace((Throwable) data);
					res.finished = true;
					break;
				}
				res.text += data.toString() + "\r\n";
				data = resp.poll();
			}
			if(res.finished)
				serviceMap.remove(serviceTag);
			ri.ajaxResult = res;
			ri.ajax = true;
		}
		else if("maint".equals(ri.args[1]))
		{
			boolean val = true;
			if(ri.args.length > 2)
				val = false;
			Globals.maintenance = val;
		}
		else
			ri.resp.setStatus(404);
	}
}
