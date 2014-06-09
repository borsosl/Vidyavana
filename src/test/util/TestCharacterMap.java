package test.util;

import hu.vidyavana.convert.indd.CharacterMapManager;

public class TestCharacterMap
{
	public static void main(String[] args) throws Exception
	{
		CharacterMapManager cmm = new CharacterMapManager();
		cmm.init();
		cmm.selectFont("Times CE Sanskrit");
		cmm.map().put(999, 'b');
		cmm.close();
	}
}
