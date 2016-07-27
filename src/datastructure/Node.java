package datastructure;

import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.common.OCL.*;

import Ocl.Printer;



public class Node implements Comparable {


	int level;
	OclExpression content;
	OclExpression parent;
	HashMap<EObject, ContextEntry> context;
	ProveOption rel2Parent;
	Tactic ruleApplied;
	
	public Node(int lv, OclExpression ct, OclExpression pt, HashMap<EObject, ContextEntry> ctx, ProveOption rel, Tactic rule){
		this.level = lv;
		this.content = ct;
		this.parent = pt;
		this.context = ctx;
		this.rel2Parent = rel;
		this.ruleApplied = rule;
	}

	public OclExpression getContent() {
		return content;
	}

	public void setContent(OclExpression content) {
		this.content = content;
	}

	public HashMap<EObject, ContextEntry> getContext() {
		return context;
	}

	public void setContext(HashMap<EObject, ContextEntry> context) {
		this.context = context;
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
		
		return String.format("Lv: %d, expr: %s, applied %s", level, Printer.print(content), ruleApplied);
	}
}
