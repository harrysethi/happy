package helper;

import java.util.Random;

import constants.Consts;

/**
 * 
 */

/**
 * @author harinder
 *
 */
public class Util {
	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	public static char getRandomCharacter() {
		int randCharIndex = Util.randInt(0, Consts.characters.length - 1);
		return Consts.characters[randCharIndex];
	}
}
