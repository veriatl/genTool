package Ocl;

import org.eclipse.m2m.atl.common.OCL.*;

public class TypeInference {

	
	// tarmm
	
	public static String infer(OclExpression expr){
		String rtn = "Unknown";
		if(expr instanceof OperationCallExp){
			OperationCallExp todo = (OperationCallExp) expr;
			rtn = _infer(todo);
		}else if(expr instanceof OclModelElement){
			OclModelElement todo = (OclModelElement) expr;
			rtn = _infer(todo);
		}
		
		return rtn;
	}
	
	public static String _infer(OperationCallExp expr){
		String rtn = "Unkown";
		if(expr.getOperationName().equals("allInstances")){
			rtn =  infer(expr.getSource());
		}
		return rtn;
	}
	
	public static String _infer(OclModelElement expr){
		
		String rtn = String.format("%s$%s", expr.getModel().getName(),expr.getName());
		
		return rtn;
	}
	
	
}
