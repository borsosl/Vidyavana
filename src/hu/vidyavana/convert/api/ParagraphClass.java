package hu.vidyavana.convert.api;

import java.util.*;

public enum ParagraphClass
{
	Konyvcim(1),
	Fejezetszam(2),
	Fejezetcim(3),
	Szakaszcim(4),
	Alcim(5),
	Versszam(6),
	Uvaca(7),
	Vers(8),
	Proza(9, true),
	Szavak(10, true),
	Forditas(11, true),
	TorzsKezdet(12, true),
	TorzsKoveto(13, true),
	TorzsUvaca(14),
	TorzsVers(15),
	Hivatkozas(16),
	Kozepen(17),
	Jobbra(18),
	MegjegyzesKezdet(19),
	MegjegyzesKoveto(20, true),
	MegjegyzesKozepen(21),
	MegjegyzesJobbra(22),
	Index(23);
	
	
	private static Map<Integer, ParagraphClass> reverseMap = new HashMap<>();
	static
	{
		for(ParagraphClass pc : values())
			reverseMap.put(pc.code, pc);
	}
	
	public final int code;
	public final boolean defaultIndent;

	
	ParagraphClass(int code)
	{
		this.code = code;
		defaultIndent = false;
	}

	
	ParagraphClass(int code, boolean defaultIndent)
	{
		this.code = code;
		this.defaultIndent = defaultIndent;
	}
	
	
	public static ParagraphClass byCode(int code)
	{
		return reverseMap.get(code);
	}
}
