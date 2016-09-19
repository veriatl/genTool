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
		
		for(Node n : children){
			if(n.getResult() == TriBoolean.UNKNOWN){
				count++;
				next = n;
			}		
		}
		
		if(count == 1){
			vTree.remove(r);
			vTree.removeAll(children);
			next.parent = null;
			vTree.add(next);
			return findSimplifiedPost(vTree);
		}else {
			return r;	
		}
		

	}
	
	

}
