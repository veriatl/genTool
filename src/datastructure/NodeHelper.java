package datastructure;

import java.util.ArrayList;

public class NodeHelper {

	
	public static Node findRoot(ArrayList<Node> tree){
		for(Node n : tree){
			if(n.parent == null){
				return n;
			}
		}
		return null;
	}
	

	
	
	public static ArrayList<Node> findLeafs(ArrayList<Node> tree){
		
		ArrayList<Node> nonLeafs = new ArrayList<Node>();
		ArrayList<Node> Leafs = new ArrayList<Node>();
		
		for(Node n : tree){
			if(n.parent != null){
				nonLeafs.add(n.parent);
			}
		}
		
		for(Node n: tree){
			if(!nonLeafs.contains(n)){
				Leafs.add(n);
			}
		}
		
		return Leafs;
		
	}

	public static ArrayList<Node> findChildren(Node parent, ArrayList<Node> tree) {
		ArrayList<Node> children =  new ArrayList<Node>();
		
		for(Node n : tree){
			if(n.parent == parent){
				children.add(n);
			}
		}
		
		return children;
	}

	public static Node findSimplifiedPost(ArrayList<Node> vTree) {
		Node r = findRoot(vTree);
		
		ArrayList<Node> children = findChildren(r, vTree);
		
		int count = 0;
		Node next = null;
		
		if(r.getResult() == TriBoolean.UNKNOWN){
			for(Node n : children){
				if(n.getResult() == TriBoolean.UNKNOWN){
					count++;
					next = n;
				}		
			}
			
			if(count == 1){
				ArrayList<Node> temp = new ArrayList<Node>(vTree);
				temp.remove(r);
				temp.removeAll(children);
				next.backUpParent = next.parent;
				next.parent = null;
				temp.add(next);
				return findSimplifiedPost(temp);
			}else {
				return r;	
			}
		}else{
			return r.getParent();
		}
		
		

	}

	public static TriBoolean repopulate(Node n, ArrayList<Node> vTree) {
		normalizeTree(vTree);
		Node r = findRoot(vTree);
		
		while(r.getResult() == TriBoolean.UNKNOWN){
			Node s = findSimplifiedPost(vTree);	
			TriBoolean res = TriBoolean.compute(findChildren(s, vTree));
			s.setResult(res);	
			normalizeTree(vTree);
		}
		
		return r.getResult();
	}

	private static void normalizeTree(ArrayList<Node> tree) {
		for(Node n : tree){
			if(n.getBackUpParent()!=null){
				n.setParent(n.getBackUpParent());
				n.setBackUpParent(null);
			}
		}	
	}
	
	

}
