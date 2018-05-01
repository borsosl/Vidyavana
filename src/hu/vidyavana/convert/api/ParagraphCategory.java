package hu.vidyavana.convert.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static hu.vidyavana.convert.api.ParagraphClass.*;

public enum ParagraphCategory
{
	Cim(Konyvcim, Fejezetcim),
	Alcim(Fejezetszam, Szakaszcim, ParagraphClass.Alcim, Versszam, FejezetszamNagy,
		Szakaszcim3, Alcim3, BalraCim, BalraCimBek, BalraKisCimBek, SzakaszcimBalra),
	SzakaszVers(Uvaca, Vers, Proza),
	Szavak(ParagraphClass.Szavak),
	Forditas(ParagraphClass.Forditas),
	Magyarazat,
	MagyarazatVers(TorzsUvaca, TorzsVers, Hivatkozas, TorzsVersszam, NemDoltVers),
	Index(ParagraphClass.Index, in0, in1, in2, in3, in4);

	public static Map<ParagraphClass, ParagraphCategory> mapFromClass;
	private ParagraphClass[] classes;
	
	static
	{
		mapFromClass = new HashMap<>();
		for(ParagraphCategory cat : values())
			for(ParagraphClass cls : cat.classes)
				mapFromClass.put(cls, cat);
		for(ParagraphClass cls : ParagraphClass.values())
			if(!mapFromClass.containsKey(cls))
				mapFromClass.put(cls, Magyarazat);
	}
	
	
	ParagraphCategory(ParagraphClass... classes)
	{
		this.classes = classes;
	}

	public static EnumSet<ParagraphCategory> enumSetOf(String types) {
		EnumSet<ParagraphCategory> set = EnumSet.noneOf(ParagraphCategory.class);
		types.chars().forEach(c -> set.add(ParagraphCategory.values()[c-48]));
		return set;
	}
}
