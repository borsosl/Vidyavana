package hu.vidyavana.convert.api;

import java.util.HashMap;
import java.util.Map;

public class DiacriticLowercase {
    public static final String LOWER = "áéíóöőúüűāīūḍḥḷḹṁṅṇñṛṝṣśṭ";
    public static final String UPPER = "ÁÉÍÓÖŐÚÜŰĀĪŪḌḤḶḸṀṄṆÑṚṜṢŚṬ";

    public static final String SANS_ALL = "āīūḍḥḷḹṁṅṇñṛṝṣśṭĀĪŪḌḤḶḸṀṄṆÑṚṜṢŚṬ";
    public static final String SANS_LOWER = "āīūḍḥḷḹṁṅṇñṛṝṣśṭ";

    public static Map<Integer, Integer> lowerMap = new HashMap<>(71);
    static
    {
        for(int i = 0; i< LOWER.length(); ++i)
        {
            int low = LOWER.charAt(i);
            int upp = UPPER.charAt(i);
            lowerMap.put(low, low);
            lowerMap.put(upp, low);
        }
    }

    
    public static int chr(int c)
    {
        if(c >= 'a' && c <= 'z' || c >= '0' && c <= '9');
        else if(c >= 'A' && c <= 'Z')
            c += 32;
        else
        {
            Integer ch = lowerMap.get(c);
            if(ch == null)
                if(Character.isAlphabetic(c))
                    c = Character.toLowerCase(c);
                else
                    c = 0;
            else
                c = ch;
        }
        return c;
    }

    public static String word(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for(int i=0; i<s.length(); ++i)
            sb.append((char) chr(s.charAt(i)));
        return sb.toString();
    }

    public static boolean hasSanskritCharacter(String s, boolean isLowercasedWord) {
        String charset = isLowercasedWord ? SANS_LOWER : SANS_ALL;
        for(int i=0; i<s.length(); ++i)
            if(charset.indexOf(s.charAt(i)) > -1)
                return true;
        return false;
    }
}
