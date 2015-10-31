package domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constants.FactorType;
import worker.FactorHelper;

public class LB_factorNode extends LB_baseNode {
	public LB_variableNode variableNode1;
	public LB_variableNode variableNode2;
	
	public Factor factor;
	
	public Map<Object, List<Object>> potentials;
	
	@Override
	public String toString() {
		return this.factor.toString();
	}
	
	public LB_factorNode(LB_variableNode variableNode) {
		this.edges = new ArrayList<LB_edge>();
		this.factor = new Factor();
		
		this.variableNode1 = variableNode;
		
		//factorNode with 1 variable is an OCR factor node
		this.factor.factorType = FactorType.OCR;
		this.factor.inGraphNode1 = this.variableNode1.inGraphNode;
		
		setPotentials();
	}
	
	public LB_factorNode(LB_variableNode variableNode1, LB_variableNode variableNode2, FactorType factorType) {
		this.edges = new ArrayList<LB_edge>();
		this.factor = new Factor();
		
		this.variableNode1 = variableNode1;
		this.variableNode2 = variableNode2;
		
		this.factor.factorType = factorType;
		this.factor.inGraphNode1 = this.variableNode1.inGraphNode;
		this.factor.inGraphNode2 = this.variableNode2.inGraphNode;
		
		setPotentials();
	}

	private void setPotentials() {
		this.potentials = new HashMap<Object, List<Object>>();
		FactorHelper.createFactorProduct(potentials, this.getBelongingNodes(), 1.0);
		
		List<Object> valueList = this.potentials.get("Value");

		List<Object> factorList_specific2Node_1 = this.potentials.get(factor.inGraphNode1);
		List<Object> factorList_specific2Node_2 = this.potentials.get(factor.inGraphNode2);
			
		FactorHelper.setValueBasedOnFactor(valueList, this.factor, factorList_specific2Node_1, factorList_specific2Node_2);
	}
	
	public Set<InGraphNode> getBelongingNodes() {
		Set<InGraphNode> belongingNodes = new HashSet<InGraphNode>();
		
		belongingNodes.add(this.variableNode1.inGraphNode);
		if(this.variableNode2 != null)
			belongingNodes.add(this.variableNode2.inGraphNode);
		
		return belongingNodes;
	}
}
