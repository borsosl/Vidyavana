package hu.vidyavana.db.model;

import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.search.model.ParaCategory;
import hu.vidyavana.util.Encrypt;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class StoragePara
{
	public ParagraphClass cls;		// 1 byte
	// short len;
	byte[] encText;
	
	// derived
	public ParaCategory displayCategory;
	public int ordinal;
	public String text;
	

	public void write(RandomAccessFile out) throws IOException
	{
		out.writeByte(cls.code);
		out.write(encText);
	}


	public void read(RandomAccessFile in, int len, boolean encrypted) throws IOException
	{
		int code = in.readUnsignedByte();
		cls = ParagraphClass.byCode(code);
		--len;
		ByteBuffer bb = ByteBuffer.allocate(len);
		in.getChannel().read(bb);
		encText = bb.array();
		decode(encrypted);
		encText = null;
	}

	
	public void encode(boolean encrypt)
	{
		if(encrypt)
			encText = Encrypt.getInstance().encrypt(text);
		else
			encText = text.getBytes(Charset.forName("UTF-8"));
	}

	
	public void decode(boolean encrypted)
	{
		if(encrypted)
			text = Encrypt.getInstance().decrypt(encText);
		else
			try
			{
				text = new String(encText, "UTF-8");
			}
			catch(UnsupportedEncodingException ex)
			{
				throw new RuntimeException("Decoding para", ex);
			}
	}

}
