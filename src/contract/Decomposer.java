package contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.m2m.atl.common.OCL.*;

import org.eclipse.m2m.atl.emftvm.ExecEnv;

import Ocl.TypeInference;
import metamodel.EMFLoader;
import transformation.Trace;





public class Decomposer extends OperatorCallExp{

	static Map<String, ArrayList<String>> trace;
	static OCLFactory make = OCLFactory.init();
	
	
	static void decompose(OclExpression expr, HashMap<EObject, String> Inferred){
		if(expr instanceof IteratorExp){
			IteratorExp todo = (IteratorExp) expr;
			_decompose(todo, Inferred);
		}
	}
	
	
	static void _decompose(IteratorExp expr, HashMap<EObject, String> Inferred){
		Iterator bv = EcoreUtil.copy(expr.getIterators().get(0));
		OclExpression loopBody = EcoreUtil.copy(expr.getBody());
		OclExpression loopSrc = EcoreUtil.copy(expr.getSource());
		String bvType = TypeInference.infer(loopSrc);
		

		
		
		for(String rule : trace.get(bvType)){
			//representCurrent
			OperationCallExp genBy = make.createOperationCallExp();
			IteratorExp newIter = make.createIteratorExp();
			OperatorCallExp newBody = make.createOperatorCallExp();
			
			newBody.setOperationName("implies");
			newBody.setSource(genBy);
			
			newBody.getArguments().add(loopBody);
			newIter.basicSetSource(loopSrc, null);
			newIter.getIterators().add(bv);
			
			newIter.setBody(newBody);
			
			// decompose further
			HashMap<EObject, String> newInferred = new HashMap<EObject, String>(Inferred);
			newInferred.put(bv, rule);
			
			decompose(loopBody, newInferred) ;
			
		}
		
		
		
	}
	
	
	
	public static void main(String[] args) throws Exception{
		ExecEnv env = Trace.moduleLoader(args[0], args[1],args[2], args[3],args[4], args[5]);
		EPackage tarmm = EMFLoader.loadEcore(args[3]);
		trace = Trace.getTrace(tarmm, env);
		
		
		List<OclExpression> postconditions = ContractLoader.init("HSM2FSM/Source/ContractSRC/HSM2FSMContract.atl");
		
		
		for(OclExpression post : postconditions){
			decompose(post, new HashMap<EObject, String>());
		}
 
	}
}
