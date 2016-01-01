package hu.vidyavana.db.model;

import java.util.*;

public enum BookPackage
{
	Sraddha("BhG|NoI|SSR"),
	SadhuSanga("SB|KB|Cc|TLC|NoD|Īśo|PQPA|Jsd|EJ|RV|BBD|OWK|PoP|Top|PLs"),
	BhajanaKriya("BB|MBh|MBhs|RY"),
	Ruci("SBC|VG|NPH|KS|NVM");

	public Set<Integer> plainBookIdSet;
	public List<String> abbrevList = new ArrayList<>();

	
	BookPackage(String accessStr)
	{
		plainBookIdSet = BookAccess.parseAccessStr(accessStr, this);
	}
	
	
	public String serialize()
	{
		StringBuilder sb = new StringBuilder();
		Map<String, Integer> idMap = TocTree.inst.abbrevToPlainBookId;
		for(String abbr : abbrevList)
			sb.append(abbr).append('|').append(idMap.get(abbr)).append('|');
		if(sb.length() > 0)
			sb.setLength(sb.length()-1);
		return sb.toString();
	}
	
	
	public static Map<String, Object> serializeAll()
	{
		Map<String, Object> res = new HashMap<>();
		for(BookPackage pkg : values())
			res.put(pkg.name(), pkg.serialize());
		return res;
	}
}
