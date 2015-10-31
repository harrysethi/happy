/**
 * 
 */
package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author harinder
 *
 */
public class CliqueTreeNode {
	
	public boolean isVisited; //used in BFS - downward msg passing
	
	private static int nodeCounter;

	public boolean isRoot;
	private int nodeID;
	public Set<InGraphNode> belongingNodes;
	public List<CliqueTreeEdge> adjList;
	
	public List<Factor> factors;
	
	public Map<Object, List<Object>> factorProduct;

	public CliqueTreeNode() {
		this.nodeID = CliqueTreeNode.nodeCounter;
		CliqueTreeNode.nodeCounter++;
		
		this.belongingNodes = new HashSet<InGraphNode>();
		this.adjList = new ArrayList<CliqueTreeEdge>();
		this.factors = new ArrayList<Factor>();
		
		this.factorProduct = new HashMap<Object, List<Object>>();
	}

	public int getNodeID() {
		return this.nodeID;
	}
	
	@Override
	public String toString(){
		return this.belongingNodes.toString();
	}
	
	public CliqueTreeEdge getCliqueTreeEdge(CliqueTreeNode cliqueTreeNode) {
		for (CliqueTreeEdge cliqueTreeEdge : adjList) {
			if(cliqueTreeEdge.getDest() == cliqueTreeNode)
				return cliqueTreeEdge;
		}
		
		return null;
	}
	
	public int getAdjacencyCount() {
		int count = 0;
		
		for(CliqueTreeEdge cliqueTreeEdge : this.adjList) {
			if(cliqueTreeEdge.isPresent && !cliqueTreeEdge.isBroken) count++;
		}
		
		return count;
	}
	
	public CliqueTreeEdge getEdgeFromLeaf() {
		//this assumes the node is a leaf...that is, it has only 1 present edge
		for(CliqueTreeEdge cliqueTreeEdge : this.adjList) {
			if(cliqueTreeEdge.isPresent && !cliqueTreeEdge.isBroken) return cliqueTreeEdge;
		}
		return null;
	}

}
