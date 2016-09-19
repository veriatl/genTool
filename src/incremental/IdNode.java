package incremental;

import datastructure.Node;

public class IdNode {
	String id;
	Node n;
	
	public IdNode(String id, Node n){
		this.id= id;
		this.n = n;
	}
	
	public Node getNode(){
		return this.n;
	}
	
	public String getId(){
		return this.id;
	}
}
