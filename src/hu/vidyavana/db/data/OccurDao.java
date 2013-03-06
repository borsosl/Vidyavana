package hu.vidyavana.db.data;

import hu.vidyavana.db.api.Database;
import java.util.*;

public class OccurDao
{
	public static void updateOneBook(int bookId, HashMap<String, HashSet<Integer>> words)
	{
		if(words.size() == 0)
			return;


		// get id's of existing words
		HashMap<String, Integer> wordIds = WordDao.getAllWords();

		// find new words
		List<String> newWords = new ArrayList<>();
		for(String word : words.keySet())
			if(wordIds.get(word) == null)
				newWords.add(word);
		
		// extended word list and get id's
		WordDao.addWords(newWords);
		wordIds = WordDao.getAllWords();
		
		// add occurences
		deleteBookOccurences(bookId);
		StringBuilder sb = new StringBuilder(100 + words.size() * 1000);
		sb.append("insert into occur (word_id, book_id, book_para_ordinal) values ");
		for(Map.Entry<String, HashSet<Integer>> e : words.entrySet())
		{
			String word = e.getKey();
			int wordId = wordIds.get(word);
			HashSet<Integer> set = e.getValue();
			for(Integer ordinal : set)
			{
				sb.append('(').append(wordId).append(',').append(bookId).append(',')
					.append(ordinal).append("),");
			}
		}
		sb.setLength(sb.length()-1);
		Database.System.execute(sb.toString());
	}

	
	public static void deleteBookOccurences(int bookId)
	{
		Database.System.execute("delete from occur where book_id=" + bookId);
	}
}
