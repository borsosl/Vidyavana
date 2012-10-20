package hu.vidyavana.convert.epub;

import hu.vidyavana.convert.api.FileListProcessor;
import java.io.File;

public class XmlToEpubXhtml
{
	/**
	 * @param args Egyetlen argumentuma a fájl-lista, az indítókönyvtárhoz relatívan.
	 * 		Ennek a fájlnak az első sora a forrásfájlok gyökérkönyvtára,
	 * 		második sora a célfájlok (xml) gyökérkönyvtára,
	 * 		az összes többi sor a forrásfájlok relatív útjai, kezdeti perjel nélkül!
	 * 		A könyvtáraknál az elválasztó karakter lehet / vagy \. A végén opcionális. 
	 * 		Az üres és # kezdetű sorok figyelmen kívül hagyódnak.
	 */
	public static void main(String[] args) throws Exception
	{
		if(args.length < 1)
		{
			System.out.println("Kerlek add meg a fajl nevet, ami a fajlok listajat tartalmazza.");
			return;
		}
		new FileListProcessor().process(new File(args[0]), new XmlToEpubXhtmlProcessor());
	}
}
