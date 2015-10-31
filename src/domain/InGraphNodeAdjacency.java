/**
 * 
 */
package domain;

import constants.FactorType;

/**
 * @author harinder
 *
 */
public class InGraphNodeAdjacency {
	public InGraphNode inGraphNode;
	public FactorType factorType;
	
	public InGraphNodeAdjacency(FactorType factorType, InGraphNode inGraphNode) {
		this.inGraphNode = inGraphNode;
		this.factorType = factorType;
	}
	
	@Override
	public String toString(){
		return factorType + "->" + inGraphNode;
	}
}
