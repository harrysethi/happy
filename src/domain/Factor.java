/**
 * 
 */
package domain;

import constants.FactorType;

/**
 * @author harinder
 *
 */
public class Factor {
	public FactorType factorType;
	public InGraphNode inGraphNode1;
	public InGraphNode inGraphNode2;
	
	@Override
	public String toString() {
		return "{" + this.factorType + ":" + inGraphNode1 + ":" + inGraphNode2 + "}";
	}
}
