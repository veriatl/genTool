package contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.m2m.atl.common.OCL.*;
import org.eclipse.m2m.atl.emftvm.ExecEnv;

import Ocl.OclHelper;
import Ocl.Printer;
import Ocl.TypeInference;
import datastructure.ContextEntry;
import datastructure.ContextHelper;
import datastructure.ContextNature;
import datastructure.Node;
import datastructure.ProveOption;
import datastructure.Tactic;
import keywords.Keyword;
import metamodel.EMFCopier;
import metamodel.EMFHelper;

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
			OclExpression src = expr.getSource();
			EList<OclExpression> args = expr.getArguments();
			
			HashMap<EObject, ContextEntry> inferNextLv =  ContextHelper.cloneContext(n.getContext());
			inferNextLv.put(src, new ContextEntry(ContextNature.INFER));		
			for(OclExpression arg : args){
				inferNextLv.put(arg, new ContextEntry(ContextNature.INFER));
			}
			
			Node n1 = new Node(n.getLevel()+1, n.getContent(), n, inferNextLv, n.getRel2Parent(), Tactic.AND_ELIM);
			tree.add(n1);
			
		}else if(expr.getOperationName().equals("or")){	
			OclExpression src = expr.getSource();
			EList<OclExpression> args = expr.getArguments();
			
			HashMap<EObject, ContextEntry> inferNextLv =ContextHelper.cloneContext(n.getContext());
			inferNextLv.put(src, new ContextEntry(ContextNature.ASSUME));
			
			Node n1 = new Node(n.getLevel()+1, n.getContent(), n, inferNextLv, ProveOption.EACH, Tactic.OR_ELIM);
			tree.add(n1);
			
			for(OclExpression arg : args){
				HashMap<EObject, ContextEntry> inferNextLv2 = ContextHelper.cloneContext(n.getContext());
				inferNextLv2.put(arg, new ContextEntry(ContextNature.ASSUME));
				
				Node nn = new Node(n.getLevel()+1, n.getContent(), n, inferNextLv2, ProveOption.EACH, Tactic.OR_ELIM);
				tree.add(nn);
			}
			
			
		}else if(expr.getOperationName().equals("implies")){
			OclExpression lhs = expr.getSource();
			OclExpression rhs = expr.getArguments().get(0);
			
			for(EObject entry : n.getContext().keySet()){
				if(OclHelper.Equal(lhs, entry)){
					HashMap<EObject, ContextEntry> inferNextLv2 =  ContextHelper.cloneContext(n.getContext());
					inferNextLv2.put(rhs, new ContextEntry(ContextNature.INFER));
					
					Node nn = new Node(n.getLevel()+1, n.getContent(), n, inferNextLv2, n.getRel2Parent(), Tactic.IMPLY_ELIM);
					tree.add(nn);
				}
			}
			
		}else if(expr.getOperationName().equals("not")){
			OclExpression src = expr.getSource();
			
			for(EObject entry : n.getContext().keySet()){
				if(OclHelper.Equal(src, entry)){
					HashMap<EObject, ContextEntry> inferNextLv2 =  ContextHelper.cloneContext(n.getContext());
					BooleanExp falseExpr = make.createBooleanExp();
					falseExpr.setBooleanSymbol(false);
					inferNextLv2.put(falseExpr, new ContextEntry(ContextNature.INFER));
					
					Node nn = new Node(n.getLevel()+1, n.getContent(), n, inferNextLv2, n.getRel2Parent(), Tactic.NEG_ELIM);
					tree.add(nn);
				}
			}
		}
		
	}
	
	static void _elimin(Node n, OperationCallExp expr) {
		if(expr.getOperationName().equals("includes")){
			OclExpression col = expr.getSource();
			OclExpression src = expr.getArguments().get(0);
			
			String colType = TypeInference.infer(col, tarmm);
			String elemType = TypeInference.getElemType(colType);
			
			
			
			if(trace.get(elemType)!=null && trace.get(elemType).size()>0){
				
				String first = trace.get(elemType).get(0);
				
				OperationCallExp c1 = make.createOperationCallExp();
				c1.setOperationName("genBy");
				StringExp s = make.createStringExp();
				s.setStringSymbol(first);
				c1.setSource(EMFCopier.deepCopy(src));
				c1.getArguments().add(s);
				
				OperatorCallExp or = make.createOperatorCallExp();
				or.setOperationName("or");
				or.setSource(c1);
				
				List<String> subTrace  = trace.get(elemType).subList(1, trace.get(elemType).size());
				
				for(String rule : subTrace ){
					OperationCallExp cn = make.createOperationCallExp();
					cn.setOperationName("genBy");
					StringExp sn = make.createStringExp();
					sn.setStringSymbol(rule);
					cn.setSource(EMFCopier.deepCopy(src));
					cn.getArguments().add(sn);
					
					or.getArguments().add(cn);
				}
				
				HashMap<EObject, ContextEntry> inferNextLv =  ContextHelper.cloneContext(n.getContext());
				inferNextLv.put(or, new ContextEntry(ContextNature.INFER));
				
				Node newNode = new Node(n.getLevel() + 1, n.getContent(), n, inferNextLv, n.getRel2Parent(), Tactic.INCLUDES_ELIM_2);
				tree.add(newNode);
				
			}
		}else if(expr.getOperationName().equals("excludes")){
			OclExpression col = expr.getSource();
			OclExpression src = expr.getArguments().get(0);
			
			String colType = TypeInference.infer(col, tarmm);
			String elemType = TypeInference.getElemType(colType);
			
			if(trace.get(elemType)!=null && trace.get(elemType).size()>0){
				String first = trace.get(elemType).get(0);
				
				OperationCallExp c1 = make.createOperationCallExp();
				c1.setOperationName("genBy");
				StringExp s = make.createStringExp();
				s.setStringSymbol(first);
				c1.setSource(EMFCopier.deepCopy(src));
				c1.getArguments().add(s);
				
				OperatorCallExp or = make.createOperatorCallExp();
				or.setOperationName("or");
				or.setSource(c1);
				
				List<String> subTrace  = trace.get(elemType).subList(1, trace.get(elemType).size());
				
				for(String rule : subTrace){
					OperationCallExp cn = make.createOperationCallExp();
					cn.setOperationName("genBy");
					StringExp sn = make.createStringExp();
					sn.setStringSymbol(rule);
					cn.setSource(EMFCopier.deepCopy(src));
					cn.getArguments().add(sn);
					
					or.getArguments().add(cn);
				}
				
				OperatorCallExp not = make.createOperatorCallExp();
				not.setOperationName("not");
				not.setSource(or);
				
				HashMap<EObject, ContextEntry> inferNextLv = ContextHelper.cloneContext(n.getContext());
				inferNextLv.put(not, new ContextEntry(ContextNature.INFER));
				
				Node newNode = new Node(n.getLevel() + 1, n.getContent(), n, inferNextLv, n.getRel2Parent(), Tactic.EXCLUDES_ELIM_2);
				tree.add(newNode);
				
			}
		}else if(expr.getOperationName().equals("oclIsUndefined")){	//TODO there probably more operation can be applied this elimin rule.
			OclExpression src = expr.getSource();
			if(src instanceof NavigationOrAttributeCallExp){
				// identify single valued navigation
				String tp = TypeInference.infer(src, tarmm);

				
				
				if(!tp.startsWith(Keyword.TYPE_COL) && !TypeInference.isPrimitive(tp)){
					OperationCallExp col = make.createCollectionOperationCallExp();
					col.setOperationName("allInstances");
					OclModelElement m = make.createOclModelElement();
					
					String mmName = EMFHelper.getModel(tp);
					String clName = EMFHelper.getClassifier(tp);
					m.setName(clName);
					OclModel model = make.createOclModel();
					model.setName(mmName);
					m.setModel(model);
					col.setSource(m);
					
					OperationCallExp includes = make.createOperationCallExp();
					includes.setOperationName("includes");
					includes.setSource(EMFCopier.deepCopy(col));
					includes.getArguments().add(EMFCopier.deepCopy(src));
					
					OperationCallExp excludes = make.createOperationCallExp();
					excludes.setOperationName("excludes");
					excludes.setSource(EMFCopier.deepCopy(col));
					excludes.getArguments().add(EMFCopier.deepCopy(src));
					
					
					if(!OclHelper.isMember(n.getContext().keySet(), includes) && !OclHelper.isMember(n.getContext().keySet(), excludes)){
						HashMap<EObject, ContextEntry> inferNextLv =  ContextHelper.cloneContext(n.getContext());
						inferNextLv.put(includes, new ContextEntry(ContextNature.ASSUME));
						Node n1 = new Node(n.getLevel() + 1, n.getContent(), n, inferNextLv, ProveOption.EACH, Tactic.NAV_SINGLE_INTRO);
						tree.add(n1);
						
						HashMap<EObject, ContextEntry> inferNextLv2 =  ContextHelper.cloneContext(n.getContext());
						inferNextLv2.put(excludes, new ContextEntry(ContextNature.ASSUME));
						Node n2 = new Node(n.getLevel() + 1, n.getContent(), n, inferNextLv2, ProveOption.EACH, Tactic.NAV_SINGLE_INTRO);
						tree.add(n2);	
					}
						
				
				}
			}	
		}
		
		
		
	}
}
