package hu.vidyavana.convert.api;

public enum Align
{
	Left,
	Center,
	Right,
	Justify,
	Verse;
	

	public final char abbrev;

	
	Align()
	{
		this.abbrev = Character.toLowerCase(name().charAt(0));
	}
	
	
	public static Align get(char abbrev)
	{
		for(Align a : values())
			if(abbrev == Character.toLowerCase(a.name().charAt(0)))
				return a;
		return null;
	}
}
