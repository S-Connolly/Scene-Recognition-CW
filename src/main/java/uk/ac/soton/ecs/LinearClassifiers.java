package uk.ac.soton.ecs;

import de.bwaldvogel.liblinear.SolverType;
import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.*;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.DoubleCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.DoubleKMeans;
import org.openimaj.util.pair.IntDoublePair;

import java.awt.*;
import java.util.*;
import java.util.List;
public class LinearClassifiers {

    //sizes proposed in the CW spec
    int patchSize = 8;
    int sampleDistance = 4;
    int clusterSize = 500;

    public LinearClassifiers() {
    }

    public LinearClassifiers(int patchSize, int sampleDistance, int clusterSize) {
        this.patchSize = patchSize;
        this.sampleDistance = sampleDistance;
        this.clusterSize = clusterSize;
    }

    protected Map<String, String> train(VFSGroupDataset<FImage> training, VFSListDataset<FImage> testing) {
        HardAssigner<double[], double[], IntDoublePair> assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(training, 100));

        //feature extractor
        DenseFeatureExtractor extractor = new DenseFeatureExtractor(assigner);

        LiblinearAnnotator<FImage, String> ann = new LiblinearAnnotator<FImage, String>(
                extractor, LiblinearAnnotator.Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);

        ann.train(training);

        Map<String, String> classifications = new HashMap<>();

        for (int i = 0; i < testing.size(); i++) {
            String prediction = (String) ann.classify(testing.getInstance(i)).getPredictedClasses().toArray()[0];
            classifications.put(testing.getID(i), prediction);
        }

        return classifications;
    }

    //cropping patches depending on the parameters of the LinearClassifier
    private FImage[][] getPatches(FImage image) {
        FImage[][] patch = new FImage[(int) Math.floor((image.getWidth() - patchSize) / sampleDistance) + 1][(int) Math.floor((image.getHeight() - patchSize) / sampleDistance) + 1];
        for (int i = 0; i < patch.length; i++) {
            for (int i2 = 0; i2 < patch[i].length; i2++) {
                Point markedPoint = new Point(i * sampleDistance + patchSize, i2 * sampleDistance + patchSize);
                float[][] crop = new float[patchSize][patchSize];
                for (int y = markedPoint.y - patchSize; y < markedPoint.y; y++) {
                    for (int x = markedPoint.x - patchSize; x < markedPoint.x; x++) {
                        int croppedX = x - (markedPoint.x - patchSize);
                        int croppedY = y - (markedPoint.y - patchSize);
                        crop[croppedY][croppedX] = image.pixels[y][x];
                    }
                }
                patch[i][i2] = new FImage(crop);
            }
        }
        return patch;
    }

    //assigner that assigns features to identifiers
    HardAssigner<double[], double[], IntDoublePair> trainQuantiser(Dataset<FImage> sample) {
        //patch vector arraylist
        List<Double[]> allkeys = new ArrayList<Double[]>();
        FImage[][] patch;
        for (FImage img : sample) {
            patch = getPatches(img);
            for (int i = 0; i < patch.length; i++) {
                for (int i2 = 0; i2 < patch[i].length; i2++) {
                    allkeys.add(convert(normalisingVector(patch[i][i2].getDoublePixelVector()).asDoubleVector()));
                }
            }
        }
        Collections.shuffle(allkeys);
        if (allkeys.size() > 10000)
            allkeys = allkeys.subList(0, 10000);

        DoubleKMeans kMeans = DoubleKMeans.createKDTreeEnsemble(clusterSize);
        DoubleCentroidsResult result = kMeans.cluster(fromList(allkeys));

        return result.defaultHardAssigner();
    }

    //method for normalising and centering the vector
    private FeatureVector normalisingVector(double[] vector) {
        double mean = 0;
        double length = 0;

        for(int i = 0; i < vector.length; i++){
            mean += vector[i];
        }
        mean = mean / vector.length;
        for(int i = 0; i < vector.length; i++){
            vector[i] = vector[i] - mean;
            length += Math.pow(vector[i],2);
        }
        for(int i = 0; i < vector.length; i++){
            vector[i] = vector[i] /length;
        }
        return new DoubleFV(vector);
    }

    //Dense feature extractor implementation that will train our classifier
    class DenseFeatureExtractor implements FeatureExtractor<DoubleFV, FImage> {
        HardAssigner<double[], double[], IntDoublePair> assigner;

        public DenseFeatureExtractor(HardAssigner<double[], double[], IntDoublePair> assigner) {
            this.assigner = assigner;
        }
        //Maps the patches to a visual word
        public DoubleFV extractFeature(FImage image) {
            FImage[][] patch = getPatches(image);
            BagOfVisualWords<double[]> bovw = new BagOfVisualWords<double[]>(assigner);
            List<double[]> extractedFeatures = new ArrayList<double[]>();
            for (int i = 0; i < patch.length; i++) {
                for (int i2 = 0; i2 < patch[i].length; i2++) {
                    extractedFeatures.add(normalisingVector(patch[i][i2].getDoublePixelVector()).asDoubleVector());
                }
            }
            return bovw.aggregateVectorsRaw(extractedFeatures).asDoubleFV();
        }
    }

    //methods for double[] to Double[] conversion and contrary
    private Double[] convert(double[] _double) {
        Double[] ret = new Double[_double.length];
        for (int i = 0; i < _double.length; i++) {
            ret[i] = _double[i];
        }
        return ret;
    }
    private double[] convert(Double[] _Double) {
        double[] ret = new double[_Double.length];
        for (int i = 0; i < _Double.length; i++) {
            ret[i] = _Double[i];
        }
        return ret;
    }
    private double[][] fromList(List<Double[]> list) {
        double[][] converted = new double[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            converted[i] = convert(list.get(i));
        }
        return converted;
    }
}
