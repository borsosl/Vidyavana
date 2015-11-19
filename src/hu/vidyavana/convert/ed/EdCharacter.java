package hu.vidyavana.convert.ed;

import java.util.HashMap;
import java.util.Map;


public class EdCharacter
{
	// ed--unicode pairs
	static int[] pair = {
		0xa7, 'á',
		0xa6, 'é',
		0xa8, 'í',
		0xa9, 'ó',
		0xa2, 'ö',
		0xc6, 'ő',
		0xcd, 'ú',
		0xa4, 'ü',
		0xc9, 'ű',
		
		0xb7, 'Á',
		0xb6, 'É',
		0xb8, 'Í',
		0xb9, 'Ó',
		0xb2, 'Ö',
		0xd6, 'Ő',
		0xdd, 'Ú',
		0xb4, 'Ü',
		0xd9, 'Ű',
		
		0x83, 'ā',
		0x8c, 'ī',
		0x81, 'ū',
		0x88, 'ḍ',
		0x8b, 'ḥ',
		0x8e, 'ḷ',
		0x8f, 'ḹ',
		0x84, 'ṁ',
		0x85, 'ṅ',
		0x86, 'ṇ',
		0x8a, 'ñ',
		0x82, 'ṛ',
		0x8d, 'ṝ',
		0x89, 'ṣ',
		0x80, 'ś',
		0x87, 'ṭ',
		
		0x93, 'Ā',
		0x9c, 'Ī',
		0x91, 'Ū',
		0x98, 'Ḍ',
		0x9b, 'Ḥ',
		0x9e, 'Ḷ',
		0x9f, 'Ḹ',
		0x94, 'Ṁ',
		0x95, 'Ṅ',
		0x96, 'Ṇ',
		0x9a, 'Ñ',
		0x92, 'Ṛ',
		// 0x9d, 'Ṝ',
		0x99, 'Ṣ',
		0x90, 'Ś',
		0x97, 'Ṭ',
		
		0xc8, '×',
		0xe5, '×',

		0xc0, '„',
		0xc4, '–',
		0xc5, '—',
		0xdf, '»',
		0xcf, '«',
		
		0xc3, 'ā',
		0x9d, 'ṁ',
		0xe1, 'ḹ',
		0xef, ' '
		
		// „“”‚‘’–—
	};
	
	
	public static enum UtfMarkers
	{
		Nbsp(-1),
		Ellipsis(-2);
		
		public int code;

		private UtfMarkers(int code)
		{
			this.code = code;
		}
	}

	static int[] table = new int[128];
	static Map<Integer, Integer> revTable;
	
	static
	{
		for(int i=0; i<pair.length; i+=2)
			table[pair[i]-128] = pair[i+1];
	}

	
	public static int convert(int c)
	{
		return table[c-128];
	}

	
	public static void initReverseConversion()
	{
		revTable = new HashMap<Integer, Integer>();
		for(int i=0; i<pair.length; i+=2)
		{
			if(pair[i] == 0xc3)
				break;
			revTable.put(pair[i+1], pair[i]);
		}
	}

	
	public static int convertToEd(int c)
	{
		Integer k = revTable.get(c);
		if(k == null)
		{
			if(c == '”')
				k = (int) '"';
			else if(c == '’')
				k = (int) '\'';
			else if(c == 160)
				k = UtfMarkers.Nbsp.code;
			else
				throw new IllegalStateException("Unsupported character code: "+c);
		}
		return k;
	}
}
