/**
 * 
 */
package domain;

import constants.WordNumType;

/**
 * @author harinder
 *
 */
public class Gibbs_sample {
	public char sample_w1[];
	public char sample_w2[];
	
	public Gibbs_sample(int size_w1, int size_w2) {
		sample_w1 = new char[size_w1];
		sample_w2 = new char[size_w2];
	}
	
	public Gibbs_sample(Gibbs_sample prevSample, int indexToChng, char newChar, WordNumType wordNumType) {
		System.arraycopy( prevSample.sample_w1, 0, this.sample_w1, 0, prevSample.sample_w1.length );
		System.arraycopy( prevSample.sample_w2, 0, this.sample_w2, 0, prevSample.sample_w2.length );
		
		switch(wordNumType) {
		case WORD_NUM_1:
			this.sample_w1[indexToChng] = newChar;
			break;
		case WORD_NUM_2:
			this.sample_w2[indexToChng] = newChar;
			break;
		}
	}
}
