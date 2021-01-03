package uk.ac.soton.ecs;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KNN
{
	public KNN(int k)
	{

	}

	public void train(GroupedDataset trainingDataset)
	{

	}

	public Map<String, String> test(ListDataset testingDataset)
	{
		return new HashMap<>();
	}

	/**
	 * Crops and resizes the given image into a 16x16 square
	 * @param image Image to be made tiny
	 * @return The tiny image
	 */
	private static FImage tinyImage(FImage image)
	{
		double[] result;

		int size = Math.min(image.getHeight(), image.getRows());
		FImage square = image.extractCenter(size, size);
		new ResizeProcessor(16f, 16f).processImage(square);

		return square;
	}

	/**
	 * Gets a vector of the pixels in the image and makes them zero mean
	 * @param image The image to get the vector from
	 * @return Zero mean vector of the image
	 */
	private static double[] zeroMeanVector(FImage image)
	{
		double[] vector = image.getDoublePixelVector();

		// Make the vector zero mean
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

	public static void main(String[] args) throws FileSystemException
	{
		GroupedDataset trainingData = Main.loadTrainingData();
		FImage test = (FImage) trainingData.getRandomInstance();

		FImage tiny = tinyImage(test);
		DisplayUtilities.display(tiny);

		double[] vector = zeroMeanVector(tiny);
		System.out.println(Arrays.toString(vector));
	}
}
