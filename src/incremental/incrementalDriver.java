package incremental;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.m2m.atl.common.ATL.MatchedRule;
import org.eclipse.m2m.atl.common.OCL.OclExpression;
import org.eclipse.m2m.atl.emftvm.ExecEnv;

import Ocl.Ocl2Boogie;
import Ocl.TypeInference;
import contract.ContractLoader;
import contract.Elimination;
import contract.Introduction;

import datastructure.ContextEntry;
import datastructure.IncrementalResult;
import datastructure.Node;
import datastructure.NodeHelper;
import datastructure.ProveOption;
import datastructure.TriBoolean;
import metamodel.EMFLoader;
import runtime.VerificationResult;
import runtime.executioner;
import transformation.GenBy;
import transformation.Trace;
import transformation.TransformationLoader;

public class incrementalDriver {
	
	public static void main(String[] args) throws Exception {
		//inc_verify_sub("HSM2FSM", "TEST", "RS2RS");
		inc_verify_post_cache("HSM2FSM", "TEST", "RS2RS");
	}
	
	
	public static void inc_verify_sub(String srcProj, String tarProj, String opRule) throws Exception{

		
		IncrementalResult src = load(genConf(srcProj));
		IncrementalResult tar = load(genConf(tarProj));
		
		
		executioner.init(tarProj);
		for(String post : tar.getLeafs4Posts().keySet()){
			
			VerificationResult postV = executioner.verify(post,"original");
			if(postV.getResult().equals("true")){
				System.out.println(postV);
			}else{
				for (IdNode subgoal : tar.getLeafs4Posts().get(post)) {			
					if(findInCache(subgoal, src.getLeafs4Posts().get(post))!=null && !subgoal.getNode().getInvolvedRuls().contains(opRule)){
						String id = String.format("%s-%s-%s", tarProj,post,subgoal.getId());		
						System.out.println(new VerificationResult(id, "Cached", 0));
					}else{
						System.out.println(executioner.verify(post, subgoal.getId()));
					}
				}
			}
		}

	}
	
	
	public static void inc_verify_post_cache(String srcProj, String tarProj, String opRule) throws Exception{

		
		IncrementalResult src = load(genConf(srcProj));
		IncrementalResult tar = load(genConf(tarProj));

		
		executioner.init(tarProj);
		
		for(String post : tar.getLeafs4Posts().keySet()){
			Set<String> r1 = src.getRules4Posts().get(post);
			Set<String> r2 = tar.getRules4Posts().get(post);
			if(r1.containsAll(r2) && r2.contains(r1) && !r2.contains(opRule)){
				//cached
			}else{
				
				// get leaf res
				for (IdNode subgoal : tar.getLeafs4Posts().get(post)) {			
					if(findInCache(subgoal, src.getLeafs4Posts().get(post))!=null && !subgoal.getNode().getInvolvedRuls().contains(opRule)){
						VerificationResult r = executioner.verify(post, subgoal.getId());
						String v = r.getResult();
						if(v.equals("true")){
							subgoal.getNode().setResult(TriBoolean.TRUE);
						}else if(v.equals("false")){
							subgoal.getNode().setResult(TriBoolean.FALSE);
						}
						
					}else{
						subgoal.getNode().setResult(TriBoolean.UNKNOWN);
					}
				}
				
				// populate tree
				ArrayList<Node> tarTree = tar.getTrees4Posts().get(post);
				ArrayList<Node> tarResultTree = new ArrayList<Node>();
				
				for(Node leaf : NodeHelper.findLeafs(tarTree)){
					tarResultTree.add(leaf);
					tarTree.remove(leaf);
				}
				
				while(tarTree.size()!=0){
					for(Node leaf : NodeHelper.findLeafs(tarTree)){
						ArrayList<Node> childrenOfLeaf = NodeHelper.findChildren(leaf, tarResultTree);
						leaf.setResult(TriBoolean.compute(childrenOfLeaf));
						tarResultTree.add(leaf);
						tarTree.remove(leaf);
					}	
				}
				
				// find node
				Node simPost = NodeHelper.findSimplifiedPost(tarResultTree);
				//verify node
				String simPostBpl = constructTask(tarProj, post, simPost, tar.getRules4Posts().get(post), tar.getEnv(), tar.getTarmm(), tar.getInfers4Posts());
				System.out.println();
				
				// update result and repopulate verification result tree
				
				VerificationResult simPostV = executioner.verify(post, simPostBpl);
				simPost.setResult(simPostV.getTriBooleanResult());		
				System.out.println(NodeHelper.repopulate(simPost, tarResultTree));

			}
		}
		


	}
	
	
	
	
	
//	public static void inc_verify_post_nocache(String srcProj, String tarProj, String opRule) throws Exception{
//
//		
//		Map<String, List<IdNode>> src = load(genConf(srcProj));
//		Map<String, List<IdNode>> tar = load(genConf(tarProj));
//
//		
//		executioner.init(tarProj);
//		for(String post : tar.keySet()){
//			
//			VerificationResult postV = executioner.verify(post,"original");
//			if(postV.getResult().equals("true")){
//				System.out.println(postV);
//			}else{
//				for (IdNode subgoal : tar.get(post)) {			
//					if(find(subgoal, src.get(post))!=null && !subgoal.getNode().getInvolvedRuls().contains(opRule)){
//						String id = String.format("%s-%s-%s", tarProj,post,subgoal.getId());		
//						System.out.println(new VerificationResult(id, "Cached", 0));
//					}else{
//						System.out.println(executioner.verify(post, subgoal.getId()));
//					}
//				}
//			}
//		}
//
//	}
	
	
	private static String constructTask(String tarProj, String post, Node simPost, Set<String> rules, ExecEnv env, EPackage tarmm, Map<String, Map<String, String>> infers) throws Exception {
		PrintStream original = new PrintStream(System.out);
		String folderName = String.format("%s/Sub-goals/%s", tarProj, post);
		String fileName = String.format("%ssimplified.bpl", folderName);
		PrintStream out =  new PrintStream(new FileOutputStream(fileName));
		System.setOut(out);
		Ocl2Boogie.init(tarmm);
		
		
		
		System.out.println("implementation driver(){\n");
		
		
		for(EObject r : simPost.getBVs()){
			System.out.println(String.format("var %s: ref;\n", Ocl2Boogie.print(r)));
			TypeInference.lookup.putAll(infers.get(post));
			
		}
		
		System.out.println("call init_tar_model();\n");
		
		ArrayList<String> order = Trace.ruleOrdered(env);
		ArrayList<String> list = new ArrayList<String>();
		
		
		for(String r : order){
			if(rules.contains(r)){
				list.add(r);
			}	
		}
		
		for(String r : list){
			System.out.println(String.format("call %s_matchAll();", r));
		}
		for(String r : list){
			System.out.println(String.format("call %s_applyAll();", r));
		}
		
		System.out.println();
		
		for(EObject entry : simPost.getAssumptions()){
			System.out.println(String.format("assume %s;\n",  Ocl2Boogie.print(entry)));

		}
		
		for(EObject entry : simPost.getInfers()){
			System.out.println(String.format("assume %s;\n",  Ocl2Boogie.print(entry)));

		}
		
		
		printPost(simPost.getContent());
		printDriverFooter();
		out.close();
		
		System.setOut(original);
		
		return "simplified";
	}



