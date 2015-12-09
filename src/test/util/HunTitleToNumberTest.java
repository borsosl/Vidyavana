package test.util;

import static org.junit.Assert.*;
import org.junit.Test;
import hu.vidyavana.util.HunTitleToNumber;

public class HunTitleToNumberTest
{
	@Test
	public void simple()
	{
		HunTitleToNumber ht = new HunTitleToNumber();
		assertEquals("1", ht.convert("1. VERS"));
		assertEquals("11", ht.convert("11-13. VERS"));
		assertEquals("7", ht.convert("VII. FEJEZET"));
		assertEquals("1", ht.convert("ELSŐ"));
		assertEquals("2", ht.convert("MÁSODIK"));
		assertEquals("4", ht.convert("NEGYEDIK"));
		assertEquals("20", ht.convert("HUSZADIK"));
		assertEquals("22", ht.convert("HUSZONKETTEDIK"));
		assertEquals("57", ht.convert("ÖTVENHETEDIK"));
		assertEquals("11", ht.convert("TIZENEGYEDIK"));
		assertEquals("14", ht.convert("TIZENNEGYEDIK"));
		assertEquals("21", ht.convert("HUSZONEGYEDIK"));
		assertEquals("24", ht.convert("HUSZONNEGYEDIK"));
		assertEquals("61", ht.convert("HATVANEGYEDIK"));
		assertEquals("64", ht.convert("HATVANNEGYEDIK"));
		assertEquals("csa", ht.convert("Csak szöveg"));
		assertEquals("más", ht.convert("Másik szöveg"));
		assertEquals("szö", ht.convert("A szöveg"));
		assertEquals("go", ht.convert("no go"));
		assertEquals("tm1", ht.convert("1. tárgymutató"));
		assertEquals("ny", ht.convert("Ny"));
		assertEquals("ist", ht.convert("Istenség Legfelsőbb"));
		assertEquals("2", ht.convert("második fejǀelső szint"));
	}
}
