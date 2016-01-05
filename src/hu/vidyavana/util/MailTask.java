package hu.vidyavana.util;

public class MailTask implements Runnable
{
	private String template;
	private String email;
	private String token;


	public MailTask(String template, String email, String token)
	{
		this.template = template;
		this.email = email;
		this.token = token;
	}


	@Override
	public void run()
	{
		if("register".equals(template))
			Mail.register(email, token);
	}
}
