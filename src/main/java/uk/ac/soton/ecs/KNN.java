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

	public static double[] tinyImage(FImage image) {
		double[] result;

		int size = Math.min(image.getHeight(), image.getRows());
		FImage square = image.extractCenter(size,size);
		new ResizeProcessor(16, 16).processImage(square);

		return square.getDoublePixelVector();
	}

	public static void main(String[] args) throws FileSystemException
	{
		GroupedDataset trainingData = Main.loadTrainingData();
		FImage test = (FImage) trainingData.getRandomInstance();

		System.out.println(Arrays.toString(tinyImage(test)));
	}
}
