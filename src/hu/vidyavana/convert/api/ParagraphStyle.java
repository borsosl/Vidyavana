package hu.vidyavana.convert.api;

import java.util.ArrayList;
import java.util.List;

public class ParagraphStyle
{
	public String basedOn;
	
	// see enum
	public Align align;
	
	// font name
	public String font;
	
	// relative font size compared to user font setting. Percentage, 100=base font size
	public Integer size;
	
	// indents and pushes
	// relative measurement compared to user font setting. 100=1 line.
	public Integer left;
	public Integer right;
	public Integer before;
	public Integer after;
	public Integer first;
	public Integer hanging;
	
	// tabs in percentage of the screen width
	public List<Integer> tabs;
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		if(align != null)
			sb.append("_i").append(align.abbrev);
		if(size != null)
			sb.append("_s").append(size);
		if(left != null)
			sb.append("_l").append(left);
		if(right != null)
			sb.append("_r").append(right);
		if(before != null)
			sb.append("_b").append(before);
		if(after != null)
			sb.append("_a").append(after);
		if(first != null)
			sb.append("_f").append(first);
		if(hanging != null)
			sb.append("_h").append(hanging);
		if(tabs != null)
			for(Integer i : tabs)
				sb.append("_t").append(i);
		return sb.toString();
	}
	
	
	public static ParagraphStyle fromString(String style)
	{
		ParagraphStyle s = new ParagraphStyle();
		// TODO parse
		return s ;
	}


	public static ParagraphStyle clone(ParagraphStyle base)
	{
		ParagraphStyle res = new ParagraphStyle();
		if(base != null)
		{
			res.align = base.align;
			res.font = base.font;
			res.size = base.size;
			res.left = base.left;
			res.right = base.right;
			res.before = base.before;
			res.after = base.after;
			res.first = base.first;
			res.hanging = base.hanging;
			if(base.tabs == null)
				res.tabs = new ArrayList<Integer>();
			else
				res.tabs = new ArrayList<Integer>(base.tabs);
		}
		return res;
	}
}
