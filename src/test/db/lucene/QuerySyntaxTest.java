package test.db.lucene;

import org.apache.lucene.search.BooleanClause;
import org.junit.Assert;
import org.junit.Test;

import static hu.vidyavana.search.task.VedabaseQueryParser.*;
import static hu.vidyavana.search.task.VedabaseQueryParser.ItemType.*;
import static org.apache.lucene.search.BooleanClause.Occur.*;

public class QuerySyntaxTest {

    @Test
    public void oneWord() {
        ExpressionList root = initRootExpressionList("x");
        parseTokens(root);
        Assert.assertEquals(1, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
    }

    @Test
    public void oneWildcardWord() {
        ExpressionList root = initRootExpressionList("xyz*");
        parseTokens(root);
        Assert.assertEquals(1, root.items.size());
        assertItem(root, 0, Wildcard, MUST, "xyz*");
    }

    @Test
    public void twoWord() {
        ExpressionList root = initRootExpressionList("x y");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
        assertItem(root, 1, Term, MUST, "y");
    }

    @Test
    public void twoWordOr() {
        ExpressionList root = initRootExpressionList("x|y");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, SHOULD, "x");
        assertItem(root, 1, Term, SHOULD, "y");
    }

    @Test
    public void twoWordLiteralOr() {
        ExpressionList root = initRootExpressionList("x or y");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, SHOULD, "x");
        assertItem(root, 1, Term, SHOULD, "y");
    }

    @Test
    public void twoWordMisplacedOr() {
        ExpressionList root = initRootExpressionList("|x y");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
        assertItem(root, 1, Term, MUST, "y");
    }

    @Test
    public void twoWordNot() {
        ExpressionList root = initRootExpressionList("x -y");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
        assertItem(root, 1, Term, MUST_NOT, "y");
    }

    @Test
    public void twoWordMisplacedNot() {
        ExpressionList root = initRootExpressionList("!x|y");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, SHOULD, "x");
        assertItem(root, 1, Term, SHOULD, "y");
    }

    @Test
    public void automaticAndSubquery() {
        ExpressionList root = initRootExpressionList("x y |z");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, List, SHOULD, null);
        assertItem(root, 1, Term, SHOULD, "z");
        ExpressionList list = root.items.get(0).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, MUST, "x");
        assertItem(list, 1, Term, MUST, "y");
    }

    @Test
    public void automaticOrSubquery() {
        ExpressionList root = initRootExpressionList("x|y z");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, List, MUST, null);
        assertItem(root, 1, Term, MUST, "z");
        ExpressionList list = root.items.get(0).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, SHOULD, "x");
        assertItem(list, 1, Term, SHOULD, "y");
    }

    @Test
    public void automaticMixedSubquery() {
        ExpressionList root = initRootExpressionList("x|y+z|w");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, List, SHOULD, null);
        assertItem(root, 1, Term, SHOULD, "w");
        ExpressionList list = root.items.get(0).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, List, MUST, null);
        assertItem(list, 1, Term, MUST, "z");
        list = list.items.get(0).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, SHOULD, "x");
        assertItem(list, 1, Term, SHOULD, "y");
    }

    @Test
    public void automaticMixedSubqueryTwoAnd() {
        ExpressionList root = initRootExpressionList("x|y+z+xyz*|w");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, List, SHOULD, null);
        assertItem(root, 1, Term, SHOULD, "w");
        ExpressionList list = root.items.get(0).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.items.size());
        assertItem(list, 0, List, MUST, null);
        assertItem(list, 1, Term, MUST, "z");
        assertItem(list, 2, Wildcard, MUST, "xyz*");
        list = list.items.get(0).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, SHOULD, "x");
        assertItem(list, 1, Term, SHOULD, "y");
    }

    @Test
    public void oneWordParen() {
        ExpressionList root = initRootExpressionList("(x)");
        parseTokens(root);
        Assert.assertEquals(1, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
    }

    @Test
    public void twoWordParen() {
        ExpressionList root = initRootExpressionList("(x y)");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
        assertItem(root, 1, Term, MUST, "y");
    }

    @Test
    public void andSubqueryParen() {
        ExpressionList root = initRootExpressionList("x|(y+z)");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, SHOULD, "x");
        assertItem(root, 1, List, SHOULD, null);
        ExpressionList list = root.items.get(1).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, MUST, "y");
        assertItem(list, 1, Term, MUST, "z");
    }

    @Test
    public void orSubqueryParen() {
        ExpressionList root = initRootExpressionList("x(y|z)");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
        assertItem(root, 1, List, MUST, null);
        ExpressionList list = root.items.get(1).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, SHOULD, "y");
        assertItem(list, 1, Term, SHOULD, "z");
    }

    @Test
    public void multiAndSubqueryParen() {
        ExpressionList root = initRootExpressionList("(x+y)|(v+z)");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, List, SHOULD, null);
        assertItem(root, 1, List, SHOULD, null);
        ExpressionList list = root.items.get(0).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, MUST, "x");
        assertItem(list, 1, Term, MUST, "y");
        list = root.items.get(1).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, MUST, "v");
        assertItem(list, 1, Term, MUST, "z");
    }

    @Test
    public void andNotOrSubqueryParen() {
        ExpressionList root = initRootExpressionList("x+ymi*-(v|z)");
        parseTokens(root);
        Assert.assertEquals(3, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
        assertItem(root, 1, Wildcard, MUST, "ymi*");
        assertItem(root, 2, List, MUST_NOT, null);
        ExpressionList list = root.items.get(2).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, SHOULD, "v");
        assertItem(list, 1, Term, SHOULD, "z");
    }

    @Test
    public void phrase() {
        ExpressionList root = initRootExpressionList("\"x y\" z");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        ExpressionItem item = assertItem(root, 0, Phrase, MUST, null);
        Assert.assertEquals(2, item.phrases.length);
        Assert.assertEquals("x", item.phrases[0]);
        Assert.assertEquals("y", item.phrases[1]);
        assertItem(root, 1, Term, MUST, "z");
    }

    @Test
    public void proximity() {
        ExpressionList root = initRootExpressionList("\"x y\"~3 z");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        ExpressionItem item = assertItem(root, 0, Proximity, MUST, null);
        Assert.assertEquals(2, item.phrases.length);
        Assert.assertEquals("x", item.phrases[0]);
        Assert.assertEquals("y", item.phrases[1]);
        Assert.assertEquals(3, item.proximity);
        assertItem(root, 1, Term, MUST, "z");
    }

    @Test
    public void multiParen() {
        ExpressionList root = initRootExpressionList("x (y !((z/x)))");
        parseTokens(root);
        Assert.assertEquals(2, root.items.size());
        assertItem(root, 0, Term, MUST, "x");
        assertItem(root, 1, List, MUST, null);
        ExpressionList list = root.items.get(1).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, MUST, "y");
        assertItem(list, 1, List, MUST_NOT, null);
        list = list.items.get(1).sub;
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.items.size());
        assertItem(list, 0, Term, SHOULD, "z");
        assertItem(list, 1, Term, SHOULD, "x");
    }

    private ExpressionItem assertItem(ExpressionList list, int ix, ItemType type, BooleanClause.Occur connect, String text) {
        ExpressionItem item = list.items.get(ix);
        Assert.assertEquals(type, item.type);
        Assert.assertEquals(connect, item.connect);
        Assert.assertEquals(text, item.text);
        return item;
    }
}
