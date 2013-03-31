package test.util;

import hu.vidyavana.db.model.*;
import hu.vidyavana.util.Encrypt;
import java.util.*;

public class DecryptTest
{
	public static void run()
	{
		final Encrypt enc = Encrypt.getInstance();
		enc.init();
		final List<String> list = new ArrayList<String>(); 
		long t1 = System.currentTimeMillis();
		for(Para p : Para.pkIdx().entities(new BookOrdinalKey(1, 1), true, new BookOrdinalKey(1, 1000), true))
			list.add(enc.decrypt(p.text));

		System.out.println(list.get(100));
		System.out.println(System.currentTimeMillis() - t1);
	}
}
