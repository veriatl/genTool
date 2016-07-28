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
import datastructure.ContextEntry;
import datastructure.ContextNature;
import datastructure.Node;
import datastructure.NodeHelper;
import datastructure.ProveOption;
import datastructure.Tactic;
import keywords.Keyword;
import metamodel.EMFHelper;
import metamodel.EMFLoader;
import transformation.Trace;


//TODO
// Control bv, forall a2 implies exists a2




public class Introduction  {

	static ExecEnv env;
	static Map<String, ArrayList<String>> trace;
	static ArrayList<Node> tree = new ArrayList<Node>();
	static EPackage tarmm;
	static private OCLFactory make = OCLFactory.init();
	

	public static void init(ExecEnv e, Map<String, ArrayList<String>> t, ArrayList<Node> tr, EPackage mm){
		env = e;
		trace = t;
		tree =tr;
		tarmm = mm;
	}


	public static void introduction(Node n, OclExpression expr, HashMap<EObject, ContextEntry> Inferred, int depth, ProveOption op) {
		
		if (expr instanceof IteratorExp) {
			IteratorExp todo = (IteratorExp) expr;
			_introduction(n, todo, Inferred, depth, op);
		}else if(expr instanceof OperatorCallExp){
			OperatorCallExp todo = (OperatorCallExp) expr;
			_introduction(n, todo, Inferred, depth, op);
		}else if(expr instanceof NavigationOrAttributeCallExp){
			NavigationOrAttributeCallExp todo = (NavigationOrAttributeCallExp) expr;
			_introduction(n, todo, Inferred, depth, op);
		}
		
		
	}

	static void _introduction(Node curr, IteratorExp expr, HashMap<EObject, ContextEntry> Inferred, int depth, ProveOption op) {

		Iterator bv = expr.getIterators().get(0);
		OclExpression loopBody = expr.getBody();
		OclExpression loopSrc = expr.getSource();
		


		
		if (expr.getName().toLowerCase().equals("forall")) {		
			HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(Inferred);
			inferNextLv.put(bv, new ContextEntry(ContextNature.BV));

			// bv in src
			OperationCallExp inclusion = make.createOperationCallExp();
			inclusion.setOperationName("includes");
			
			inclusion.setSource(EMFHelper.deepCopy(loopSrc));
			
			VariableExp var = make.createVariableExp();
			var.setReferredVariable(EMFHelper.deepCopy(bv));
			inclusion.getArguments().add(var);
			inferNextLv.put(inclusion, new ContextEntry(ContextNature.ASSUME));
			
			Node n = new Node(depth + 1, loopBody, curr, inferNextLv, ProveOption.EACH, Tactic.FORALL_INTRO);
			tree.add(n);
			

		}else if (expr.getName().toLowerCase().equals("exists")) {	

		}	

		
		
	}
	
	static void _introduction(Node n, NavigationOrAttributeCallExp expr, HashMap<EObject, ContextEntry> Inferred, int depth, ProveOption op){	
		// identify single valued navigation
		String tp = TypeInference.infer(expr,tarmm);
		
		if(!tp.startsWith(Keyword.TYPE_COL) && !TypeInference.isPrimitive(tp)){
			
		}
		
	}
	
	static void _introduction(Node curr, OperatorCallExp expr, HashMap<EObject, ContextEntry> Inferred, int depth, ProveOption op){
		
		if(expr.getOperationName().equals("implies")){
			
			OclExpression rhs = expr.getArguments().get(0);
			
			HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(Inferred);
			inferNextLv.put(expr.getSource(), new ContextEntry(ContextNature.ASSUME));
			
			Node n = new Node(depth + 1, rhs, curr, inferNextLv, op, Tactic.IMPLY_INTRO);
			tree.add(n);
			
			
		}else if(expr.getOperationName().equals("and")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			Node n1 = new Node(depth + 1, lhs, curr, Inferred, ProveOption.EACH, Tactic.SPLIT);
			tree.add(n1);
			
			
			Node n2 = new Node(depth + 1, rhs, curr, Inferred, ProveOption.EACH, Tactic.SPLIT);
			tree.add(n2);
			
				
		}else if(expr.getOperationName().equals("or")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			Node n1 = new Node(depth + 1, lhs, curr, Inferred, ProveOption.ANY, Tactic.OR_LEFT);
			tree.add(n1);
			
			
			Node n2 = new Node(depth + 1, rhs, curr, Inferred, ProveOption.ANY, Tactic.OR_RIGHT);
			tree.add(n2);
			
			
		}else if(expr.getOperationName().equals("not")){
			OclExpression src = expr.getSource();
			BooleanExp bFalse = make.createBooleanExp();
			bFalse.setBooleanSymbol(false);
			
			HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(Inferred);
			inferNextLv.put(src, new ContextEntry(ContextNature.ASSUME));
			
			Node n1 = new Node(depth + 1, bFalse, curr, inferNextLv, op, Tactic.NEG_INTRO);
			tree.add(n1);
			
			
			
		}
		
		
		
		
	}
	
	

	
}
