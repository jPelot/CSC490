package notation_parser;

public class Die{
	boolean positive;
	int count;
	int value;
	
	public enum Type {
		die,
		constant
	}
	
	public Die() {
		this.positive = true;
		this.count = -1;
		this.value = -1;
	}
	
	public Die(boolean positive, int count, int value) {
		this.positive = positive;
		this.count = count;
		this.value = value;
	}
	
	public Type type() {
		if (value == -1) {
			return Type.constant;
		}
		return Type.die;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public boolean isNegative() {
		return !this.positive;
	}
}
