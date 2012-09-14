package hu.vidyavana.convert.api;

public enum ParagraphClass
{
	Konyvcim,
	Fejezetszam,
	Fejezetcim,
	Szakaszcim,
	Alcim,
	Versszam,
	Uvaca,
	Vers,
	Proza,
	Szavak,
	Forditas,
	TorzsKezdet(true),
	TorzsKoveto(true),
	TorzsUvaca,
	TorzsVers,
	Hivatkozas,
	Kozepen,
	Jobbra,
	MegjegyzesKezdet,
	MegjegyzesKoveto(true),
	MegjegyzesKozepen,
	MegjegyzesJobbra,
	TargymutatoElem;
	
	
	public final boolean defaultIndent;

	ParagraphClass()
	{
		this.defaultIndent = false;
		
	}

	ParagraphClass(boolean defaultIndent)
	{
		this.defaultIndent = defaultIndent;
		
	}
}
