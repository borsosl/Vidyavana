package hu.vidyavana.db.model;

import java.io.Serializable;
import java.util.Date;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import hu.vidyavana.util.Encrypt;

public class User implements Serializable
{
	public static enum AdminLevel
	{
		// never rename old ones, serialized by name
		None,
		Full,
		BookRights
	}
	
	public long id;
	public AdminLevel adminLevel;
	public String email;
	public String password;
	public String name;
	public String regToken;
	public String accessStr;
	
	
	public void setDefaults()
	{
		id = new Date().getTime();
		adminLevel = AdminLevel.None;
		if(name == null)
			name = "";
		else
			name = name.trim();
		regToken = Encrypt.md5(""+id);
		accessStr = "BhG|NoI|SSR";
	}
	
	
	public User fromDoc(Document doc, boolean forList)
	{
		id = (Long)((StoredField) doc.getField("id")).numericValue();
		adminLevel = AdminLevel.valueOf(doc.getField("admin").stringValue());
		email = doc.getField("email").stringValue();
		name = doc.getField("name").stringValue();
		if(!forList)
		{
			password = doc.getField("password").stringValue();
			regToken = doc.getField("reg-token").stringValue();
			accessStr = doc.getField("access").stringValue();
		}
		return this;
	}
	
	
	public Document toDoc(Document doc)
	{
		doc.add(new LongField("id", id, Store.YES));
		doc.add(new StringField("admin", adminLevel.name(), Store.YES));
		doc.add(new StringField("email", email, Store.YES));
		doc.add(new StringField("password", password, Store.YES));
		doc.add(new StringField("name", name, Store.YES));
		doc.add(new StringField("reg-token", regToken, Store.YES));
		doc.add(new StringField("access", accessStr, Store.YES));
		return doc;
	}
	
	@Override
	public String toString()
	{
		if(name == null || name.trim().isEmpty())
			return email;
		return name + " (" + email + ")";
	}
}
