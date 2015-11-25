package hu.vidyavana.db.model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class BookSegment
{
	public static final int CURRENT_VERSION = 1;

	public int len;
	public byte version;
	public boolean inactive;
	public short bookId;
	public byte segment;
	public String title;
	public int priority;		// TODO user or system priority from settings file
	public int repoVersion;
	public StorageTocItem[] contents;
	public int[] paraPtr;
	public StoragePara[] para;
	public List<StorageMultimedia> mm;
	
	
	// derived
	public int paraNum;
	private long startOfs;
	private long paraAbsOfs;
	private boolean encrypted;


	

	public void write(Storage sroot) throws IOException
	{
		RandomAccessFile out = sroot.handle;
		if(para.length > Short.MAX_VALUE)
			throw new RuntimeException("Too many paragraphs in book. Divide it into segments.");
		startOfs = out.length();
		out.seek(startOfs);
		out.writeInt(0);		// set length after we know it
		out.writeByte(CURRENT_VERSION);
		out.writeBoolean(inactive);
		out.writeShort(bookId);
		out.writeByte(segment);
		out.writeUTF(title);
		out.writeShort(priority);
		out.writeInt(repoVersion);
		
		out.writeShort(contents.length);
		for(StorageTocItem toci : contents)
			toci.write(out);

		paraPtr = new int[para.length];
		boolean enc = sroot.isEncrypted();
		int pOfs = 0;
		int pi = 0;
		for(StoragePara p : para)
		{
			p.encode(enc);
			pOfs += p.encText.length+1;
			paraPtr[pi++] = pOfs;
		}
		
		out.writeShort(para.length);
		ByteBuffer ptrBuf = ByteBuffer.allocate(paraPtr.length*Integer.BYTES);
		IntBuffer intBuf = ptrBuf.asIntBuffer();
		intBuf.put(paraPtr);
		out.getChannel().write(ptrBuf);

		for(StoragePara p : para)
		{
			p.write(out);
			p.encText = null;
		}

		if(mm != null)
			for(StorageMultimedia m : mm)
				m.write(out);

		long endOfs = out.getFilePointer();
		out.seek(startOfs);
		out.writeInt((int)(endOfs-startOfs));
	}



	public static BookSegment readMeta(RandomAccessFile in, boolean encrypted) throws IOException
	{
		long pos = in.getFilePointer();
		if(pos == in.length())
			return null;
		BookSegment bs = new BookSegment();
		bs.startOfs = pos;
		bs.len = in.readInt();
		bs.version = in.readByte();
		bs.inactive = in.readBoolean();
		bs.bookId = in.readShort();
		bs.segment = in.readByte();
		bs.title = in.readUTF();
		bs.priority = in.readShort();
		bs.repoVersion = in.readInt();
		
		int contentsLen = in.readShort();
		bs.contents = new StorageTocItem[contentsLen];
		for(int i=0; i<contentsLen; ++i)
			bs.contents[i] = StorageTocItem.read(in);
		
		bs.paraNum = in.readShort();
		ByteBuffer ptrBuf = ByteBuffer.allocate(bs.paraNum*Integer.BYTES);
		in.getChannel().read(ptrBuf);
		ptrBuf.flip();
		IntBuffer intBuf = ptrBuf.asIntBuffer();
		bs.paraPtr = new int[bs.paraNum];
		intBuf.get(bs.paraPtr);

		// not read content, but skip to segment end
		bs.paraAbsOfs = in.getFilePointer();
		in.seek(pos + bs.len);
		return bs;
	}


	public List<StoragePara> readRange(RandomAccessFile in, int startPara, int endPara) throws IOException
	{
		if(startPara >= paraPtr.length)
			return null;
		List<StoragePara> para = new ArrayList<StoragePara>();
		int stPtr = startPara==0 ? 0 : paraPtr[startPara-1];
		in.seek(paraAbsOfs+stPtr);
		for(int p = startPara; p<endPara; ++p)
		{
			if(p >= paraPtr.length)
				break;
			int endPtr = paraPtr[p];
			int len = endPtr-stPtr;
			StoragePara sp = new StoragePara();
			sp.read(in, len, encrypted);
			para.add(sp);
			stPtr = endPtr;
		}
		return para;
	}


	public int id()
	{
		return segment<<16 | bookId;
	}


	public int priority()
	{
		return priority<<16 | segment;
	}


	public void inactivate(RandomAccessFile raf) throws IOException
	{
		raf.seek(startOfs+Integer.BYTES);
		raf.writeBoolean(true);
	}
}
