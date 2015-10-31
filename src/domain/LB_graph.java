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
public class LB_graph {
	//public List<LB_BaseNode> nodes;
	public List<LB_variableNode> variableNodes;
	public List<LB_factorNode> factorNodes;
	
	public LB_graph() {
		//this.nodes = new ArrayList<LB_BaseNode>();
		this.variableNodes = new ArrayList<LB_variableNode>();
		this.factorNodes = new ArrayList<LB_factorNode>();
	}
}
