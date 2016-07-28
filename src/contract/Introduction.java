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


	public static void introduction(OclExpression expr, HashMap<EObject, ContextEntry> Inferred, int depth, ProveOption op) {
		
		if (expr instanceof IteratorExp) {
			IteratorExp todo = (IteratorExp) expr;
			_introduction(todo, Inferred, depth, op);
		}else if(expr instanceof OperatorCallExp){
			OperatorCallExp todo = (OperatorCallExp) expr;
			_introduction(todo, Inferred, depth, op);
		}else if(expr instanceof NavigationOrAttributeCallExp){
			NavigationOrAttributeCallExp todo = (NavigationOrAttributeCallExp) expr;
			_introduction(todo, Inferred, depth, op);
		}
		
		
	}

	static void _introduction(IteratorExp expr, HashMap<EObject, ContextEntry> Inferred, int depth, ProveOption op) {

		Iterator bv = expr.getIterators().get(0);
		OclExpression loopBody = expr.getBody();
		OclExpression loopSrc = expr.getSource();
		


		
		if (expr.getName().toLowerCase().equals("forall")) {		
			HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(Inferred);
			inferNextLv.put(bv, new ContextEntry(ContextNature.BV));

			// bv in src
			OperationCallExp inclusion = make.createOperationCallExp();
			inclusion.setOperationName("includes");
			
			EcoreUtil.Copier cpy = new EcoreUtil.Copier();
			EObject tempSrc = cpy.copy(loopSrc);
			cpy.copyReferences();
			if(tempSrc instanceof OclExpression){
				inclusion.setSource((OclExpression)tempSrc);
			}
			
			VariableExp var = make.createVariableExp();
			var.setReferredVariable(EMFHelper.deepCopy(bv));
			inclusion.getArguments().add(var);
			inferNextLv.put(inclusion, new ContextEntry(ContextNature.ASSUME));
			
			Node n = new Node(depth + 1, loopBody, expr, inferNextLv, ProveOption.EACH, Tactic.FORALL_INTRO);
			tree.add(n);
			
			introduction(loopBody, inferNextLv, depth + 1, ProveOption.EACH);
			

		}else if (expr.getName().toLowerCase().equals("exists")) {	

		}	

		
		
	}
	
	static void _introduction(NavigationOrAttributeCallExp expr, HashMap<EObject, ContextEntry> Inferred, int depth, ProveOption op){	
		// identify single valued navigation
		String tp = TypeInference.infer(expr,tarmm);
		
		if(!tp.startsWith(Keyword.TYPE_COL) && !TypeInference.isPrimitive(tp)){
			
		}
		
	}
	
	static void _introduction(OperatorCallExp expr, HashMap<EObject, ContextEntry> Inferred, int depth, ProveOption op){
		
		if(expr.getOperationName().equals("implies")){
			
			OclExpression rhs = expr.getArguments().get(0);
			
			HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(Inferred);
			inferNextLv.put(expr.getSource(), new ContextEntry(ContextNature.ASSUME));
			
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
			
			HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(Inferred);
			inferNextLv.put(src, new ContextEntry(ContextNature.ASSUME));
			
			Node n1 = new Node(depth + 1, bFalse, expr, inferNextLv, op, Tactic.NEG_INTRO);
			tree.add(n1);
			introduction(bFalse, inferNextLv, depth + 1, op);
			
			
		}
		
		
		
		
	}
	
	

	
}
