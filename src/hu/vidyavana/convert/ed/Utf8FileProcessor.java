package hu.vidyavana.convert.ed;

import static hu.vidyavana.convert.ed.EdCharacter.UtfMarkers.*;
import java.io.*;
import hu.vidyavana.convert.api.FileProcessor;

public class Utf8FileProcessor implements FileProcessor
{
	private File destDir;


	@Override
	public void init(File srcDir, File destDir)
	{
		this.destDir = destDir;
		EdCharacter.initReverseConversion();
	}


	@Override
	public void process(File srcFile, String fileName) throws Exception
	{
		fileName = fileName.substring(0, fileName.indexOf('.'));
		File destFile = new File(destDir.getAbsolutePath() + "/" + fileName + ".001");
		process(srcFile, destFile);
	}


	@Override
	public void finish()
	{
	}


	public void process(File utf, File ed) throws Exception
	{
		try(BufferedReader is = new BufferedReader(new InputStreamReader(
			new FileInputStream(utf), "UTF8"));
			OutputStream os = new BufferedOutputStream(new FileOutputStream(ed)))
		{
			int u;
			int len = 0;
			StringBuffer tag = null;
			boolean newline = false;
			boolean lineBreak = false;
			while((u=is.read())!=-1)
			{
				int e;
				if(u < 128)
					e = u;
				else
					try
					{
						e = EdCharacter.convertToEd(u);
						if(e < 0)
						{
							if(e == Nbsp.code)
								os.write(new byte[]{'<', 'N', '>'});
							++len;
							continue;
						}
					}
					catch(Exception ex)
					{
						os.write(new byte[]{'{', '`', '#'});
						String s = ""+u;
						for(int i=0; i<s.length(); ++i)
							os.write((byte) s.charAt(i));
						os.write((byte) '}');
						len += 4 + s.length();
						continue;
					}
				if(lineBreak && (e == 13 || e == 10))
					continue;
				lineBreak = false;
				switch(e)
				{
					case ' ':
						if(len > 75)
							newline = true;
					case 13:
					case 10:
						len = 0;
						break;
					case '<':
						tag = new StringBuffer();
						break;
					case '>':
						if("R".equals(tag))
							lineBreak = true;
						++len;
						tag = null;
						break;
					default:
						if(tag != null)
							tag.append((char) u);
						else
							++len;
				}
				if(!newline)
					os.write(e);
				if(newline || lineBreak || e==10)
				{
					os.write(13);
					os.write(10);
					len = 0;
					newline = false;
				}
			}
		}
	}
}