	//TODO this only suitable for our case study, make it more customized
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

	private static IdNode findInCache(IdNode n, List<IdNode> nodes) {

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

	public static IncrementalResult load(String[] args) throws Exception {
		PrintStream original = new PrintStream(System.out);
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

		Map<String, List<IdNode>> leafs4Posts = new HashMap<String, List<IdNode>>();
		Map<String, ArrayList<Node>> tree4Posts = new HashMap<String, ArrayList<Node>>();
		Map<String, Set<String>> rules4Posts = new HashMap<String, Set<String>>();
		Map<String, Map<String, String>> infers4Posts = new HashMap<String, Map<String, String>>();
		
		
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

			
			
			// print tree 
			Collections.sort(tree);
			Ocl2Boogie.init(tarmm);
			PrintStream out;

			String goalName = post.getCommentsBefore().get(0).replace("--", "")+"/";
			String folderName = String.format("%s%s", subGoalsPath,goalName);
			File file = new File(folderName); 
			FileUtils.forceMkdir(file);
			
			
			
			int i = 0;
			Set<String> relatedRules = new HashSet<String>();	// get all related rules for a post
			
			for(Node n : NodeHelper.findLeafs(tree)){
				//gen sub-goals
				String fileName = String.format("%scase%02d.bpl", folderName,i);
				out =  new PrintStream(new FileOutputStream(fileName));
				System.setOut(out);
				System.out.println(n.toBoogie(env));
				out.close();
				
				//
				String id = String.format("case%02d", i);
				IdNode currNode = new IdNode(id, n);

				if (leafs4Posts.get(goalName) == null) {
					List<IdNode> nodes = new ArrayList<IdNode>();
					nodes.add(currNode);
					leafs4Posts.put(goalName, nodes);
				} else {
					leafs4Posts.get(goalName).add(currNode);
				}
				
				
				relatedRules.addAll(n.getInvolvedRuls());	
				
				
				i++;
			}
			
			tree4Posts.put(goalName, tree);
			rules4Posts.put(goalName, relatedRules);
			
			Map<String,String> infers = new HashMap<String,String>(TypeInference.lookup);
			infers4Posts.put(goalName, infers);
			
			
			printDriver(env, post, folderName);

		}

		GenBy.init(rules,srcmm);
		GenBy.print(genByPath);
		
		System.setOut(original);
		return new IncrementalResult(leafs4Posts,tree4Posts,rules4Posts, env, tarmm, infers4Posts);
	}
	
	private static void printDriver(ExecEnv env, OclExpression post, String folderName) throws Exception {
		PrintStream original = new PrintStream(System.out);
		
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
		out.close();
		
		System.setOut(original);
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
