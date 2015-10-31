/**
 * 
 */
package domain;

import java.util.List;

/**
 * @author harinder
 *
 */
public class CliqueTree {
	public List<CliqueTreeNode> nodes;
	//public List<CliqueTreeEdge> edges;
	
	public CliqueTreeNode getLeaf() {
		for(CliqueTreeNode cliqueTreeNode : this.nodes) {
			if(!cliqueTreeNode.isRoot && cliqueTreeNode.getAdjacencyCount()==1) return cliqueTreeNode;
		}
		
		return null;
	}
	
	public CliqueTreeNode getNewRoot() {
		//for(CliqueTreeNode cliqueTreeNode : this.nodes) {
			//if(cliqueTreeNode.getAdjacencyCount() == 1) {
				//cliqueTreeNode.isRoot = true;
				//return cliqueTreeNode;
			//}
		//}

		this.nodes.get(0).isRoot = true;
		return this.nodes.get(0);
	}
	
}
