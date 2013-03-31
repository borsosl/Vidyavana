package hu.vidyavana.ui;

import hu.vidyavana.db.*;
import hu.vidyavana.db.api.*;
import hu.vidyavana.db.model.Settings;
import hu.vidyavana.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.*;
import org.apache.lucene.index.IndexWriter;
import com.sleepycat.persist.EntityCursor;

public class Main implements UncaughtExceptionHandler
{
	public static Main inst;
	public ExecutorService executor;

	public JFrame frame;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	public String dbCreatedAt;


	public static void main(String[] args)
	{
		setLookAndFeel();

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				inst = new Main();
				inst.main();
			}
		});
	}


	void main()
	{
		Thread.setDefaultUncaughtExceptionHandler(this);
		System.setProperty("sun.awt.exception.handler", getClass().getName());

		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					executor.shutdown();
					executor.awaitTermination(5, TimeUnit.SECONDS);
				}
				catch(Exception ex)
				{
				}
				Db.inst.close();
				Lucene.inst.close();
				Log.close();
			}
		});

		executor = Executors.newSingleThreadExecutor();
		databaseMigration();
		Encrypt.getInstance().init();
		showWindow();
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				SplashScreen sp = SplashScreen.getSplashScreen();
				if(sp != null)
					sp.close();
			}
		});
	}


	@Override
	public void uncaughtException(Thread th, Throwable t)
	{
		Log.error(th.getName(), t);
		while(t.getCause() != null && t.getCause() != t)
			t = t.getCause();
		String msg = t.getMessage();
		if(msg == null || msg.isEmpty())
			msg = t.getClass().getName();
		try
		{
			messageBox(msg, "Hiba");
		}
		catch(Exception e)
		{
			System.out.println(msg);
		}
	}


	private void databaseMigration()
	{
		Db.inst.open(false);
		EntityCursor<Settings> c = Settings.pkIdx().entities();
		Settings set = c.first();
		c.close();
		if(set != null)
			dbCreatedAt = set.createdAt;
		else
		{
			set = new Settings();
			dbCreatedAt = set.createdAt = new Date().toString();
			set.dbMigrate = "0";
			set.booksVersion = "0";
			Settings.pkIdx().put(set);
		}
		Db.inst.open(true);
	}


	private static void setLookAndFeel()
	{
		try
		{
			boolean hasNimbus = false;
			for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if("Nimbus".equals(info.getName()))
				{
					UIManager.setLookAndFeel(info.getClassName());
					hasNimbus = true;
					break;
				}
			}
			if(!hasNimbus)
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
		}
	}


	private void showWindow()
	{
		menuBar = new JMenuBar();
		menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));

		addDatabaseMenu();

		toolBar = new JToolBar();
		toolBar.setBorder(new EtchedBorder());
		toolBar.add(new JButton("Placeholder"));

		JTextArea textArea = new JTextArea();
		textArea.setText("Text area");
		textArea.setFont(new Font("Times", Font.PLAIN, 20));
		textArea.setWrapStyleWord(true);
		JScrollPane textPane = new JScrollPane();
		textPane.setViewportView(textArea);

		frame = new JFrame("Vidyāvana");
		setIcon(frame);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		frame.add(textPane);
		frame.add(toolBar, BorderLayout.NORTH);
		frame.setSize(1024, 600);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}


	private void setIcon(JFrame frame)
	{
		try
		{
			Image im = ImageIO.read(getClass().getResource("/hu/resource/image/vidyavana.gif"));
			frame.setIconImage(im);
		}
		catch(IOException ex)
		{
		}
	}


	public static void messageBox(final String msg, final String caption)
	{
		if(SwingUtilities.isEventDispatchThread())
			messageBoxOnSwingThread(msg, caption);
		else
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					messageBoxOnSwingThread(msg, caption);
				}
			});
	}


	static void messageBoxOnSwingThread(String msg, String caption)
	{
		JOptionPane.showMessageDialog(Main.inst.frame, msg, caption, JOptionPane.PLAIN_MESSAGE);
	}


	private void addDatabaseMenu()
	{
		JMenu dbMenuItem = new JMenu("Database");
		menuBar.add(dbMenuItem);

		dbMenuItem.add(new JMenuItem(new UpdateBooksAction()));
		dbMenuItem.add(new JMenuItem(new AddBooksAction()));
		dbMenuItem.add(new JMenuItem(new TestAction()));
	}


	private final class UpdateBooksAction extends AbstractAction
	{
		public UpdateBooksAction()
		{
			super("Update Books");
		}


		@Override
		public void actionPerformed(ActionEvent e)
		{
			executor.execute(new Runnable()
			{
				@Override
				public void run()
				{
					UpdateBooks ub = new UpdateBooks();
					ub.run();
					messageBox("Új: " + ub.added + ", módosítva: " + ub.updated, "Eredmény");
				}
			});
		}
	}


	private final class AddBooksAction extends AbstractAction
	{
		public AddBooksAction()
		{
			super("Add Books");
		}


		@Override
		public void actionPerformed(ActionEvent e)
		{
			executor.execute(new Runnable()
			{
				@Override
				public void run()
				{
					String[] paths = {};
					IndexWriter writer = Lucene.inst.open().writer();
					for(int bookId = 1; bookId <= paths.length; ++bookId)
					{
						if(bookId == 2 || bookId == 14)
							continue;
						System.out.println(bookId);
						new AddBook(bookId, paths[bookId - 1], writer).run();
					}
					Lucene.inst.closeWriter();
				}
			});
		}
	}


	private final class TestAction extends AbstractAction
	{
		public TestAction()
		{
			super("Test code");
		}


		@Override
		public void actionPerformed(ActionEvent e)
		{
			executor.execute(new Runnable()
			{
				@Override
				public void run()
				{
				}
			});
		}
	}
}
