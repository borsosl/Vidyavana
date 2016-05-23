package hu.vidyavana.convert.api;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DiacriticWordComparator implements Comparator<String> {

    static int[] orderPair = {
            'á', 'a'*10,
            'é', 'e'*10,
            'í', 'i'*10,
            'ó', 'o'*10,
            'ö', 'o'*10+1,
            'ő', 'o'*10+1,
            'ú', 'u'*10,
            'ü', 'u'*10+2,
            'ű', 'u'*10+2,
            'ā', 'a'*10+1,
            'ī', 'i'*10+1,
            'ū', 'u'*10+1,
            'ḍ', 'd'*10+1,
            'ḥ', 'h'*10+1,
            'ḷ', 'l'*10+1,
            'ḹ', 'l'*10+1,
            'ṁ', 'm'*10+1,
            'ṅ', 'n'*10+1,
            'ṇ', 'n'*10+1,
            'ñ', 'n'*10+1,
            'ṛ', 'r'*10+1,
            'ṝ', 'r'*10+1,
            'ṣ', 's'*10+1,
            'ś', 's'*10+1,
            'ṭ', 't'*10+1
    };
    static Map<Integer, Integer> orderMap = new HashMap<>();

    static {
        for(int i=0; i<orderPair.length; i+=2)
            orderMap.put(orderPair[i], orderPair[i+1]);
    }

    @Override
    public int compare(String s1, String s2) {
        int len = Math.min(s1.length(), s2.length());
        for(int i=0; i<len; ++i) {
            int c = chrValue(s1.charAt(i)) - chrValue(s2.charAt(i));
            if(c != 0)
                return c;
        }
        return s1.length() - s2.length();
    }

    private int chrValue(char c) {
        int lc = DiacriticLowercase.chr(c);
        if(lc >= 'a' && lc <= 'z')
            return lc*10;
        Integer order = orderMap.get((int) c);
        if(order == null)
            return 0;
        return order;
    }
}
