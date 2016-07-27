package contract;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.common.OCL.*;


import datastructure.ContextEntry;
import datastructure.Node;
import datastructure.ProveOption;

public class Elimination {
	
	static boolean terminated(ArrayList<Node> leafs){
		
		boolean rst = true;
		
		for(Node leaf : leafs){
			for(ContextEntry entry : leaf.getContext().values()){
				if(!entry.isEliminated()){
					return false;
				}
			}
		}
		
		return rst;
	}
	
	static void elimin(EObject ocl) {
		System.out.println("triggered");
		
		
	}
}
