package incremental;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.m2m.atl.common.ATL.MatchedRule;
import org.eclipse.m2m.atl.common.ATL.Rule;
import org.eclipse.m2m.atl.common.OCL.OclExpression;
import org.eclipse.m2m.atl.emftvm.ExecEnv;

import Ocl.Ocl2Boogie;
import contract.ContractLoader;
import contract.Elimination;
import contract.Introduction;
import datastructure.ContextEntry;
import datastructure.Node;
import datastructure.NodeHelper;
import datastructure.ProveOption;
import metamodel.EMFLoader;
import transformation.GenBy;
import transformation.Trace;
import transformation.TransformationLoader;

public class Driver {


	
	
	
	public static void main(String[] args) throws Exception {
		PrintStream original = new PrintStream(System.out);
		
		Map<String, List<IdNode>> src = load(genConf("HSM2FSM"));
		
		Map<String, List<IdNode>> AF2 = load(genConf("AF2"));
		Map<String, List<IdNode>> AR = load(genConf("AR"));
		Map<String, List<IdNode>> DB3 = load(genConf("DB3"));
		Map<String, List<IdNode>> DR1 = load(genConf("DR1"));
		Map<String, List<IdNode>> MB6 = load(genConf("MB6"));
		Map<String, List<IdNode>> MF6 = load(genConf("MF6"));
		Map<String, List<IdNode>> MT2 = load(genConf("MT2"));
		
		List<Map<String, List<IdNode>>> mutations = new ArrayList<Map<String, List<IdNode>>>();
		mutations.add(AF2);
		mutations.add(AR);
		mutations.add(DB3);
		mutations.add(DR1);
		mutations.add(MB6);
		mutations.add(MF6);
		mutations.add(MT2);
		
		Map<Map<String, List<IdNode>>, String> mutator = new HashMap<Map<String, List<IdNode>>, String>();
		mutator.put(AF2, "RS2RS");
		mutator.put(AR, "CS2RS");
		mutator.put(DB3, "IS2IS");
		mutator.put(DR1, "SM2SM");
		mutator.put(MB6, "T2TB");
		mutator.put(MF6, "T2TB");
		mutator.put(MT2, "RS2RS");
		
		Map<Map<String, List<IdNode>>, String> mutatorId = new HashMap<Map<String, List<IdNode>>, String>();
		mutatorId.put(AF2, "AF2");
		mutatorId.put(AR, "AR");
		mutatorId.put(DB3, "DB3");
		mutatorId.put(DR1, "DR1");
		mutatorId.put(MB6, "MB6");
		mutatorId.put(MF6, "MF6");
		mutatorId.put(MT2, "MT2");
		
		for(Map<String, List<IdNode>> mutant : mutations){
			
			int total = 0;
			int succ = 0;
			
			
			for (String goal : mutant.keySet()) {
				for (IdNode n : mutant.get(goal)) {				
					if(!n.getNode().getInvolvedRuls().contains(mutator.get(mutant))){
						IdNode srcNode = find(n, src.get(goal));
						if (srcNode != null) {
							
							
							
//							String fileName = String.format("RES/%s.txt", mutatorId.get(mutant));
//							PrintStream out =  new PrintStream(new FileOutputStream(fileName, true));
//							System.setOut(out);
//							System.out.printf("/Sub-goals/%s%s.bpl\n", goal, srcNode.id);
//							
//							
//							
//							fileName = String.format("RES/mutated_%s.txt", mutatorId.get(mutant));
//							out =  new PrintStream(new FileOutputStream(fileName, true));
//							System.setOut(out);
//							System.out.printf("/Sub-goals/%s%s.bpl\n", goal, n.id);
							
							// goal/new : goal/cached
							String fileName = String.format("RES/link_%s.txt", mutatorId.get(mutant));
							PrintStream out =  new PrintStream(new FileOutputStream(fileName, true));
							System.setOut(out);
							System.out.printf("/Sub-goals/%s%s.bpl:/Sub-goals/%s%s.bpl\n", goal, n.id, goal, srcNode.id);
							
							
							succ++;
							
							
						}
					}
			
					total++;
				}
			}

			//original.println(String.format("%s:%s: %s", mutatorId.get(mutant), total, succ));
			//original.println(String.format("=========="));
		}
		
		
	
	}

	private static boolean compareExpressionLists(List<EObject> l1, List<EObject> l2) {

		List<String> s1 = new ArrayList<String>();
		List<String> s2 = new ArrayList<String>();

		for (EObject o1 : l1) {
			s1.add(Ocl2Boogie.print(o1));
		}

		for (EObject o2 : l2) {
			s2.add(Ocl2Boogie.print(o2));
		}

		return s1.containsAll(s2) && s2.containsAll(s1);
	}

	private static IdNode find(IdNode n, List<IdNode> nodes) {

		for (IdNode curr : nodes) {
			boolean rule = n.getNode().getInvolvedRuls().containsAll(curr.getNode().getInvolvedRuls())
					&& curr.getNode().getInvolvedRuls().containsAll(n.getNode().getInvolvedRuls());
			boolean conclusion = Ocl2Boogie.print(n.getNode().getContent())
					.equals(Ocl2Boogie.print(curr.getNode().getContent()));
			boolean assumption = compareExpressionLists(n.getNode().getAssumptions(), curr.getNode().getAssumptions());

			boolean infer = compareExpressionLists(n.getNode().getInfers(), curr.getNode().getInfers());

			if (rule && conclusion && assumption && infer) {
				return curr;
			}

		}

		return null;
	}

