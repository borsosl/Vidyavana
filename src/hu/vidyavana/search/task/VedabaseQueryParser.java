package hu.vidyavana.search.task;

import hu.vidyavana.db.model.BookAccess;
import hu.vidyavana.search.api.*;
import hu.vidyavana.search.model.Search.Order;
import hu.vidyavana.search.model.SearchRange;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.*;

import java.util.ArrayList;
import java.util.List;

public class VedabaseQueryParser
{
	public enum ItemType {
        List,
	    Term,
        Wildcard,
        Phrase,
        Proximity
	}

    public static class ExpressionItem {
        public ItemType type;
		public Occur connect;
		public ExpressionList sub;
		public String text;
		public String[] phrases;
		public int proximity;
    }

	public static class ExpressionList {
		List<String> tokens;
		public List<ExpressionItem> items;
	}


	public static Query parse(String s, BookAccess bookAccess, Order order, List<SearchRange> searchRanges, int paraTypesBits)
	{
		ExpressionList root = initRootExpressionList(s);
		parseTokens(root);

		BooleanQuery.Builder bqb = new BooleanQuery.Builder();
		addToBooleanQuery(bqb, root, order);

		if(bookAccess != null && !bookAccess.fullAccess)
			bqb.add(new FilterByIntegerSetQuery("bookId", bookAccess), Occur.MUST);
		if(searchRanges != null)
			bqb.add(new FilterBySearchRangesQuery("rangeFilterOrdinal", searchRanges), Occur.MUST);
		if(paraTypesBits > 0)
			bqb.add(new FilterByParagraphTypesQuery("paraCategory", paraTypesBits), Occur.MUST);
		return bqb.build();
	}

