package runtime;

public class VerificationResult {
	boolean result;
	long time;
	String id;
	
	public VerificationResult(String id, boolean r, long t){
		this.id=id;
		this.result = r;
		this.time = t;
	}
	
	public String toString(){
		return String.format("Id:%s, Time: %s, Result: %s", this.id, this.time, this.result);
	}
}
