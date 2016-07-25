package contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.m2m.atl.common.OCL.*;

import org.eclipse.m2m.atl.emftvm.ExecEnv;

import Ocl.TypeInference;
import datastructure.ContextNature;
import datastructure.Node;
import datastructure.ProveOption;
import datastructure.Tactic;
import metamodel.EMFLoader;
import transformation.Trace;


//TODO
// Control bv, forall a2 implies exists a2




public class Introduction extends OperatorCallExp {

	static Map<String, ArrayList<String>> trace;
	
	static ArrayList<Node> tree = new ArrayList<Node>();
	

	
	
	
	static private OCLFactory make = OCLFactory.init();
	
	//static OCLFactory make = OCLFactory.init();

	static String indent(int depth) {
		String rtn = "";
		for (int i = 0; i < depth; i++) {
			rtn += "\t";
		}
		return rtn;
	}

	static void introduction(OclExpression expr, HashMap<EObject, ContextNature> Inferred, int depth, ProveOption op) {
		
		if (expr instanceof IteratorExp) {
			IteratorExp todo = (IteratorExp) expr;
			_introduction(todo, Inferred, depth, op);
		}else if(expr instanceof OperatorCallExp){
			OperatorCallExp todo = (OperatorCallExp) expr;
			_introduction(todo, Inferred, depth, op);
		}
		
		
	}

	static void _introduction(IteratorExp expr, HashMap<EObject, ContextNature> Inferred, int depth, ProveOption op) {

		Iterator bv = expr.getIterators().get(0);
		OclExpression loopBody = expr.getBody();
		OclExpression loopSrc = expr.getSource();
		String bvType = TypeInference.infer(loopSrc);


		
		if (expr.getName().toLowerCase().equals("forall")) {		

			HashMap<EObject, ContextNature> inferNextLv = new HashMap<EObject, ContextNature>(Inferred);
			inferNextLv.put(bv, ContextNature.BV);
			// bv in col needs to be in the context
			
			
			
			Node n = new Node(depth + 1, loopBody, expr, inferNextLv, ProveOption.EACH, Tactic.FORALL_INTRO);
			tree.add(n);
			
			introduction(loopBody, inferNextLv, depth + 1, ProveOption.EACH);
			

		}else if (expr.getName().toLowerCase().equals("exists")) {	
			HashMap<EObject, ContextNature> inferNextLv = new HashMap<EObject, ContextNature>(Inferred);
			inferNextLv.put(bv, ContextNature.BV);
			// bv in col needs to be in the context
			
			
			Node n = new Node(depth + 1, loopBody, expr, inferNextLv, ProveOption.ANY, Tactic.EXISTS_INTRO);
			tree.add(n);
			
			introduction(loopBody, inferNextLv, depth + 1, ProveOption.ANY);
		}	

		
		
	}
	
	static void _introduction(OperatorCallExp expr, HashMap<EObject, ContextNature> Inferred, int depth, ProveOption op){
		
		if(expr.getOperationName().equals("implies")){
			
			OclExpression rhs = expr.getArguments().get(0);
			
			HashMap<EObject, ContextNature> inferNextLv = new HashMap<EObject, ContextNature>(Inferred);
			inferNextLv.put(expr.getSource(), ContextNature.ASSUME);
			
			Node n = new Node(depth + 1, rhs, expr, inferNextLv, op, Tactic.IMPLY_INTRO);
			tree.add(n);
			introduction(rhs, inferNextLv, depth + 1, op);
			
		}else if(expr.getOperationName().equals("and")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			Node n1 = new Node(depth + 1, lhs, expr, Inferred, ProveOption.EACH, Tactic.SPLIT);
			tree.add(n1);
			introduction(lhs, Inferred, depth + 1, ProveOption.EACH);
			
			Node n2 = new Node(depth + 1, rhs, expr, Inferred, ProveOption.EACH, Tactic.SPLIT);
			tree.add(n2);
			introduction(rhs, Inferred, depth + 1, ProveOption.EACH);
				
		}else if(expr.getOperationName().equals("or")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			Node n1 = new Node(depth + 1, lhs, expr, Inferred, ProveOption.ANY, Tactic.OR_LEFT);
			tree.add(n1);
			introduction(lhs, Inferred, depth + 1, ProveOption.ANY);
			
			Node n2 = new Node(depth + 1, rhs, expr, Inferred, ProveOption.ANY, Tactic.OR_RIGHT);
			tree.add(n2);
			introduction(rhs, Inferred, depth + 1, ProveOption.ANY);
			
		}else if(expr.getOperationName().equals("not")){
			OclExpression src = expr.getSource();
			BooleanExp bFalse = make.createBooleanExp();
			bFalse.setBooleanSymbol(false);
			
			HashMap<EObject, ContextNature> inferNextLv = new HashMap<EObject, ContextNature>(Inferred);
			inferNextLv.put(src, ContextNature.ASSUME);
			
			Node n1 = new Node(depth + 1, bFalse, expr, inferNextLv, op, Tactic.NEG_INTRO);
			tree.add(n1);
			introduction(bFalse, inferNextLv, depth + 1, op);
			
			
		}
		
		
		
		
	}
	
	

	public static void main(String[] args) throws Exception {
		ExecEnv env = Trace.moduleLoader(args[0], args[1], args[2], args[3], args[4], args[5]);
		EPackage tarmm = EMFLoader.loadEcore(args[3]);
		trace = Trace.getTrace(tarmm, env);

		List<OclExpression> postconditions = ContractLoader.init("HSM2FSM/Source/ContractSRC/HSM2FSMContract.atl");

		for (OclExpression post : postconditions) {
			HashMap<EObject, ContextNature> emptyTrace = new HashMap<EObject, ContextNature>();
			Node root = new Node(0, post, null, emptyTrace, null, null);
			tree.add(root);
			introduction(post, emptyTrace, 0, ProveOption.EACH);	//TODO, default prove option
		}

		// print tree
		Collections.sort(tree);
		for(Node n : tree){
			System.out.println(n.toString());
		}
		
		
		
	}
}
