package domain;

import java.util.List;
import java.util.Map;

public class LB_variableNode extends LB_baseNode {
	public InGraphNode inGraphNode;
	
	public Map<Object, List<Object>> belief;
	
	public LB_variableNode(InGraphNode inGraphNode) {
		this.inGraphNode = inGraphNode;
	}
	
	@Override
	public String toString() {
		return this.inGraphNode.toString();
	}
}