	private static void addToBooleanQuery(BooleanQuery.Builder bqb, ExpressionList list, Order order) {
		for(ExpressionItem item : list.items) {
			switch(item.type) {
				case List:
					BooleanQuery.Builder subQueryBuilder = new BooleanQuery.Builder();
					addToBooleanQuery(subQueryBuilder, item.sub, order);
					bqb.add(subQueryBuilder.build(), item.connect);
					break;
				case Term:
					bqb.add(new TermQuery(new Term("text", item.text)), item.connect);
					break;
				case Wildcard:
					WildcardQuery wq = new WildcardQuery(new Term("text", item.text));
					if(order == Order.Score)
						wq.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_REWRITE);
					bqb.add(wq, item.connect);
					break;
				case Phrase:
					bqb.add(new PhraseQuery(0, "text", item.phrases), item.connect);
					break;
				case Proximity:
					bqb.add(new PhraseQuery(item.proximity, "text", item.phrases), item.connect);
					break;
			}
		}
	}

	public static ExpressionList initRootExpressionList(String s) {
		ExpressionList root = new ExpressionList();
		root.tokens = QueryAnalyzer.getTokens(s);
		return root;
	}

	public static void parseTokens(ExpressionList list) {
        list.items = new ArrayList<>();
        ExpressionItem nextItem = new ExpressionItem(), prevItem = null;
		for(int i = 0; i < list.tokens.size(); i++) {
			String t = list.tokens.get(i);
			if(t.isEmpty())
				continue;
			if(SeparateQueryOperatorFilter.OPERATOR_CHAR.matcher(t).find()) {
				if("(".equals(t)) {
                    int end = matchingParenthesis(list.tokens, i);
                    nextItem.sub = new ExpressionList();
                    nextItem.sub.tokens = new ArrayList<>(list.tokens.subList(i+1, end));
                    parseTokens(nextItem.sub);
                    if(nextItem.sub.items.size() > 1)
                        nextItem.type = ItemType.List;
                    else if(nextItem.sub.items.size() == 1)
                        nextItem = nextItem.sub.items.get(0);
                    else
                        nextItem.sub = null;
                    i = end;
                } else if("\"".equals(t)) {
				    int end = matchingQuotes(list.tokens, i);
				    if(end == i+2) {
						nextItem.text = list.tokens.get(i+1);
						nextItem.type = termType(nextItem.text);
					} else if(end > i+2) {
                        nextItem.phrases = list.tokens.subList(i+1, end).toArray(new String[end-i-1]);
                        nextItem.type = ItemType.Phrase;
                    }
                    i = end;
                }
				else if("+".equals(t))
				    nextItem.connect = Occur.MUST;
                else if("-".equals(t) || "!".equals(t))
                    nextItem.connect = Occur.MUST_NOT;
                else if("/".equals(t) || "|".equals(t))
                    nextItem.connect = Occur.SHOULD;
                else if("~".equals(t) && prevItem != null && prevItem.type == ItemType.Phrase && list.tokens.size()>i+1) {
                    try {
                        prevItem.proximity = Integer.parseInt(list.tokens.get(i+1));
                        prevItem.type = ItemType.Proximity;
                        i++;
                    } catch (Exception ignored) {
                    }
                }
			} else {
				if("or".equals(t))
				    nextItem.connect = Occur.SHOULD;
				else if("not".equals(t))
				    nextItem.connect = Occur.MUST_NOT;
				else {
                    nextItem.type = termType(t);
                    nextItem.text = t;
                }
			}
			if(nextItem.type != null) {
                if(nextItem.connect == null)
                    nextItem.connect = Occur.MUST;
			    list.items.add(nextItem);
                prevItem = nextItem;
                nextItem = new ExpressionItem();
            }
		}
		if(list.items.size() > 1) {
            ExpressionItem prev = null;
		    for(int ix = 0; ix < list.items.size(); ix++) {
                ExpressionItem it = list.items.get(ix);
                if(it.connect == Occur.SHOULD) {
                    if(ix == 0)
                        it.connect = Occur.MUST;
                    else if(ix == 1) {
                        if(prev.connect == Occur.MUST)
                            prev.connect = Occur.SHOULD;
                    } else if(prev.connect != Occur.SHOULD) {
                        combineBeforeItem(list, ix);
                        list.items.get(0).connect = Occur.SHOULD;
                        ix = 1;
                    }
                } else if(it.connect == Occur.MUST) {
                    if(ix > 1 && prev.connect == Occur.SHOULD) {
                        combineBeforeItem(list, ix);
                        list.items.get(0).connect = Occur.MUST;
                        ix = 1;
                    }
                } else if(it.connect == Occur.MUST_NOT) {
                    if(ix == 0)
                        it.connect = Occur.MUST;
                }
                prev = it;
            }
        } else if(list.items.size() == 1) {
			ExpressionItem item = list.items.get(0);
			if(item.type == ItemType.List) {
				list.items = item.sub.items;
			}
		}
	}

    private static void combineBeforeItem(ExpressionList list, int ix) {
        ExpressionItem combinedItem = new ExpressionItem();
        combinedItem.type = ItemType.List;
	    combinedItem.sub = new ExpressionList();
        combinedItem.sub.items = new ArrayList<>(list.items.subList(0, ix));
        list.items = new ArrayList<>(list.items.subList(ix, list.items.size()));
        list.items.add(0, combinedItem);
    }

    private static ItemType termType(String t) {
        int ixAst = t.indexOf('*');
        int ixQm = t.indexOf('?');
        if((ixAst > 1 && (ixQm == -1 || ixQm > 1)) || ixQm > 1 && ixAst == -1)
            return ItemType.Wildcard;
        return ItemType.Term;
    }

    private static int matchingParenthesis(List<String> tokens, int start) {
		int level = 1;
	    for(int i = start+1; i < tokens.size(); i++) {
			if(")".equals(tokens.get(i))) {
				level--;
				if(level == 0)
			        return i;
            } else if ("(".equals(tokens.get(i))) {
			    level++;
            }
		}
		while(level-- > 0)
            tokens.add(")");
	    return tokens.size()-1;
	}

	private static int matchingQuotes(List<String> tokens, int start) {
		for(int i = start+1; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if("\"".equals(t))
                return i;
        }
        tokens.add("\"");
        return tokens.size()-1;
    }
}
