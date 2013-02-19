package hu.vidyavana.convert.api;


public class DiacriticToLatinPairs
{
	static int[] pair = {
		'ā', 'a',
		'ī', 'i',
		'ū', 'u',
		'ḍ', 'd',
		'ḥ', 'h',
		'ḷ', 'l',
		'ḹ', 'l',
		'ṁ', 'm',
		'ṅ', 'n',
		'ṇ', 'n',
		'ñ', 'n',
		'ṛ', 'r',
		'ṝ', 'r',
		'ṣ', 's',
		'ś', 's',
		'ṭ', 't',

		'Ā', 'A',
		'Ī', 'I',
		'Ū', 'U',
		'Ḍ', 'D',
		'Ḥ', 'H',
		'Ḷ', 'L',
		'Ḹ', 'L',
		'Ṁ', 'M',
		'Ṅ', 'N',
		'Ṇ', 'N',
		'Ñ', 'N',
		'Ṛ', 'R',
		'Ṝ', 'R',
		'Ṣ', 'S',
		'Ś', 'S',
		'Ṭ', 'T'
	};

	static int[] table = new int[10000];
	
	static
	{
		for(int i=0; i<pair.length; i+=2)
			table[pair[i]] = pair[i+1];
	}

	
	public static int convert(int c)
	{
		return table[c];
	}
}
