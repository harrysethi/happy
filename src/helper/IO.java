/**
 * 
 */
package helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import domain.Pair_data;
import domain.Pair_truth;
import domain.Potentials;

/**
 * @author harinder
 *
 */
public class IO {
	public static void readPotentials(String ocrFilePath, String transFilePath)
			throws IOException {
		readOcrPotentials(ocrFilePath);
		readTransPotentials(transFilePath);
	}

	private static void readOcrPotentials(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));

		String line = br.readLine();

		while (line != null) {
			StringTokenizer st = new StringTokenizer(line);
			Potentials.addOcrPotentialEntry(st.nextToken(), st.nextToken(),
					st.nextToken());
			line = br.readLine();
		}

		br.close();
	}

	private static void readTransPotentials(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));

		String line = br.readLine();
		while (line != null) {
			StringTokenizer st = new StringTokenizer(line);
			Potentials.addTransPotentialEntry(st.nextToken(), st.nextToken(),
					st.nextToken());
			line = br.readLine();
		}

		br.close();
	}

	public static List<Pair_truth> readTruthPairs(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));

		String line = br.readLine();
		
		List<Pair_truth> truthPairs = new ArrayList<Pair_truth>();
		while (line != null) {
			Pair_truth truthPair = new Pair_truth();
			
			truthPair.first = line.trim(); 
			truthPair.second = br.readLine().trim();
			
			line = br.readLine();
			line = br.readLine();
			
			truthPairs.add(truthPair);
		}

		br.close();
		
		return truthPairs;
	}

	public static List<Pair_data> readDataPairs(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));

		String line = br.readLine();
		
		List<Pair_data> dataPairs = new ArrayList<Pair_data>();
		while (line != null) {
			Pair_data dataPair = new Pair_data();
			
			StringTokenizer st = new StringTokenizer(line);
			List<String> imageIds_first = new ArrayList<String>();
			while(st.hasMoreElements()){
				imageIds_first.add(st.nextToken());
			}

			dataPair.first = imageIds_first;
			
			line = br.readLine();
			st = new StringTokenizer(line);
			List<String> imageIds_second = new ArrayList<String>();
			while(st.hasMoreElements()){
				imageIds_second.add(st.nextToken());
			}
			
			dataPair.second = imageIds_second;
			line = br.readLine();
			line = br.readLine();
			
			dataPairs.add(dataPair);
		}

		br.close();
		
		return dataPairs;
	}
}
