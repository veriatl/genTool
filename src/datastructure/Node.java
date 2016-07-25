package datastructure;

import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.common.OCL.*;



public class Node implements Comparable {


	int level;
	OclExpression content;
	OclExpression parent;
	HashMap<EObject, ContextNature> context;
	ProveOption rel2Parent;
	Tactic ruleApplied;
	
	public Node(int lv, OclExpression ct, OclExpression pt, HashMap<EObject, ContextNature> ctx, ProveOption rel, Tactic rule){
		this.level = lv;
		this.content = ct;
		this.parent = pt;
		this.context = ctx;
		this.rel2Parent = rel;
		this.ruleApplied = rule;
	}

	@Override
	public int compareTo(Object other) {
		
		if(other instanceof Node){
			return this.level - ((Node)other).level;
		}else{
			return 0;
		}
		
		
	}
	
	@Override
	public String toString() {
		
		return String.format("Lv: %d, ctx: %s, applied %s", level, context.toString(), ruleApplied);
	}
}
