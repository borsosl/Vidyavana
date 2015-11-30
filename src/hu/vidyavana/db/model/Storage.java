package hu.vidyavana.db.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import hu.vidyavana.util.Globals;

public class Storage
{
	public static final int CURRENT_VERSION = 1;
	public static Storage SYSTEM = forUser(null);

	public byte version = CURRENT_VERSION;
	public String userName = "server";

	public File file;
	public RandomAccessFile handle;
	private String mode;
	private Map<Integer, BookSegment> segments;
	private TreeMap<Integer, Integer> segmentOrder;
	private List<BookSegment> inactiveSegment;
	private boolean encrypted;
	private boolean locked;
	
	
	private Storage(String user)
	{
		if(user == null)
			file = new File(Globals.cwd, "system/text.bin");
		else
			file = new File(Globals.cwd, "users/"+user+"/text.bin");
	}
	
	
	public static Storage forUser(String user)
	{
		if(user == null && SYSTEM != null)
			return SYSTEM;
		// TODO cache for some time
		return new Storage(user);
	}

	
	public boolean open(String mode) throws IOException
	{
		if(locked)
			throw new IOException("locked");
		if(mode.equals(this.mode))
			return true;
		close();
		try
		{
			handle = new RandomAccessFile(file, mode);
			this.mode = mode;
			return true;
		}
		catch(FileNotFoundException ex)
		{
			handle = null;
			this.mode = null;
			if("rw".equals(mode))
				throw new RuntimeException("Writing storage "+file, ex);
			return false;
		}
	}
	
	
	public void openForWrite() throws IOException
	{
		if("rw".equals(mode))
			return;
		boolean init = mode == null;
		if(!open("r"))
		{
			open("rw");
			writeHead();
		}
		else
		{
			if(init)
				readMeta();
			open("rw");
			handle.seek(handle.length());
		}
	}
	
	
	public void openForRead() throws IOException
	{
		boolean init = mode == null;
		if(!open("r"))
		{
			open("rw");
			writeHead();
			open("r");
		}
		if(init)
			readMeta();
	}

	
	public void close() throws IOException
	{
		if(handle != null)
			handle.close();
		handle = null;
		mode = null;
	}


	public void lock(boolean lock)
	{
		locked = lock;
	}

	
	public void writeHead() throws IOException
	{
		handle.writeBytes("Vidy");
		handle.writeByte(version);
		handle.writeUTF(userName);
		// segments are written separately
	}
	

	public void readMeta() throws IOException
	{
		if(handle.readByte()!='V' || handle.readByte()!='i' ||
			handle.readByte()!='d' || handle.readByte()!='y')
				throw new RuntimeException("Invalid file");
		version = handle.readByte();
		userName = handle.readUTF();
		segments = new HashMap<Integer, BookSegment>();
		segmentOrder = new TreeMap<Integer, Integer>();
		while(true)
		{
			BookSegment bs = BookSegment.readMeta(handle, encrypted);
			if(bs == null)
				break;
			if(bs.inactive)
				inactiveSegment.add(bs);
			else
			{
				int bookSeg = bs.id();
				segments.put(bookSeg, bs);
				int prioritySeg = bs.priority();
				segmentOrder.put(prioritySeg, bookSeg);
			}
		}
	}
	
	
	public BookSegment segment(int id)
	{
		return segments.get(id);
	}
	
	
	public TreeMap<Integer,Integer> segmentOrder()
	{
		return segmentOrder;
	}
	
	
	public void addSegment(BookSegment bs)
	{
		segments.put(bs.id(), bs);
	}



	public void inactivateSegment(BookSegment bs) throws IOException
	{
		String oldMode = mode;
		open("rw");
		bs.inactivate(handle);
		open(oldMode);
	}


	public boolean isEncrypted()
	{
		return encrypted;
	}


	public void setEncrypted(boolean encrypted)
	{
		this.encrypted = encrypted;
	}
}
