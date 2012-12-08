package hu.vidyavana.ui;

import hu.vidyavana.db.DatabaseMigration;
import hu.vidyavana.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

public class Main
{
	public static final String PRIMARY_JAR_NAME = "Vidyavana.jar";
	private JMenuBar menuBar;
	private JToolBar toolBar;


	public static void main(String[] args)
	{
		new Main().main();
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

		JFrame frame = new JFrame("Vidyavana");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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


	private final class AddBookAction extends AbstractAction
	{
		public AddBookAction()
		{
			super("Add Book");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
		}
	}


	private void addDatabaseMenu()
	{
		JMenu dbMenuItem = new JMenu("Database");
		menuBar.add(dbMenuItem);

		dbMenuItem.add(new JMenuItem(new AddBookAction()));
	}
}
