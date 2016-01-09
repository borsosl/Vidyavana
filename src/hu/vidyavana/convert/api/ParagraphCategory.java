package hu.vidyavana.convert.api;

import static hu.vidyavana.convert.api.ParagraphClass.*;
import java.util.HashMap;
import java.util.Map;

public enum ParagraphCategory
{
	Cim(Konyvcim, Fejezetcim),
	Alcim(Fejezetszam, Szakaszcim, ParagraphClass.Alcim, Versszam, FejezetszamNagy,
		Szakaszcim3, Alcim3, BalraCim, BalraCimBek, BalraKisCimBek, SzakaszcimBalra),
	SzakaszVers(Uvaca, Vers, Proza),
	Szavak(ParagraphClass.Szavak),
	Forditas(ParagraphClass.Forditas),
	Magyarazat,
	MagyarazatVers(TorzsUvaca, TorzsVers, Hivatkozas, TorzsVersszam),
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
	
	
	private ParagraphCategory(ParagraphClass... classes)
	{
		this.classes = classes;
	}
}
