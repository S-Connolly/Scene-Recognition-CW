package uk.ac.soton.ecs;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

import java.io.*;
import java.util.*;

public class KNN
{
	private final int k;

	//private Map<String, List<double[]>> dataset;
	private Map<String, List<double[]>> trainingDataset;// = new HashMap<>();
	private Map<String, List<double[]>> testingDataset;// = new HashMap<>();

	public KNN(int k)
	{
		this.k = k;
	}

	/**
	 * Trains calculates all the tiny image vectors for the dataset given
	 * @param imageDataset The dataset to train on
	 */
	public void train(VFSGroupDataset<FImage> imageDataset, double split)
	{
		Map<String, List<double[]>> dataset = new HashMap<>();
		for(String groupName : imageDataset.getGroups())
		{
			List<double[]> group = new ArrayList<>();
			for(FImage image : imageDataset.get(groupName))
			{
				group.add(unitLengthVector(zeroMeanVector(tinyImage(image))));
			}
			dataset.put(groupName, group);
		}

		trainingDataset = new HashMap<>();
		testingDataset = new HashMap<>();
		splitData(dataset, split);
	}

	public void splitData(Map<String, List<double[]>> dataset, double split) {

		List<double[]> sublist1;
		List<double[]> sublist2;

		for (Map.Entry<String, List<double[]>> entry : dataset.entrySet()) {

				int size = (int) (entry.getValue().size() * split);
				sublist1 = entry.getValue().subList(0, size);
				sublist2 = entry.getValue().subList(size, entry.getValue().size());

				trainingDataset.put(entry.getKey(), sublist1);
				testingDataset.put(entry.getKey(), sublist2);
		}
	}

//	public Map<String, String> test(VFSListDataset<FImage> testingDataset)
//	{
//
//		Map<String, String> predictions = new HashMap<>();
//
//		String name;
//		String pred;
//		double distance;
//		Double[] kNearest = new Double[k];
//		String[] kNearestStrings;
//		FImage testImage;
//		String testImageName;
//		double max = Double.MAX_VALUE;
//
//
//		for (int i = 0; i < testingDataset.size(); i++) {
//			testImage = testingDataset.get(i);
//			testImageName = testingDataset.getID(i);
//			System.out.println();
//			System.out.println("Testing " + testImageName + "		" + i);
//			Arrays.fill(kNearest, max);
//			kNearestStrings = new String[k];
//			Arrays.fill(kNearestStrings, "");
//
//			for(Map.Entry<String, List<double[]>> entry : dataset.entrySet()) {
//				name = entry.getKey();
//
//				for (double[] d : entry.getValue()) {
//					distance = Math.abs(euclid(d, unitLengthVector(zeroMeanVector(tinyImage(testImage)))));
//					addKNearest(distance, kNearest, kNearestStrings, name);
//				}
//			}
//
//			pred = highestFrequency(kNearestStrings);
//			System.out.println("Predicted " + pred);
//			predictions.put(testImageName, pred);
//		}
//
//		System.out.println("Testing Completed!");
//		return predictions;
//	}

	public Map<String, String> test()
	{

		Map<String, String> predictions = new HashMap<>();

		double correct = 0d;
		double wrong = 0d;
		String name;
		String pred;
		double distance;
		Double[] kNearest = new Double[k];
		String[] kNearestStrings;
		String testImageName;
		double max = Double.MAX_VALUE;
		int i = 0;

		for(Map.Entry<String, List<double[]>> entry1 : testingDataset.entrySet()) {
			testImageName = entry1.getKey();

			for (double[] d1 : entry1.getValue()) {
				System.out.println();
				System.out.println("Testing " + testImageName + "		" + i);
				Arrays.fill(kNearest, max);
				kNearestStrings = new String[k];
				Arrays.fill(kNearestStrings, "");

				for (Map.Entry<String, List<double[]>> entry2 : trainingDataset.entrySet()) {
					name = entry2.getKey();

					for (double[] d2 : entry2.getValue()) {
						distance = Math.abs(euclid(d2, d1));
						addKNearest(distance, kNearest, kNearestStrings, name);
					}
				}
				pred = highestFrequency(kNearestStrings);
				System.out.println("Predicted " + pred);
				if (pred.equals(testImageName)) correct++;
					else wrong++;
				predictions.put(testImageName, pred);
				i++;
			}
		}

		System.out.println("Testing Completed!");
		System.out.println("Accuracy = " + ((correct/(correct+wrong))*100) + "%");
		return predictions;
	}

