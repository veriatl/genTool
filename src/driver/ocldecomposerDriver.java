package driver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.m2m.atl.common.OCL.OclExpression;
import org.eclipse.m2m.atl.emftvm.ExecEnv;

import contract.ContractLoader;
import contract.Elimination;
import contract.Introduction;
import datastructure.ContextEntry;
import datastructure.Node;
import datastructure.NodeHelper;
import datastructure.ProveOption;
import metamodel.EMFLoader;
import transformation.Trace;

public class ocldecomposerDriver {
	public static void main(String[] args) throws Exception {
		ExecEnv env = Trace.moduleLoader(args[0], args[1], args[2], args[3], args[4], args[5]);
		EPackage tarmm = EMFLoader.loadEcore(args[3]);
		Map<String, ArrayList<String>> trace = Trace.getTrace(tarmm, env);
		ArrayList<Node> tree = new ArrayList<Node>();
		
		Introduction.init(env, trace, tree, tarmm);
		List<OclExpression> postconditions = ContractLoader.init("HSM2FSM/Source/ContractSRC/HSM2FSMContract.atl");
		for (OclExpression post : postconditions) {
			HashMap<EObject, ContextEntry> emptyTrace = new HashMap<EObject, ContextEntry>();
			Node root = new Node(0, post, null, emptyTrace, null, null);
			tree.add(root);
			Introduction.introduction(post, emptyTrace, 0, ProveOption.EACH);	//TODO, default prove option
		}
		
		Elimination.init(env, trace, tree, tarmm);
		while(!Elimination.terminated(NodeHelper.findLeafs(tree))){
			ArrayList<Node> leafs = NodeHelper.findLeafs(tree);
			
			for(Node n : leafs){
				HashMap<EObject, ContextEntry> ctx = n.getContext();
				for(EObject ocl : ctx.keySet()){
					if(ctx.get(ocl).isEliminated()){
						continue;
					}else{
						Elimination.elimin(n, ocl);
						ctx.get(ocl).setEliminated(true);
						break;
					}
				}
			}
		}
		
		
		// print tree
		Collections.sort(tree);
		for(Node n : NodeHelper.findLeafs(tree)){
			System.out.println(n.toString());
			System.out.println("===");
		}
		
	}
}
