package Ocl;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.m2m.atl.common.OCL.*;

import metamodel.EMFLoader;

public class TypeInference {

	

	
	public static String infer(OclExpression expr, EPackage mm){
		String rtn = "Unknown";
		if(expr instanceof OperationCallExp){
			OperationCallExp todo = (OperationCallExp) expr;
			rtn = _infer(todo,mm);
		}else if(expr instanceof OclModelElement){
			OclModelElement todo = (OclModelElement) expr;
			rtn = _infer(todo,mm);
		}else if(expr instanceof NavigationOrAttributeCallExp){
			NavigationOrAttributeCallExp todo = (NavigationOrAttributeCallExp) expr;
			rtn = _infer(todo,mm);
		}
		
		return rtn;
	}
	
	public static String _infer(OperationCallExp expr, EPackage mm){
		String rtn = "Unkown";
		if(expr.getOperationName().equals("allInstances")){
			rtn =  infer(expr.getSource(),mm);
		}
		return rtn;
	}
	
	public static String _infer(OclModelElement expr, EPackage mm){
		String rtn = String.format("%s$%s", expr.getModel().getName(),expr.getName());
		return rtn;
	}
	
	public static String _infer(NavigationOrAttributeCallExp expr, EPackage mm){
		
		String srcType = infer(expr.getSource(),mm);
		
		return EMFLoader.getStructuralFeatureType(srcType, expr.getName(),mm);
		
	}
	
	
	
	public static boolean isPrimitive(String tp){
		if(tp.equals("EInt") || tp.equals("EBoolean") || tp.equals("EString")){
			return true;
		}else{
			return false;
		}
	}
	
	
}
