package hu.vidyavana.convert.api;

public class ParagraphStyle
{
	// see enum
	public Align align;
	
	// relative font size compared to user font setting. Percentage, 100=base font size
	public int font = 100;
	
	// relative measurement compared to user font setting. 100=1 line.
	public int indent;
	public int before;
	public int after;
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(align.toString());
		if(font != 100)
			sb.append("_f").append(font);
		if(indent != 0)
			sb.append("_i").append(indent);
		if(before != 0)
			sb.append("_b").append(before);
		if(after != 0)
			sb.append("_a").append(after);
		return sb.toString();
	}
}
