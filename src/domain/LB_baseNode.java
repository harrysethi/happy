/**
 * 
 */
package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author harinder
 *
 */
public abstract class LB_baseNode {
	public List<LB_edge> edges;
	
	public LB_baseNode() {
		this.edges = new ArrayList<LB_edge>();
	}
	
	public void addEdge(LB_baseNode dest) {
		LB_edge lb_edge = new  LB_edge(this, dest);
		this.edges.add(lb_edge);
	}
	
	public LB_edge getSpecificEdge(LB_baseNode destNode) {
		for (LB_edge lb_edge : edges) {
			if(lb_edge.dest == destNode) return lb_edge;
		}
		
		return null;
	}
	
}