	public static Map<String, List<IdNode>> load(String[] args) throws Exception {
		ExecEnv env = Trace.moduleLoader(args[0], args[1], args[2], args[3], args[4], args[5]);
		EPackage tarmm = EMFLoader.loadEcore(args[3]);
		EPackage srcmm = EMFLoader.loadEcore(args[2]);
		Map<String, ArrayList<String>> trace = Trace.getTrace(tarmm, env);

		String contractPath = args[6];
		String transformationSrcPath = args[7];
		String subGoalsPath = args[8];
		String genByPath = args[9];
		
		List<OclExpression> postconditions = ContractLoader.init(contractPath);
		List<MatchedRule> rules = TransformationLoader.init(transformationSrcPath);

		Map<String, List<IdNode>> res = new HashMap<String, List<IdNode>>();

		for (OclExpression post : postconditions) {

			ArrayList<Node> tree = new ArrayList<Node>();
			Introduction.init(env, trace, tree, tarmm);

			HashMap<EObject, ContextEntry> emptyTrace = new HashMap<EObject, ContextEntry>();
			Node root = new Node(0, post, null, emptyTrace, null, null);
			tree.add(root);

			// intro
			ArrayList<Node> oldLeafs;
			ArrayList<Node> newLeafs;

			do {
				oldLeafs = NodeHelper.findLeafs(tree);

				for (Node n : oldLeafs) {
					// TODO, default prove option
					Introduction.introduction(n, n.getContent(), n.getContext(), n.getLevel(), ProveOption.EACH);
				}

				newLeafs = NodeHelper.findLeafs(tree);
			} while (!oldLeafs.containsAll(newLeafs));

			// elimin
			Elimination.init(env, trace, tree, tarmm);
			while (!Elimination.terminated(NodeHelper.findLeafs(tree))) {
				ArrayList<Node> leafs = NodeHelper.findLeafs(tree);

				for (Node n : leafs) {
					HashMap<EObject, ContextEntry> ctx = n.getContext();
					for (EObject ocl : ctx.keySet()) {
						if (ctx.get(ocl).isEliminated()) {
							continue;
						} else {
							ctx.get(ocl).setEliminated(true);
							Elimination.elimin(n, ocl);
							break;
						}
					}
				}

			}

			// print tree test
			Collections.sort(tree);
			Ocl2Boogie.init(tarmm);
			PrintStream out;

			String goalName = post.getCommentsBefore().get(0).replace("--", "")+"/";
			String folderName = String.format("%s%s", subGoalsPath,goalName);
			File file = new File(folderName); 
			FileUtils.forceMkdir(file);
			
			int i = 0;
			for(Node n : NodeHelper.findLeafs(tree)){
				//gen sub-goals
				String fileName = String.format("%scase%02d.bpl", folderName,i);
				out =  new PrintStream(new FileOutputStream(fileName));
				System.setOut(out);
				System.out.println(n.toBoogie(env));
			
				
				//create cache
				String id = String.format("case%02d", i);
				IdNode currNode = new IdNode(id, n);

				if (res.get(goalName) == null) {
					List<IdNode> nodes = new ArrayList<IdNode>();
					nodes.add(currNode);
					res.put(goalName, nodes);
				} else {
					res.get(goalName).add(currNode);
				}
				
				i++;
			}
						
			printDriver(env, post, folderName);

		}

		GenBy.init(rules,srcmm);
		GenBy.print(genByPath);
		
		return res;
	}
	
	private static String[] genConf(String proj){
		List<String> args = new ArrayList<String>();
		args.add(String.format("%s/Source/EMFTVM/", proj));
		args.add(String.format("HSM2FSM"));
		args.add(String.format("%s/Source/SRCMM/HSM.ecore", proj));
		args.add(String.format("%s/Source/TARMM/FSM.ecore", proj));
		args.add(String.format("HSM"));
		args.add(String.format("FSM"));
		args.add(String.format("%s/Source/ContractSRC/HSM2FSMContract.atl", proj));
		args.add(String.format("%s/Source/ATLSRC/HSM2FSM.atl", proj));
		args.add(String.format("%s/Sub-goals/", proj));
		args.add(String.format("%s/Sub-goals/", proj));
		return args.toArray(new String[0]);
	}

	
	private static void printDriver(ExecEnv env, OclExpression post, String folderName) throws Exception {
		String fileName = String.format("%soriginal.bpl", folderName);
		PrintStream out =  new PrintStream(new FileOutputStream(fileName));
		System.setOut(out);
		
		printDriverHeader();
		for(org.eclipse.m2m.atl.emftvm.Rule r : env.getRules()){
			System.out.println(String.format("call %s_matchAll();", r.getName()));
		}
		for(org.eclipse.m2m.atl.emftvm.Rule r : env.getRules()){
			System.out.println(String.format("call %s_applyAll();", r.getName()));
		}
		System.out.println();
		printPost(post);
		printDriverFooter();
	}



	private static void printPost(OclExpression post) {
		System.out.println(String.format("assert %s;", Ocl2Boogie.print(post)));
		
	}

	private static void printDriverHeader() {
		System.out.println("implementation driver(){");
		System.out.println("call init_tar_model(); ");
		
	}
	
	private static void printDriverFooter() {
		System.out.println("}");
	}
}
