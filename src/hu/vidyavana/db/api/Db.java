package hu.vidyavana.db.api;

import java.io.File;
import com.sleepycat.je.*;
import com.sleepycat.persist.*;

public class Db
{
	public static Db inst = new Db();
	
	private boolean readOnly;
	private Environment env;
	private EntityStore store;


	public void open(boolean readOnly)
	{
		if(store != null)
		{
			if(this.readOnly == readOnly)
				return;
			close();
		}

		try
		{
			this.readOnly = readOnly;
			File path = new File("./text");
			path.mkdirs();
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setAllowCreate(!readOnly);
			env = new Environment(path, envConfig);

			StoreConfig storeConfig = new StoreConfig();
			storeConfig.setAllowCreate(!readOnly);
			store = new EntityStore(env, "", storeConfig);
		}
		catch(DatabaseException ex)
		{
			throw new RuntimeException(ex);
		}
	}


	public void close()
	{
		if(store != null)
		{
			try
			{
				store.close();
				store = null;
			}
			catch(DatabaseException ex)
			{
				throw new RuntimeException(ex);
			}
		}

		if(env != null)
		{
			try
			{
				env.close();
			}
			catch(DatabaseException ex)
			{
				throw new RuntimeException(ex);
			}
		}
	}


	public static Environment env()
	{
		return inst.env;
	}


	public static EntityStore store()
	{
		return inst.store;
	}
}