	/**
	 * Computes the euclidean distance between 2 vectors of the same length
	 * @param d1 Vector A
	 * @param d2 Vector B
	 * @return The euclidean distance between A and B
	 */
	private static double euclid(double[] d1, double[] d2) {
		double distance = 0;
		for (int i = 0; i < d1.length; i++) {
			distance += Math.pow(d1[i]-d2[i], 2);
		}
		return Math.sqrt(distance);
	}

	/**
	 * Crops and resizes the given image into a 256 length vector square
	 * @param image Image to be made tiny
	 * @return The vector of the tiny image
	 */
	private static double[] tinyImage(FImage image)
	{
		int size = Math.min(image.getHeight(), image.getRows());
		FImage square = image.extractCenter(size, size);
		new ResizeProcessor(16f, 16f).processImage(square);

		return square.getDoublePixelVector();
	}

	/**
	 * Creates a zero mean vector from the vector provided
	 * @param vector The vector to be made zero mean
	 * @return The zero mean vector
	 */
	private static double[] zeroMeanVector(double[] vector)
	{
		double mean = 0d;
		for(double value : vector)
		{
			mean += value;
		}
		mean /= vector.length;

		double[] zeroMeanVector = new double[vector.length];
		for(int i = 0; i < zeroMeanVector.length; i++)
		{
			zeroMeanVector[i] = vector[i] - mean;
		}

		return zeroMeanVector;
	}

	/**
	 * Creates a unit length vector from the vector provided
	 * @param vector The vector to be made unit length
	 * @return The unit length vector
	 */
	private static double[] unitLengthVector(double[] vector)
	{
		double length = euclid(vector, new double[vector.length]);

		double[] unitLengthVector = new double[vector.length];
		for(int i = 0; i < unitLengthVector.length; i++)
		{
			unitLengthVector[i] = vector[i] / length;
		}

		return unitLengthVector;
	}

	private static void addKNearest(double input, Double[] kNearest, String[] kNearestStrings, String name) {
		int indexOfLargest = getIndexOfLargest(kNearest);

		if (kNearest[indexOfLargest] > input) {
			kNearest[indexOfLargest] = input;
			kNearestStrings[indexOfLargest] = name;
		}
	}

	private static int getIndexOfLargest(Double[] arr) {
		if ( arr== null || arr.length == 0 ) return -1;

		int largest = 0;
		for ( int i = 1; i < arr.length; i++ ) {
			if ( arr[i] > arr[largest] ) {
				largest = i;
			}
		}
		return largest;
	}

	private static String highestFrequency(String[] kNearestStrings) {

		if (kNearestStrings == null || kNearestStrings.length == 0)
			return "";

		Arrays.sort(kNearestStrings);

		String previous = kNearestStrings[0];
		String popular = kNearestStrings[0];
		int count = 1;
		int maxCount = 1;

		for (int i = 1; i < kNearestStrings.length; i++) {
			if (kNearestStrings[i].equals(previous))
				count++;
			else {
				if (count > maxCount) {
					popular = kNearestStrings[i-1];
					maxCount = count;
				}
				previous = kNearestStrings[i];
				count = 1;
			}
		}

		return count > maxCount ? kNearestStrings[kNearestStrings.length-1] : popular;
	}
}
