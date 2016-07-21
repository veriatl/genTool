package datastructure;

import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.common.OCL.*;



public class Node {


	int level;
	OclExpression content;
	OclExpression parent;
	HashMap<EObject, String> context;
	ProveOption rel2Parent;
	Tactic ruleApplied;
	
	public Node(int lv, OclExpression ct, OclExpression pt, HashMap<EObject, String> ctx, ProveOption rel, Tactic rule){
		this.level = lv;
		this.content = ct;
		this.parent = pt;
		this.context = ctx;
		this.rel2Parent = rel;
		this.ruleApplied = rule;
	}
	
	
}
