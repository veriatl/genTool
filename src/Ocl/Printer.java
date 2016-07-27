package Ocl;

import java.util.HashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.common.OCL.*;


import datastructure.ContextEntry;
import datastructure.Node;
import datastructure.ProveOption;
import datastructure.Tactic;

public class Printer {

	static String indent(int depth) {
		String rtn = "";
		for (int i = 0; i < depth; i++) {
			rtn += "\t";
		}
		return rtn;
	}
	
	public static String print(OclExpression expr){
		String rtn = "Unknown";
		
		if(expr == null){
			return rtn;
		}
		
		if(expr instanceof IteratorExp){
			IteratorExp todo = (IteratorExp) expr;
			rtn = _print(todo);
		}else if(expr instanceof OperatorCallExp){
			OperatorCallExp todo = (OperatorCallExp) expr;
			rtn = _print(todo);
		}else if(expr instanceof OperationCallExp){
			OperationCallExp todo = (OperationCallExp)expr;
			rtn = _print(todo);
		}else if (expr instanceof OclModelElement){
			OclModelElement todo = (OclModelElement)expr;
			rtn = _print(todo);
		}else if (expr instanceof VariableExp){
			VariableExp todo = (VariableExp)expr;
			rtn = _print(todo);
		}else if (expr instanceof NavigationOrAttributeCallExp){
			NavigationOrAttributeCallExp todo = (NavigationOrAttributeCallExp)expr;
			rtn = _print(todo);
		}
		
		return rtn;
	}
	
	static String _print(IteratorExp expr) {
		String rtn = "Unknown";
		Iterator bv = expr.getIterators().get(0);
		OclExpression loopBody = expr.getBody();
		OclExpression loopSrc = expr.getSource();



		
		if (expr.getName().toLowerCase().equals("forall")) {		
			rtn = String.format("%s->forall(%s|%s)", print(loopSrc), bv.getVarName(), print(loopBody));	
		}else if (expr.getName().toLowerCase().equals("exists")) {	
			rtn = String.format("%s->exists(%s|%s)", print(loopSrc), bv.getVarName(), print(loopBody));	
		}	

		
		return rtn;
	}
	
	static String _print(OperatorCallExp expr){
		String rtn = "Unknown";
		if(expr.getOperationName().equals("implies")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			rtn = String.format("%s implies %s", print(lhs), print(rhs));
			
		}else if(expr.getOperationName().equals("and")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			rtn = String.format("%s and %s", print(lhs), print(rhs));
				
		}else if(expr.getOperationName().equals("or")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			rtn = String.format("%s or %s", print(lhs), print(rhs));	
			
		}else if(expr.getOperationName().equals("not")){
			OclExpression src = expr.getSource();

			rtn = String.format("not %s", print(src));	
			
		}else if(expr.getOperationName().equals("<>")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			rtn = String.format("%s <> %s", print(lhs), print(rhs));	
			
		}
		
		return rtn;
		
		
	}
	
	public static String _print(OperationCallExp expr){
		String rtn = "Unkown";
		if(expr.getOperationName().equals("allInstances")){
			OclExpression src = expr.getSource();
			rtn =  String.format("%s->allInstances()", print(src));
		}
		return rtn;
	}
	
	public static String _print(OclModelElement expr){		
		String rtn = String.format("%s$%s", expr.getModel().getName(),expr.getName());	
		return rtn;
	}

	
	public static String _print(VariableExp expr){		
		String rtn = String.format("%s", expr.getReferredVariable().getVarName());	
		return rtn;
	}
	
	public static String _print(NavigationOrAttributeCallExp expr){		
		String rtn = String.format("%s.%s", print(expr.getSource()), expr.getName());	
		return rtn;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
