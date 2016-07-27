package datastructure;




public class ContextEntry {

	ContextNature n;
	boolean eliminated;
	


	public ContextEntry(ContextNature nature){
		this.n = nature;
		this.eliminated = false;
	}
	
	public boolean isEliminated() {
		return eliminated;
	}

	public void setEliminated(boolean eliminated) {
		this.eliminated = eliminated;
	}

	
}

