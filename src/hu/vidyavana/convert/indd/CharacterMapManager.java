package hu.vidyavana.convert.indd;

import hu.vidyavana.convert.api.DirFilesProcessor;
import hu.vidyavana.convert.api.FileProcessor;
import hu.vidyavana.convert.api.WriterInfo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CharacterMapManager
{
	public static final String DEFAULT_STYLENAME = "defaultStylename";
	public static final String DEFAULT_TEXT = "defaultText";
	static String cmProp = System.getProperty("cmapDir", "c:\\prg\\vidyavana\\meta\\cmap");
	static String DEFAULT_TEXT_MAP_FILE = "eng1.cmap.txt";
	static String DEFAULT_STYLENAME_MAP_FILE = "hun1.cmap.txt";
	static File cmapDir = new File(cmProp);

	Map<String, CharacterMap> fontToCmap;
	List<CharacterMap> cmaps;
	public CharacterMap defaultTextMap;
	public CharacterMap defaultStylenameMap;
	CharacterMap currentMap;
	public String selectedFont;

	public void init() throws Exception
	{
		fontToCmap = new HashMap<>();
		cmaps = new ArrayList<>();
		new DirFilesProcessor().process(cmapDir.getAbsolutePath(), null, new FileProcessor()
		{
			@Override
			public void process(File srcFile, String fileName) throws Exception
			{
				CharacterMap cmap = new CharacterMap(srcFile);
				if(cmap.init())
				{
					cmaps.add(cmap);
					for(String font : cmap.fonts)
						fontToCmap.put(font, cmap);
					if(fileName.equals(DEFAULT_TEXT_MAP_FILE))
						defaultTextMap = cmap;
					if(fileName.equals(DEFAULT_STYLENAME_MAP_FILE))
						defaultStylenameMap = cmap;
				}
			}
			
			
			@Override
			public void init(File srcDir, File destDir) throws Exception
			{
			}
			
			
			@Override
			public void finish() throws Exception
			{
			}
		});
	}
	
	
	public void selectFont(String font)
	{
		if(DEFAULT_TEXT.equals(font))
			currentMap = defaultTextMap;
		else if(DEFAULT_STYLENAME.equals(font))
			currentMap = defaultStylenameMap;
		else
			currentMap = fontToCmap.get(font);
		if(currentMap == null)
			throw new RuntimeException("No character maps define font "+font);
		selectedFont = font;
	}
	
	
	public CharacterMap map()
	{
		return currentMap;
	}
	
	
	public void close() throws IOException
	{
		for(CharacterMap map : cmaps)
			map.close();
	}
}
