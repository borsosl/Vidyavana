package test.db;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;


public class RandomAccessTest
{
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception
	{
		RandomAccessFile raf = new RandomAccessFile("vid.dat", "rw");
		FileChannel fc = raf.getChannel();
		raf.close();
	}
}
