package engg5106.ir;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

public class Document implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Map<String, String> fields;

	Document() {
		this.fields = new HashMap<String, String>();
	}

	public void addField(String name, String content) {
		this.fields.put(name, content);
	}

	public String getField(String name) {
		return this.fields.get(name);
	}

	/**
	 * permalink is used as key
	 */
	public String key() {
		return DigestUtils.sha1Hex(this.fields.get("permalink"));
	}
}
