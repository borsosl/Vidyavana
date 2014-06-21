package test.convert;

import static org.junit.Assert.*;
import hu.vidyavana.convert.indd.InddFileProcessor;
import org.junit.Test;

public class InddParaConvertTest
{
	@Test
	public void allTextSameStyle()
	{
		InddFileProcessor indd = new InddFileProcessor();
		assertEquals(null, indd.allTextSameStyle(""));
		assertEquals(null, indd.allTextSameStyle("1"));
		assertEquals(null, indd.allTextSameStyle("1<b>2</b>"));
		assertEquals("1", indd.allTextSameStyle("1 "));
		assertEquals("1<b>", indd.allTextSameStyle("1<b> "));
		assertEquals("1", indd.allTextSameStyle("<i>1</i>"));
		assertEquals("1", indd.allTextSameStyle("<b><i>1</i></b>"));
		assertEquals("1", indd.allTextSameStyle(" <b><i>1</i> </b>"));
		assertEquals("1", indd.allTextSameStyle("<b><i> 1</i></b>"));
		assertEquals(null, indd.allTextSameStyle("<b><i>123<b>456</i></b>"));
		assertEquals(null, indd.allTextSameStyle("<b><i>1</b></i>"));
		assertEquals(null, indd.allTextSameStyle("<b>1</b>2<b>3</b>"));
	}
	
	
	@Test
	public void emptyTextStyles()
	{
		InddFileProcessor indd = new InddFileProcessor();
		assertEquals(null, indd.emptyTextStyles(""));
		assertEquals(null, indd.emptyTextStyles("1"));
		assertEquals("1", indd.emptyTextStyles("1<i></i>"));
		assertEquals("1 ", indd.emptyTextStyles("1<i> </i>"));
	}
}
