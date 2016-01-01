package hu.vidyavana.db.model;

import java.util.*;

public class BookAccess extends HashSet<Integer>
{
	private int hashCode;
	public boolean fullAccess;
	
	
	@Override
	public boolean add(Integer e)
	{
		hashCode = 0;
		return super.add(e);
	}
	
	
	@Override
	public boolean remove(Object o)
	{
		hashCode = 0;
		return super.remove(o);
	}
	
	
	@Override
	public int hashCode()
	{
		if(hashCode != 0)
			return hashCode;
		List<Integer> ids = new ArrayList<>(this);
		Collections.sort(ids);
		int c = 0;
		for(Integer id : ids)
			c = c*17 + id;
		return c;
	}


	public static BookAccess parseAccessStr(String accessStr, BookPackage pkg)
	{
		BookAccess res = new BookAccess();
		Map<String, Integer> abbrevMap = TocTree.inst.abbrevToPlainBookId;
		String[] parts = accessStr.split("\\|");
		int numbers = 0;
		for(String part : parts)
		{
			if("1".equals(part))
			{
				res.addAll(BookPackage.Sraddha.plainBookIdSet);
				++numbers;
			}
			else if("2".equals(part))
			{
				res.addAll(BookPackage.SadhuSanga.plainBookIdSet);
				++numbers;
			}
			else if("3".equals(part))
			{
				res.addAll(BookPackage.BhajanaKriya.plainBookIdSet);
				++numbers;
			}
			else if("4".equals(part))
			{
				res.addAll(BookPackage.Ruci.plainBookIdSet);
				++numbers;
			}
			else if(part.startsWith("-"))
			{
				Integer id = abbrevMap.get(part.substring(1));
				if(id != null)
				{
					res.remove(id);
					--numbers;
				}
			}
			else
			{
				Integer id = abbrevMap.get(part);
				if(id != null)
				{
					res.add(id);
					if(pkg != null)
						pkg.abbrevList.add(part);
				}
			}
		}
		res.fullAccess = numbers == 4;
		return res;
	}
}
