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
				nonLeafs.add(findParent(tree, n));
			}
		}
		
		for(Node n: tree){
			if(!nonLeafs.contains(n)){
				Leafs.add(n);
			}
		}
		
		return Leafs;
		
	}
	
	
	public static Node findParent(ArrayList<Node> tree, Node t){
		
		for(Node n : tree){
			if(n.content == t.parent && t.level == n.level + 1){
				return n;
			}
		}
		
		return null;		
		
	}
}
