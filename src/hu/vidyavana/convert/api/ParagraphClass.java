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
	Uvaca(7, false, true),
	Vers(8, false, true),
	KozepenVers(131, false, true),
	Proza(9, true),
	Szavak(10, true),
	Forditas(11, true),
	TorzsKezdet(12, true),
	BalraKezdet(50),
	TorzsKoveto(13, true),
	TorzsUvaca(14, false, true),
	TorzsVers(15, false, true),
	TorzsKozepenVers(132, false, true),
	Hivatkozas(16, false, true),
	NemDoltVers(130, false, true),
	Balra(17),
	Kozepen(18),
	Jobbra(19),
	MegjegyzesKezdet(20),
	MegjegyzesKoveto(21, true),
	MegjegyzesKozepen(22),
	MegjegyzesJobbra(23),
	Index(24),
	in0(25),
	in1(26),
	in2(27),
	in3(28),
	in4(29),
	
	// Non-BBT extras
	FejezetszamNagy(100),
	Szakaszcim3(101),
	Alcim3(102),
	TorzsKezdet0Bek(103),
	TorzsKezdetDolt(104),
	TorzsKovetoDolt(105),
	TorzsVersszam(106, false, true),
	KozepenDolt(107),
	KozepenKoveto(108),
	KozepenKovetoDolt(109),
	BalraKoveto(110),
	BalraCim(111),
	BalraCimBek(112),
	BalraKisCimBek(128),
	BalraBek2(113),
	Csillagok(114),
	Ures1(115),
	Ures2(116),
	Ures3(117),
	UjOldal(118),
	Labjegyzet(119),
	MegjegyzesKezdetDolt(121),
	MegjegyzesKovetoDolt(122, true),
	MegjegyzesJobbraDolt(123),
	BehuzottFuggo(124),
	LabjegyzetKoveto(125),
	LabjegyzetVersKoveto(126),
	SzakaszcimBalra(127),
	UresFel(129);

	
	
	private static Map<Integer, ParagraphClass> reverseMap = new HashMap<>();
	static
	{
		for(ParagraphClass pc : values())
			if(reverseMap.put(pc.code, pc) != null)
				throw new RuntimeException("Duplikált kód: " + pc.code);
	}
	
	public final int code;
	public final boolean defaultIndent;
	public final boolean verse;

	
	ParagraphClass(int code)
	{
		this.code = code;
		defaultIndent = false;
		this.verse = false;
	}

	
	ParagraphClass(int code, boolean defaultIndent)
	{
		this.code = code;
		this.defaultIndent = defaultIndent;
		this.verse = false;
	}


	ParagraphClass(int code, boolean defaultIndent, boolean verse)
	{
		this.code = code;
		this.defaultIndent = defaultIndent;
		this.verse = verse;
	}

	
	public static ParagraphClass byCode(int code)
	{
		return reverseMap.get(code);
	}
}
