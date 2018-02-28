package hu.vidyavana.web.ctrl;

import hu.vidyavana.db.AddBook;
import hu.vidyavana.db.model.User;
import hu.vidyavana.util.Globals;
import hu.vidyavana.util.Log;
import hu.vidyavana.web.PanditServlet;
import hu.vidyavana.web.RequestInfo;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServerUtilController
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
		if(ri.user.adminLevel != User.AdminLevel.Full)
		{
			ri.resp.setStatus(404);
			return;
		}
		if("down".equals(ri.args[1]))
			downtime(ri);
		else if("rebuild".equals(ri.args[1]))
		{
			rebuild(ri);
		}
		else if("monitor".equals(ri.args[1]))
		{
			monitorBackgroundTask(ri);
		}
		else if("rmindex".equals(ri.args[1]))
		{
			rmIndex(ri);
		}
		else
			ri.resp.setStatus(404);
	}

	private void rebuild(final RequestInfo ri) throws ServletException, IOException
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
					try
					{
						responseQueue.put(ex);
					}
					catch(InterruptedException ex1)
					{
						ex1.printStackTrace();
					}
				}
			}
		}).start();
		ServiceResponse res = new ServiceResponse();
		res.serviceTag = serviceTag;
		ri.ajaxResult = res;
		ri.ajaxText();
		ri.renderJsp("/server-monitor.jsp");
	}

	private void monitorBackgroundTask(final RequestInfo ri) throws InterruptedException
	{
		ri.ajax = true;
		Integer serviceTag = Integer.valueOf(ri.args[2]);
		BlockingQueue<Object> resp = serviceMap.get(serviceTag);
		ServiceResponse res = new ServiceResponse();
		res.text = "";
		if(resp == null)
		{
			res.finished = true;
		}
		else
		{
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
		}
		ri.ajaxResult = res;
	}

	
	private void downtime(RequestInfo ri)
	{
		String time = ri.req.getParameter("time");
		if(time != null)
		{
			time = time.trim();
			if(time.isEmpty())
				time = null;
		}
		Globals.downtime = time;
		Globals.maintenance = "most".equalsIgnoreCase(time);
		PanditServlet.okResult(ri);
	}

	private void rmIndex(final RequestInfo ri) {
		Path path = Globals.cwd.toPath().resolve("system/index");
		try {
			Files.walk(path)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
                    .forEach(File::delete);
			ri.ajaxText = "{\"success\": true, \"path\": \""+path.toString()+"\"}";
		} catch (IOException e) {
			ri.ajaxText = "{\"fail\": \""+e.getClass().getName()+" "+e.getMessage()+"\", \"path\": \""
					+path.toString()+"\"}";
		}
	}
}
