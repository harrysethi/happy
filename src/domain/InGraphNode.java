/**
 * 
 */
package domain;

import java.util.ArrayList;
import java.util.List;

import constants.WordNumType;

/**
 * @author harinder
 *
 */
public class InGraphNode {
	public WordNumType wordNumType;
	public int nodeID;
	public String imgID;
	public List<InGraphNodeAdjacency> adjList;
	public boolean isActive;
	
	private String key;
	
	public char charAssigned; //used for gibbs sampling
	
	
	public InGraphNode(int nodeID, String imgID, WordNumType wordNumType){
		this.nodeID = nodeID;
		this.imgID = imgID; 
		this.wordNumType = wordNumType;
		this.adjList = new ArrayList<InGraphNodeAdjacency>();
		this.isActive = true;
		
		this.key = this.nodeID + "<=>" + this.wordNumType;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public boolean isAdjacent(InGraphNode inGraphNode) {
		for (InGraphNodeAdjacency inGraphNodeAdjacency : adjList) {
			if(inGraphNodeAdjacency.inGraphNode.nodeID == inGraphNode.nodeID) return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "{" + this.nodeID + ":" + this.imgID + "|" + this.wordNumType + "|" + this.isActive + "}";
	}
}
