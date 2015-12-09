package hu.vidyavana.db.model;

import java.io.IOException;
import java.io.RandomAccessFile;

public class StorageTocItem
{
	public byte level;
	public String title;
	public String abbrev;
	public short paraOrdinal;
	
	// derived
	public int startPara;
	public int endPara;			// TODO used for searching checked branches only
	

	public void write(RandomAccessFile out) throws IOException
	{
		out.writeByte(level);
		out.writeUTF(title);
		out.writeUTF(abbrev);
		out.writeShort(paraOrdinal);
	}


	public static StorageTocItem read(RandomAccessFile in) throws IOException
	{
		StorageTocItem ci = new StorageTocItem();
		ci.level = in.readByte();
		ci.title = in.readUTF();
		ci.abbrev = in.readUTF();
		ci.paraOrdinal = in.readShort();
		return ci;
	}


}
