package hu.vidyavana.db;

import hu.vidyavana.convert.api.ParagraphCategory;
import hu.vidyavana.convert.api.ParagraphClass;
import hu.vidyavana.db.model.StoragePara;
import hu.vidyavana.search.api.Lucene;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static hu.vidyavana.convert.api.ParagraphCategory.*;

public class IndexBooks
{
	private Lucene lucene;
	private IndexWriter iw;
	private FieldType txtFieldType;
	private int plainBookId;
	private int segment;
	private boolean bookOfSrilaPrabhupada;
	private Map<ParagraphCategory, Float> categoryBoostFactors;


	public IndexBooks(Lucene lucene)
	{
		this.lucene = lucene;
		categoryBoostFactors = new HashMap<>();
		categoryBoostFactors.put(Cim, 5f);
		categoryBoostFactors.put(Alcim, 3f);
		categoryBoostFactors.put(Forditas, 2f);
		categoryBoostFactors.put(SzakaszVers, 1.8f);
		categoryBoostFactors.put(MagyarazatVers, 1.2f);
		categoryBoostFactors.put(Szavak, 0.5f);
		categoryBoostFactors.put(Index, 0.01f);
	}


	public void init()
	{
		iw = lucene.writer();
		txtFieldType = new FieldType();
		txtFieldType.setTokenized(true);
		txtFieldType.setStored(false);
		txtFieldType.setStoreTermVectors(false);
		txtFieldType.setOmitNorms(false);
		txtFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		txtFieldType.freeze();
	}


	public void initBook(int plainBookId, int segment)
	{
		this.plainBookId = plainBookId;
		this.segment = segment;
		bookOfSrilaPrabhupada = (plainBookId <= 27 || plainBookId == 41) && !(plainBookId == 2 && segment >= 10);
	}

	
	public void setBookOfSrilaPrabhupada(boolean bookOfSrilaPrabhupada)
	{
		this.bookOfSrilaPrabhupada = bookOfSrilaPrabhupada;
	}


	public void addPara(int ordinal, ParagraphClass cls, String txt)
	{
		Document doc = new Document();
		doc.add(new IntField("bookId", plainBookId, Store.YES));
		doc.add(new NumericDocValuesField("bookId", plainBookId));
		doc.add(new IntField("segment", segment, Store.YES));
		doc.add(new IntField("ordinal", ordinal, Store.YES));
		doc.add(new NumericDocValuesField("rangeFilterOrdinal",
				StoragePara.rangeFilterOrdinal(plainBookId, segment, ordinal)));
		Field textField = new Field("text", txt, txtFieldType);
		float boost = 1f;
		ParagraphCategory paraCateg = ParagraphCategory.mapFromClass.get(cls);
		Float factor = categoryBoostFactors.get(paraCateg);
		if(factor != null)
		{
			if(cls == ParagraphClass.Versszam && cls != ParagraphClass.Fejezetszam && cls != ParagraphClass.FejezetszamNagy)
				boost *= 0.02f;
			else
				boost *= factor;
		}
		if(bookOfSrilaPrabhupada)
			boost *= 2.5f;
		textField.setBoost(boost);
		doc.add(textField);
		doc.add(new NumericDocValuesField("paraCategory", 1<<paraCateg.ordinal()));
		try
		{
			// TODO transaction
			iw.addDocument(doc);
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Adding para to index: "+txt, ex);
		}
	}


	public void finish()
	{
		lucene.closeWriter();
	}
}
