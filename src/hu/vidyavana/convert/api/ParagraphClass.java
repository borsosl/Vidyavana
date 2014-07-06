package hu.vidyavana.convert.api;

import java.util.HashMap;
import java.util.Map;

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
	Balra(17),
	Kozepen(18),
	Jobbra(19),
	MegjegyzesKezdet(20),
	MegjegyzesKoveto(21, true),
	MegjegyzesKozepen(22),
	MegjegyzesJobbra(23),
	Index(24),
	
	// Non-BBT extras
	FejezetszamNagy(30),
	Szakaszcim3(31),
	Alcim3(32),
	TorzsKezdet0Bek(33),
	TorzsKezdetDolt(34),
	TorzsKovetoDolt(35),
	TorzsVersszam(36),
	KozepenDolt(37),
	KozepenKoveto(38),
	KozepenKovetoDolt(39),
	BalraKoveto(40),
	BalraCim(41),
	BalraCimBek(42),
	Csillagok(43),
	Ures1(44),
	Ures2(45),
	Ures3(46),
	UjOldal(47),
	Labjegyzet(48),
	MegjegyzesKezdetDolt(49),
	MegjegyzesKovetoDolt(50, true),
	BehuzottFuggo(51);
	
	
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
