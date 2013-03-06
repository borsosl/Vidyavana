package hu.vidyavana.ui;

import hu.vidyavana.db.*;
import hu.vidyavana.db.api.Database;
import hu.vidyavana.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

public class Main
{
	public static final String PRIMARY_JAR_NAME = "Vidyavana.jar";
	public static Main instance;

	private JMenuBar menuBar;
	private JToolBar toolBar;
	public JFrame frame;


	public static void main(String[] args)
	{
		instance = new Main();
		instance.main();
	}


	private void main()
	{
		try
		{
			databaseMigration();
			setLookAndFeel();
			showWindow();
		}
		catch(Throwable t)
		{
			Log.error(null, t);
			System.out.println(t.getMessage());
			System.exit(1);
		}
		finally
		{
			Database.closeAll();
		}
	}


	private void databaseMigration()
	{
		DatabaseMigration dbm = new DatabaseMigration();
		if(!ResourceUtil.dbMigrationUsingJar(dbm))
			if(!ResourceUtil.dbMigrationUsingFiles(dbm))
			{
				System.out.println("SQL file olvasasi hiba.");
				System.exit(1);
			}
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
		//	    JButton exampleButton = new JButton(exampleAction);
		//	    toolBar.add(exampleButton);

		frame = new JFrame("Vidyavana");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				Database.closeAll();
				frame.dispose();
				System.exit(0);
			}
		});
		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		frame.setSize(1024, 600);
		frame.setVisible(true);
		setIcon(frame);
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

	
	public static void messageBox(String msg, String caption)
	{
		JOptionPane.showMessageDialog(Main.instance.frame, msg, caption, JOptionPane.PLAIN_MESSAGE);
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
			UpdateBooks ub = new UpdateBooks();
			ub.run();
			messageBox("Új: "+ub.added+", módosítva: "+ub.updated, "Eredmény");
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
			int bookId = Integer.parseInt(System.getProperty("bookId"));
			new AddBook(bookId).run();
		}
	}


	private final class IndexAction extends AbstractAction
	{
		public IndexAction()
		{
			super("Indexing");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int bookId = Integer.parseInt(System.getProperty("bookId"));
			new IndexBook(bookId).run();
		}
	}


	private void addDatabaseMenu()
	{
		JMenu dbMenuItem = new JMenu("Database");
		menuBar.add(dbMenuItem);

		dbMenuItem.add(new JMenuItem(new UpdateBooksAction()));
		dbMenuItem.add(new JMenuItem(new AddBooksAction()));
		dbMenuItem.add(new JMenuItem(new IndexAction()));
	}
}
