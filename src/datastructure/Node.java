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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public OclExpression getParent() {
		return parent;
	}

	public void setParent(OclExpression parent) {
		this.parent = parent;
	}

	public ProveOption getRel2Parent() {
		return rel2Parent;
	}

	public void setRel2Parent(ProveOption rel2Parent) {
		this.rel2Parent = rel2Parent;
	}

	public Tactic getRuleApplied() {
		return ruleApplied;
	}

	public void setRuleApplied(Tactic ruleApplied) {
		this.ruleApplied = ruleApplied;
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
		String ctx = "";
		
		for(EObject entry : this.context.keySet()){
			ctx += String.format("%s \t *%s* , \n",  Printer.print(entry), this.context.get(entry));
		}
		
		return String.format("Lv: %d, ctx: [%s], expr: %s, applied %s", level, ctx, Printer.print(content), ruleApplied);
	}
}
