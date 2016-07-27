package datastructure;

import org.eclipse.emf.ecore.EObject;

import Ocl.Printer;

public class ContextEntry  {

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

	public String toString() {
		return String.format("%s", n.toString());
	}
	

}

