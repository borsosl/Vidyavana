package hu.vidyavana.convert.api;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WriterInfo
{
	public static enum SpecialFile
	{
		BG_DS("hubg00ds.h05"),
		BG_PF("hubg00pf.h12");
		
		public static Map<String, SpecialFile> fnameMap = new HashMap<>();
		static
		{
			for(SpecialFile sp : SpecialFile.values())
				fnameMap.put(sp.fname, sp);
		}
		
		private String fname;

		SpecialFile(String fname)
		{
			this.fname = fname;
			
		}
	}

	public File srcFile;
	public File xmlFile;
	public List<String> fileNames;
	public boolean forEbook;
	public SpecialFile specialFile;

	public Writer out;
	public int indentLevel;
	public String line;
	public int pos;
	
	public Writer toc;
	public String tocDivision;
	public int tocOrdinal;
	public int paraOrdinal;
	public int tocDivisionParaOrdinal;
	
	public TreeMap<String, Object> diacritics/* = new TreeMap<>()*/;		// uncomment for collect diacritics function
	public ProofreadWords proofreadWords = new ProofreadWords();
}
