package hu.vidyavana.convert.font;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

public class SpecialFontToUnicode
{

	public static void main(String[] args)
	{
		Pattern codes = Pattern.compile("^(\\d+)\\t(\\d+)");
		String path = "";
		try(InputStreamReader is = new InputStreamReader(new FileInputStream(path), "UTF8");
			Scanner scanner = new Scanner(System.in))
		{
			BufferedReader br = new BufferedReader(new FileReader("cmap.txt"));
			Map<Integer, Character> cmap = new HashMap<Integer, Character>();
			boolean cmapChanged = false;
			String ln;
			while((ln = br.readLine()) != null)
			{
				Matcher m = codes.matcher(ln);
				if(m.find())
				{
					Integer key = Integer.valueOf(m.group(1));
					char value = (char) Integer.valueOf(m.group(2)).intValue();
					cmap.put(key, value);
				}
			}
			br.close();

			int c;
			StringBuilder sb = new StringBuilder();
			while((c=is.read())!=-1)
			{
				if(c<127)
					sb.append((char) c);
				else
				{
					Character k = cmap.get(c);
					if(k != null)
						sb.append(k);
					else
					{
						if(c == 65279)
							continue;
						System.out.println(sb.substring(Math.max(sb.length()-30, 0), sb.length()));
						String s = scanner.next();
						if("?".equals(s))
							sb.append(s);
						else if("X".equals(s))
							break;
						else
						{
							cmap.put(c, s.charAt(0));
							cmapChanged = true;
							sb.append(s);
						}
					}
				}
			}
			if(cmapChanged)
			{
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("cmap.txt"), "UTF8"));
				for(Entry<Integer, Character> e : cmap.entrySet())
				{
					char k = (char) e.getKey().intValue();
					bw.write(""+e.getKey()+"\t"+((int) e.getValue().charValue())+"\t"+k+"\t"+e.getValue()+"\r\n");
				}
				bw.close();
			}
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+".utf8"), "UTF8"));
			bw.write(sb.toString());
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
