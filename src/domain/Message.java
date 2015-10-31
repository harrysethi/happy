/**
 * 
 */
package domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author harinder
 *
 */
public class Message {
	public Map<Object, List<Object>> factorProduct;
	public Set<InGraphNode> nodes;
	
	public Message(Map<Object, List<Object>> factorProduct, Set<InGraphNode> nodes) {
		this.factorProduct = factorProduct;
		this.nodes = nodes;
	}
}
