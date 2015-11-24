package hu.vidyavana.db.api;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import org.apache.lucene.analysis.charfilter.BaseCharFilter;

public class DiscardingHtmlCharFilter extends BaseCharFilter
{
	static enum ChunkType
	{
		Tag('<', '>'),
		Entity('&', ';'),
		Char(' ', ' ');
		
		char start;
		char end;

		ChunkType(char start, char end)
		{
			this.start = start;
			this.end = end;
		}
	}

	
	private ChunkType chunkType = ChunkType.Char;
	private int inCount = 0;
	private int outCount = 0;
	private Map<String, Character> entityMap;


	public DiscardingHtmlCharFilter(Reader in, Map<String, Character> entityMap)
	{
		super(in);
		this.entityMap = entityMap;
	}


	@Override
	public int read(char cbuf[], int off, int len) throws IOException
	{
		int i = 0;
		for(; i < len; ++i)
		{
			int ch = read();
			if(ch == -1)
				break;
			cbuf[off++] = (char) ch;
		}
		return i > 0 ? i : (len == 0 ? 0 : -1);
	}


	@Override
	public int read() throws IOException
	{
		StringBuilder sb = null;
		while(true)
		{
			int c = input.read();
			if(c == -1)
				return c;
			++inCount;
			if(chunkType != ChunkType.Char)
			{
				if(c == chunkType.end)
				{
					Character ch = ' ';
					if(sb != null && entityMap != null)
					{
						ch = entityMap.get(sb.toString());
						if(ch == null)
							ch = ' ';
					}
					++outCount;
					addOffCorrectMap(outCount, inCount-outCount);
					chunkType = ChunkType.Char;
					return ch;
				}
				else if(chunkType == ChunkType.Entity)
					sb.append((char) c);
			}
			else
			{
				if(c == ChunkType.Tag.start)
					chunkType = ChunkType.Tag;
				else if(c == ChunkType.Entity.start)
				{
					chunkType = ChunkType.Entity;
					sb = new StringBuilder();
				}
				if(chunkType == ChunkType.Char)
				{
					++outCount;
					return c;
				}
			}
		}
	}
}
