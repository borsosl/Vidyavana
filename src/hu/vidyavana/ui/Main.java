package hu.vidyavana.ui;

import hu.vidyavana.db.*;
import hu.vidyavana.db.api.Database;
import hu.vidyavana.db.data.SettingsDao;
import hu.vidyavana.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.*;

public class Main implements UncaughtExceptionHandler
{
	public static final String PRIMARY_JAR_NAME = "Vidyavana.jar";
	public static Main instance;

	private JMenuBar menuBar;
	private JToolBar toolBar;
	public JFrame frame;
	public ExecutorService executor;
	public String dbCreatedAt;


	public static void main(String[] args)
	{
		try
		{
			for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if("Nimbus".equals(info.getName()))
				{
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch(Exception e)
		{
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				instance = new Main();
				instance.main();
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
				Database.closeAll();
				Log.close();
			}
		});

		executor = Executors.newSingleThreadExecutor();
		databaseMigration();
		Encrypt.getInstance().init();
		setLookAndFeel();
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
		DatabaseMigration dbm = new DatabaseMigration();
		if(!ResourceUtil.dbMigrationUsingJar(dbm))
			if(!ResourceUtil.dbMigrationUsingFiles(dbm))
				throw new RuntimeException("SQL fájl olvasási hiba.");
		dbCreatedAt = SettingsDao.getCreatedAt();
	}


	private void setLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception ex)
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
		JOptionPane.showMessageDialog(Main.instance.frame, msg, caption, JOptionPane.PLAIN_MESSAGE);
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
					for(int bookId = 1; bookId <= paths.length; ++bookId)
					{
						if(bookId == 2 || bookId == 14)
							continue;
						System.out.println(bookId);
						new AddBook(bookId, paths[bookId - 1]).run();
						new IndexBook(bookId).run();
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
