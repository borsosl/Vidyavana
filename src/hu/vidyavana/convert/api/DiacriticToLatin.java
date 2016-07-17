package hu.vidyavana.convert.api;


public class DiacriticToLatin
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

	public static String convert(String word) {
		return convert(new StringBuilder(), word);
	}

	public static String convert(StringBuilder sb, String word) {
		sb.setLength(0);
		int len = word.length();
		for(int j = 0; j < len; ++j)
        {
            char orig = word.charAt(j);
            int latin = convert(orig);
            sb.append(latin == 0 ? (char) orig : (char) latin);
        }
		return sb.toString();
	}
}
