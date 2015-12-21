package hu.vidyavana.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CaptureJspOutput extends HttpServletResponseWrapper
{
	private StringWriter sw = new StringWriter(50000);


	public CaptureJspOutput(HttpServletResponse response)
	{
		super(response);
	}


	@Override
	public PrintWriter getWriter() throws IOException
	{
		return new PrintWriter(sw);
	}


	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		throw new UnsupportedOperationException();
	}


	@Override
	public String toString()
	{
		return sw.toString();
	}
}
