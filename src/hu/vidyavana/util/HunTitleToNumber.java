package hu.vidyavana.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HunTitleToNumber
{
	public static final Pattern NUM = Pattern.compile("\\d+");
	public static final Pattern WHITE = Pattern.compile("\\s");
	public static final Pattern FIRST = Pattern.compile("\\belső\\b");
	public static final Object[] TENS = {
		"tize", 10, "husz", 20, "harminc", 30, "negyven", 40, "ötven", 50,
		"hatvan", 60, "hetven", 70, "nyolcvan", 80, "kilencven", 90};
	public static final Object[] ONES = {
		"viii.", 8, "vii.", 7, "vi.", 6, "iii.", 3, "ii.", 2, "i.", 1, "iv.", 4, "v.", 5, "ix.", 9,
		"enegyedik", 1, "anegyedik", 1, "onegyedik", 1, "negyedik", 4, "egyedik", 1, 
		"első", 1, "második", 2, "kettedik", 2, "harmadik", 3, "ötödik", 5,
		"hatodik", 6, "hetedik", 7, "nyolcadik", 8, "kilencedik", 9};

	
	private int fallbackOrdinal;

	
	public String convert(String title)
	{
		title = title.trim().toLowerCase();
		int ix = title.indexOf('ǀ');
		if(ix > -1)
			title = title.substring(0, ix);
		Matcher m = NUM.matcher(title);
		if(m.find() && m.start(0) == 0)
		{
			if(m.group().length() == 1 && title.indexOf("tárgy") > -1 && title.indexOf("mutató") > -1)
				return "tm" + m.group();
			return m.group();
		}
		int n = 0;
		// not expecting chapter titles to ever go above 199
		if(title.indexOf("száz") > -1)
			n += 100;
		for(int i = 0; i < TENS.length; i += 2)
			if(title.indexOf((String) TENS[i]) > -1)
			{
				n += (Integer) TENS[i+1];
				break;
			}
		for(int i = 0; i < ONES.length; i += 2)
			if(title.indexOf((String) ONES[i]) > -1)
			{
				n += (Integer) ONES[i+1];
				break;
			}
		if(n == 1 && title.indexOf("első") > -1 && !FIRST.matcher(title).find())
			n = 0;
		if(n == 0)
		{
			String[] split = WHITE.split(title);
			for(int i = 0; i < split.length; ++i)
				if(split[i].length() >= 3)
					return split[i].substring(0, 3);
			if(split.length > 0)
			{
				String ret = split[split.length-1];
				if(ret.length() > 0)
					return ret;
			}
			return "[" + (++fallbackOrdinal) + "]";
		}
		return "" + n;
	}
}
