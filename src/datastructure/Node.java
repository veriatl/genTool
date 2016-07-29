package datastructure;

import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.common.OCL.*;

import Ocl.Printer;



public class Node implements Comparable {


	int level;
	OclExpression content;
	Node parent;
	HashMap<EObject, ContextEntry> context;
	ProveOption rel2Parent;
	Tactic ruleApplied;
	
	public Node(int lv, OclExpression ct, Node pt, HashMap<EObject, ContextEntry> ctx, ProveOption rel, Tactic rule){
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

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
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
			ctx += String.format("%s \t *%s* , [%s]\n",  Printer.print(entry), this.context.get(entry), this.context.get(entry).eliminated);
		}
		
		String h = "";
		if(parent != null){
			h =Integer.toHexString(parent.hashCode());
		}
		
		return String.format("Lv: %d\n Node: %s, Parent: %s\nctx: [%s], \n===\nGoal: %s, \napplied %s", level, Integer.toHexString(this.hashCode()), h, ctx, Printer.print(content), ruleApplied);
	}
}
