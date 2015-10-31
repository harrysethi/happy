/**
 * 
 */
package domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import worker.FactorHelper;

/**
 * @author harinder
 *
 */
public class LB_edge {
	public LB_baseNode src;
	public LB_baseNode dest;
	
	public Map<Object, List<Object>> msg;
	
	public LB_edge(LB_baseNode src, LB_baseNode dest) {
		this.src = src;
		this.dest = dest;
		
		this.msg = new HashMap<Object, List<Object>>();
		
		if(src instanceof LB_variableNode) {
			FactorHelper.createFactorProduct(msg, ((LB_variableNode) src).inGraphNode, 1.0);
		}
		
		else if(dest instanceof LB_variableNode) {
			FactorHelper.createFactorProduct(msg, ((LB_variableNode) dest).inGraphNode, 1.0);
		}
	}
	
	@Override
	public String toString() {
		return this.src + "->" + this.dest;
	}
}
