package hu.vidyavana.ui;

import hu.vidyavana.db.AddBook;
import hu.vidyavana.db.api.Lucene;
import hu.vidyavana.util.Encrypt;
import hu.vidyavana.util.Log;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

public class Main implements UncaughtExceptionHandler
{
	public static Main inst;
	public ExecutorService executor;

	public JFrame frame;
	private JMenuBar menuBar;
	private JToolBar toolBar;


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
//				Db.inst.close();
				Lucene.SYSTEM.close();
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
//		Db.openForWrite();
//		EntityCursor<Settings> c = Settings.pkIdx().entities();
//		Settings set = c.first();
//		c.close();
//		if(set != null)
//			dbCreatedAt = set.createdAt;
//		else
//		{
//			set = new Settings();
//			dbCreatedAt = set.createdAt = new Date().toString();
//			set.dbMigrate = "0";
//			set.booksVersion = "0";
//			Settings.pkIdx().put(set);
//		}
//		Db.openForRead();
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

		dbMenuItem.add(new JMenuItem(new AddBooksAction()));
		dbMenuItem.add(new JMenuItem(new TestAction()));
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
					try
					{
						AddBook.addFromStaticList();
					}
					catch(IOException ex)
					{
						ex.printStackTrace();
					}
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
