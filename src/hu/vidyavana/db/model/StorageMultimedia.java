package hu.vidyavana.db.model;

import java.io.IOException;
import java.io.RandomAccessFile;

public interface StorageMultimedia
{
	int getLength();
	byte[] getObject();
	void write(RandomAccessFile out) throws IOException;
	void read(RandomAccessFile in) throws IOException;
}
