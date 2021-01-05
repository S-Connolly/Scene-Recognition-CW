package uk.ac.soton.ecs;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Main
{
    public static void main( String[] args ) throws IOException
    {
        // Load datasets
        VFSGroupDataset<FImage> trainingData = loadTrainingData();
        VFSListDataset<FImage> testingData = loadTestingData();

        // Run #1 (K Nearest Neighbour)
        KNN knn = new KNN(10);
        knn.train(trainingData);
        Map<String, String> knnPredictions = knn.test(testingData);
        makePredictionFile("run1", knnPredictions);

        /*
        // Run #2 (Linear Classifiers)
        LinearClassifiers linear = new LinearClassifiers();
        Map<String, String> linearPredictions = linear.train(trainingData, testingData);
        makePredictionFile("run2", linearPredictions);
        for (String key : linearPredictions.keySet()) {
            System.out.println(key + " " + linearPredictions.get(key));
        }
        */

    }

    /**
     * Gets all of the images from the training datasets
     * @return VFSGroupDataset of training images
     * @throws FileSystemException If there is an error loading any of the image files
     */
    public static VFSGroupDataset<FImage> loadTrainingData() throws FileSystemException
    {
        String path = new File("training").getAbsolutePath();
        return new VFSGroupDataset<>(path, ImageUtilities.FIMAGE_READER);
    }

    /**
     * Gets all of the testing images from the testing dataset
     * @return VFSLListDataset of testing images
     * @throws FileSystemException If there is an error loading any of the image files
     */
    public static VFSListDataset<FImage> loadTestingData() throws FileSystemException
    {
        String path = new File("testing").getAbsolutePath();
        return new VFSListDataset<>(path, ImageUtilities.FIMAGE_READER);
    }

    /**
     * Writes the predictions to a txt file in the required format
     * @param fileName Name of the file to create (.txt extension added automatically)
     * @param predictions The map of image file to class predictions
     * @throws IOException If there is an error writing to the file
     */
    public static void makePredictionFile(String fileName, Map<String, String> predictions) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + ".txt"));

        for(String pair : predictions.keySet())
        {
            writer.write(pair + " " + predictions.get(pair) + "\r\n");
        }

        writer.close();
    }
}
