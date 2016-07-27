package contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.m2m.atl.common.OCL.*;
import org.eclipse.m2m.atl.emftvm.ExecEnv;

import Ocl.Printer;
import Ocl.TypeInference;
import datastructure.ContextEntry;
import datastructure.ContextNature;
import datastructure.Node;
import datastructure.ProveOption;
import datastructure.Tactic;

public class Elimination {
	
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
	
	public static boolean terminated(ArrayList<Node> leafs){
		
		boolean rst = true;
		
		for(Node leaf : leafs){
			for(ContextEntry entry : leaf.getContext().values()){
				if(!entry.isEliminated()){
					return false;
				}
			}
		}
		
		return rst;
	}
	
	public static void elimin(Node n, EObject expr) {
		if (expr instanceof IteratorExp) {
			IteratorExp todo = (IteratorExp) expr;
			_elimin(n, todo);
		}else if(expr instanceof OperatorCallExp){
			OperatorCallExp todo = (OperatorCallExp) expr;
			_elimin(n, todo);
		}else if(expr instanceof OperationCallExp){
			OperationCallExp todo = (OperationCallExp) expr;
			_elimin(n, todo);
		}
	}
	
	static void _elimin(Node n, IteratorExp expr) {
		
	}
	
	static void _elimin(Node n, OperatorCallExp expr) {
		if(expr.getOperationName().equals("and")){
			
		}else if(expr.getOperationName().equals("or")){	
			OclExpression src = expr.getSource();
			EList<OclExpression> args = expr.getArguments();
			
			HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(n.getContext());
			inferNextLv.put(src, new ContextEntry(ContextNature.ASSUME));
			
			Node n1 = new Node(n.getLevel()+1, n.getContent(), n.getContent(), inferNextLv, n.getRel2Parent(), Tactic.OR_ELIM);
			tree.add(n1);
			
			for(OclExpression arg : args){
				HashMap<EObject, ContextEntry> inferNextLv2 = new HashMap<EObject, ContextEntry>(n.getContext());
				inferNextLv2.put(arg, new ContextEntry(ContextNature.ASSUME));
				
				Node nn = new Node(n.getLevel()+1, n.getContent(), n.getContent(), inferNextLv2, n.getRel2Parent(), Tactic.OR_ELIM);
				tree.add(nn);
			}
			
			
		}else if(expr.getOperationName().equals("implies")){
			
		}else if(expr.getOperationName().equals("not")){
			
		}
		
	}
	
	static void _elimin(Node n, OperationCallExp expr) {
		if(expr.getOperationName().equals("includes")){
			OclExpression col = expr.getSource();
			OclExpression src = expr.getArguments().get(0);
			
			String colType = TypeInference.infer(col, tarmm);
			String elemType = TypeInference.getElemType(colType);
			
			if(trace.get(elemType).size()>0){
				String first = trace.get(elemType).remove(0);
				
				OperationCallExp c1 = make.createOperationCallExp();
				c1.setOperationName("genBy");
				StringExp s = make.createStringExp();
				s.setStringSymbol(first);
				c1.setSource(EcoreUtil.copy(src));
				c1.getArguments().add(s);
				
				OperatorCallExp or = make.createOperatorCallExp();
				or.setOperationName("or");
				or.setSource(c1);
				
				for(String rule : trace.get(elemType)){
					OperationCallExp cn = make.createOperationCallExp();
					cn.setOperationName("genBy");
					StringExp sn = make.createStringExp();
					sn.setStringSymbol(rule);
					cn.setSource(EcoreUtil.copy(src));
					cn.getArguments().add(sn);
					
					or.getArguments().add(cn);
				}
				
				HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(n.getContext());
				inferNextLv.put(or, new ContextEntry(ContextNature.INFER));
				
				Node newNode = new Node(n.getLevel() + 1, n.getContent(), n.getContent(), inferNextLv, n.getRel2Parent(), Tactic.INCLUDES_ELIM_2);
				tree.add(newNode);
				
			}
		}if(expr.getOperationName().equals("excludes")){
			OclExpression col = expr.getSource();
			OclExpression src = expr.getArguments().get(0);
			
			String colType = TypeInference.infer(col, tarmm);
			String elemType = TypeInference.getElemType(colType);
			
			if(trace.get(elemType).size()>0){
				String first = trace.get(elemType).remove(0);
				
				OperationCallExp c1 = make.createOperationCallExp();
				c1.setOperationName("genBy");
				StringExp s = make.createStringExp();
				s.setStringSymbol(first);
				c1.setSource(EcoreUtil.copy(src));
				c1.getArguments().add(s);
				
				OperatorCallExp or = make.createOperatorCallExp();
				or.setOperationName("or");
				or.setSource(c1);
				
				for(String rule : trace.get(elemType)){
					OperationCallExp cn = make.createOperationCallExp();
					cn.setOperationName("genBy");
					StringExp sn = make.createStringExp();
					sn.setStringSymbol(rule);
					cn.setSource(EcoreUtil.copy(src));
					cn.getArguments().add(sn);
					
					or.getArguments().add(cn);
				}
				
				OperatorCallExp not = make.createOperatorCallExp();
				not.setOperationName("not");
				not.setSource(or);
				
				HashMap<EObject, ContextEntry> inferNextLv = new HashMap<EObject, ContextEntry>(n.getContext());
				inferNextLv.put(not, new ContextEntry(ContextNature.INFER));
				
				Node newNode = new Node(n.getLevel() + 1, n.getContent(), n.getContent(), inferNextLv, n.getRel2Parent(), Tactic.EXCLUDES_ELIM_2);
				tree.add(newNode);
				
			}
		}
		
		
		
	}
}
