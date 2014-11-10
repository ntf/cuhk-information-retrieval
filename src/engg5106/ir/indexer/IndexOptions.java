package engg5106.ir.indexer;

public class IndexOptions {
	private String field;
	private Type type;

	public static enum Type {
		Keyword, Tokenize
	};

	public IndexOptions(String field, Type type) {
		this.field = field;
		this.type = type;
	}

	public String getField() {
		return this.field;
	}

	public Type getType() {
		return this.type;
	}
}
