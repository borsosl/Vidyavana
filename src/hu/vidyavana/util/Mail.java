package hu.vidyavana.util;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail
{
	public static String host;
	public static String port;
	public static String from;
	public static String bounce;
	public static String password;


	public static void init()
	{
		host = Conf.get("smtp.host");
		port = Conf.get("smtp.port");
		from = Conf.get("smtp.from");
		bounce = Conf.get("smtp.bounce");
		password = Conf.get("smtp.password");
	}


	public static void send(String email, String subject, String htmlText)
	{
		if(host == null)
			init();
		
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.from", bounce);
		Session session = Session.getInstance(props, new Authenticator() {
	        @Override
			protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(from, password);
	        }
	    });

		try
		{
			MimeMessage msg = new MimeMessage(session);
			msg.addFrom(InternetAddress.parse(bounce));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			msg.setText(htmlText, "UTF-8", "html");
			Transport.send(msg);
		}
		catch(MessagingException mex)
		{
			Log.error("Sending email", mex);
			Exception ex = mex;
			do
			{
				if(ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException) ex;
					Address[] invalid = sfex.getInvalidAddresses();
					if(invalid != null)
						Log.warning("Invalid address: " + email, null);

					Address[] validUnsent = sfex.getValidUnsentAddresses();
					if(validUnsent != null)
						Log.warning("E-mail address valid, but unsent: " + email, null);
				}
				if(ex instanceof MessagingException)
					ex = ((MessagingException) ex).getNextException();
				else
					ex = null;
			}
			while(ex != null);
		}
	}


	public static void register(String email, String token)
	{
		List<String> lines = FileUtil.readTextFile(new File(Globals.cwd, "../../admin/register-email.html"));
		StringBuilder sb = new StringBuilder();
		for(String line : lines)
		{
			line = line.trim();
			int ix = line.indexOf("?email=");
			if(ix > -1)
				line = line.substring(0, ix+7) + email + line.substring(ix+7);
			ix = line.indexOf("&token=");
			if(ix > -1)
				line = line.substring(0, ix+7) + token + line.substring(ix+7);
			sb.append(line);
		}
		send(email, "pandit.hu regisztráció megerősítése", sb.toString());
	}
}
