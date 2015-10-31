/**
 * 
 */
package domain;

import java.util.List;

/**
 * @author harinder
 *
 */
public class Pair_data {
	public List<String> first;
	public List<String> second;
	
	public String toString(){
		return this.first + ":" + this.second;
	}
}
