package hu.vidyavana.convert.indd;

import hu.vidyavana.convert.api.DirFilesProcessor;

public class ConvertInddFiles
{
	public static void main(String[] args) throws Exception
	{
		new DirFilesProcessor().process(".", "xml", new InddFileProcessor());
	}
}
