package hu.vidyavana.search.util;

import hu.vidyavana.convert.api.DiacriticLowercase;
import hu.vidyavana.convert.api.DiacriticToLatin;
import hu.vidyavana.util.XmlToken;
import hu.vidyavana.util.XmlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class HitListEntry {

    private static Pattern WORD_BOUNDARY = Pattern.compile("[^a-z0-9"+DiacriticLowercase.LOWER+"]+");

    private int targetLength;

    public HitListEntry(int targetLength) {
        this.targetLength = targetLength;
    }

    public String create(String para, String query, boolean allItalic, boolean allBold) {
        List<XmlToken> tokens = XmlUtil.tokenize(para);
        adjustTokenLengths(tokens);
        List<Sought> soughts = createSoughtPatterns(query);
        List<TokenRange> ranges = findMatchingTokens(tokens, soughts);
        addHalfOfTargetLengthAroundFoundTokens(tokens, ranges);
        ranges = unifyOverlappingRanges(ranges);
        extendRangesToTargetLength(tokens, ranges);
        return printRanges(ranges, tokens, allItalic, allBold);
    }

    private void adjustTokenLengths(List<XmlToken> tokens) {
        int ofs = 0;
        for(XmlToken token : tokens) {
            token.printPos += ofs;
            if(token.isTag && token.text.startsWith("<br")) {
                token.printLength = 3;
                ofs +=3;
            }
        }
    }

    private static List<Sought> createSoughtPatterns(String query) {
        List<Sought> soughts = new ArrayList<>();
        String[] tokens = query.split("\\s+");
        for (String token : tokens) {
            if(token.isEmpty())
                continue;
            Sought sought = new Sought();
            sought.word = token;
            String wcReplace = token.replace("*", ".*").replace("?", ".?");
            if(!wcReplace.equals(token)) {
                sought.rex = Pattern.compile(wcReplace);
            }
            sought.hasDiacritic = !DiacriticToLatin.convert(token).equals(token);
            soughts.add(sought);
        }
        return soughts;
    }

    private static List<TokenRange> findMatchingTokens(List<XmlToken> tokens, List<Sought> soughts) {
        List<TokenRange> ranges = new ArrayList<>();
        for(int i=0; i<tokens.size(); ++i) {
            XmlToken token = tokens.get(i);
            if(!token.isText())
                continue;
            String[] words = WORD_BOUNDARY.split(DiacriticLowercase.word(token.text));
            for (String word : words) {
                if(word.isEmpty())
                    continue;
                if(tokenMatches(soughts, word)) {
                    TokenRange range = new TokenRange();
                    range.startToken = range.endToken = i;
                    range.matchingTokens.add(i);
                    ranges.add(range);
                }
            }
        }
        return ranges;
    }

    private static boolean tokenMatches(List<Sought> soughts, String word) {
        for (Sought sought : soughts) {
            boolean match;
            if(sought.rex != null) {
                String wd = word;
                if(!sought.hasDiacritic)
                    wd = DiacriticToLatin.convert(wd);
                match = sought.rex.matcher(wd).matches();
            } else if(sought.hasDiacritic) {
                match = word.equals(sought.word);
            } else {
                match = DiacriticToLatin.convert(word).equals(sought.word);
            }
            if(match)
                return true;
        }
        return false;
    }

    private void addHalfOfTargetLengthAroundFoundTokens(List<XmlToken> tokens, List<TokenRange> ranges) {
        for (TokenRange range : ranges) {
            int aroundLen = (targetLength - tokens.get(range.startToken).printLength) / 2;
            int remainLength = aroundLen;
            for(int i = range.startToken-1; i > 0; --i) {
                remainLength -= tokens.get(i).fullLength();
                if(remainLength < 0)
                    break;
                range.startToken = i;
            }
            remainLength = aroundLen;
            for(int i=range.endToken+1; i<tokens.size(); ++i) {
                remainLength -= tokens.get(i).fullLength();
                if(remainLength < 0)
                    break;
                range.endToken = i;
            }
            XmlToken startToken = tokens.get(range.startToken);
            XmlToken endToken = tokens.get(range.endToken);
            int printLength = endToken.printPos + endToken.fullLength() - startToken.printPos;
            range.remainLength = targetLength - printLength;
        }
    }

    private List<TokenRange> unifyOverlappingRanges(List<TokenRange> ranges) {
        if(ranges.size() < 2)
            return ranges;
        List<TokenRange> unifiedRanges = new ArrayList<>();
        for(int i=0; i<ranges.size()-1; ++i) {
            TokenRange current = ranges.get(i);
            TokenRange next = ranges.get(i + 1);
            if(current.endToken >= next.startToken) {
                next.startToken = current.startToken;
                next.matchingTokens.addAll(current.matchingTokens);
                next.isUnified = true;
            } else {
                unifiedRanges.add(current);
            }
        }
        unifiedRanges.add(ranges.get(ranges.size()-1));
        return unifiedRanges;
    }

    private void extendRangesToTargetLength(List<XmlToken> tokens, List<TokenRange> ranges) {
        int lastTokenIx = tokens.size() - 1;
        for (TokenRange range : ranges) {
            if(range.isUnified) {
                int firstFoundToken = range.matchingTokens.first();
                int lastFoundToken = range.matchingTokens.last();
                range.startToken = Math.max(firstFoundToken-3, 0);
                range.endToken = Math.min(lastFoundToken+3, lastTokenIx);
            }

            XmlToken startToken = tokens.get(range.startToken);
            XmlToken endToken = tokens.get(range.endToken);
            if(range.isUnified || range.remainLength > 5) {
                boolean leftEnd = range.startToken == 0;
                boolean rightEnd = range.endToken == lastTokenIx;
                for(int i=0; !leftEnd || !rightEnd; ++i) {
                    if((i % 2 == 0) ? rightEnd : leftEnd)
                        continue;
                    int printLength;
                    if(i % 2 == 0) {
                        XmlToken newEndToken = tokens.get(range.endToken + 1);
                        printLength = newEndToken.printPos + newEndToken.fullLength() - startToken.printPos;
                        if(printLength > targetLength)
                            rightEnd = true;
                        else {
                            ++range.endToken;
                            if(range.endToken == lastTokenIx)
                                rightEnd = true;
                            else
                                endToken = newEndToken;
                        }
                    } else {
                        XmlToken newStartToken = tokens.get(range.startToken - 1);
                        printLength = endToken.printPos + endToken.fullLength() - newStartToken.printPos;
                        if(printLength > targetLength)
                            leftEnd = true;
                        else {
                            --range.startToken;
                            if(range.startToken == 0)
                                leftEnd = true;
                            else
                                startToken = newStartToken;
                        }
                    }
                }
            }

            if(startToken.printPos < 5)
                range.startToken = 0;
            else if(startToken.isTag && startToken.printLength > 0)
                ++range.startToken;
            if(range.endToken < lastTokenIx) {
                XmlToken lastToken = tokens.get(lastTokenIx);
                int paraLen = lastToken.printPos + lastToken.fullLength();
                if(paraLen - endToken.printPos - endToken.fullLength() < 5)
                    range.endToken = lastTokenIx;
            }
        }
    }

    private String printRanges(List<TokenRange> ranges, List<XmlToken> tokens, boolean allItalic, boolean allBold) {
        StringBuilder sb = new StringBuilder(ranges.size()*targetLength*2);
        int rangeIx = 0;
        TokenRange currentRange = ranges.get(rangeIx);
        boolean inRange = false;
        int italic = allItalic ? 1 : 0;
        int bold = allBold ? 1 : 0;
        for(int i = 0; i < tokens.size(); ++i) {
            XmlToken token = tokens.get(i);
            if(i > currentRange.endToken) {
                inRange = false;
                if(italic > 0)
                    sb.append("</i>");
                if(bold > 0)
                    sb.append("</b>");
                if(i < tokens.size())
                    sb.append(" …");
                ++rangeIx;
                if(rangeIx >= ranges.size())
                    break;
                currentRange = ranges.get(rangeIx);
                sb.append("<br/>\n");
            }
            if(i == currentRange.startToken) {
                inRange = true;
                if(i > 0)
                    sb.append("… ");
                if(bold > 0)
                    sb.append("<b>");
                if(italic > 0)
                    sb.append("<i>");
            }
            String t = token.text;
            boolean skipTag = false;
            if(token.isTag) {
                if("<b>".equals(t)) {
                    ++bold;
                    skipTag = bold > 1;
                }
                else if("</b>".equals(t)) {
                    --bold;
                    skipTag = bold != 0;
                }
                else if("<i>".equals(t)) {
                    ++italic;
                    skipTag = italic > 1;
                }
                else if("</i>".equals(t)) {
                    --italic;
                    skipTag = italic != 0;
                }
            }
            if(inRange) {
                if(token.isTag && t.startsWith("<br")) {
                    sb.append(" / ");
                    continue;
                }
                if(!skipTag)
                    sb.append(t);
                if(token.spaceFollows)
                    sb.append(" ");
            }
        }
        return sb.toString();
    }

    private static class TokenRange {
        int startToken;
        int endToken;
        TreeSet<Integer> matchingTokens = new TreeSet<>();
        int remainLength;
        boolean isUnified;
    }

    private static class Sought {
        String word;
        Pattern rex;
        boolean hasDiacritic;
    }
}
