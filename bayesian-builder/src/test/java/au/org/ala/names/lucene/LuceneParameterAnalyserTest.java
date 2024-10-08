package au.org.ala.names.lucene;

import au.org.ala.bayesian.Inference;
import au.org.ala.bayesian.Network;
import au.org.ala.bayesian.Observable;
import au.org.ala.bayesian.Observation;
import au.org.ala.names.generated.SimpleLinnaeanFactory;
import au.org.ala.vocab.BayesianTerm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LuceneParameterAnalyserTest {
    private static final double DEFAULT_WIEGHT = 1.0;
    private Network network;
    private LuceneUtils lucene;
    private Observable<Double> weight;
 
    @Before
    public void setUp() throws Exception {
        BayesianTerm.values(); // Ensure loaded
        this.network = Network.read(this.getClass().getResource("simple-network.json"));
        this.weight = this.network.findObservable(BayesianTerm.weight, Double.class,true).get();
        this.lucene = new LuceneUtils(LuceneParameterAnalyser.class, "parameter-analyser-1.csv", this.network.getObservables(), this.network.getNameObservable());
    }

    @After
    public void cleanUp() throws Exception {
        if (this.lucene != null) {
            this.lucene.close();
            this.lucene = null;
        }
    }

    private Observation makeFact(Observable observable, String... values) {
        return new Observation(true, observable, values);
    }

    private Observation makeNotFact(Observable observable, String... values) {
        return new Observation(false, observable, values);
    }

    @Test
    public void testTotalWeight1() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        assertEquals(250.0, analyser.getTotalWeight(), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputePrior1() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        assertEquals(0.28, analyser.computePrior(this.makeFact(SimpleLinnaeanFactory.genus,"Osphranter")), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputePrior2() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        assertEquals(0.48, analyser.computePrior(this.makeFact(SimpleLinnaeanFactory.genus,"Acacia")), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputePrior3() throws Exception {
         LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);

        assertEquals(Inference.MAXIMUM_PROBABILITY, analyser.computePrior(this.makeFact(SimpleLinnaeanFactory.genus,"Montitega", "Acacia", "Osphranter", "Agathis")), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputePrior4() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);

        assertEquals(Inference.MINIMUM_PROBABILITY, analyser.computePrior(this.makeFact(SimpleLinnaeanFactory.genus,"Invalid")), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputeConditional1() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.genus,"Acacia");
        Observation input1 = this.makeFact(SimpleLinnaeanFactory.specificEpithet,"abbreviata");
        assertEquals(Inference.MAXIMUM_PROBABILITY, analyser.computeConditional(node, input1), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputeConditional2() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.genus,"Acacia");
        Observation input1 = this.makeNotFact(SimpleLinnaeanFactory.specificEpithet,"abbreviata");
        assertEquals(0.43478260869565216, analyser.computeConditional(node, input1), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputeConditional3() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.genus,"Acacia");
        Observation input1 = this.makeFact(SimpleLinnaeanFactory.specificEpithet,"dealbata");
        assertEquals(0.7692307692307693, analyser.computeConditional(node, input1), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputeConditional4() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.genus,"Acacia");
        Observation input1 = this.makeFact(SimpleLinnaeanFactory.specificEpithet,"dealbata");
        Observation input2 = this.makeFact(SimpleLinnaeanFactory.scientificName,"Acacia dealbata");
        assertEquals(Inference.MAXIMUM_PROBABILITY, analyser.computeConditional(node, input1, input2), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputeConditional5() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.genus, "Acacia");
        Observation input1 = this.makeFact(SimpleLinnaeanFactory.specificEpithet,"dealbata");
        Observation input2 = this.makeNotFact(SimpleLinnaeanFactory.scientificName, "Acacia dealbata");
        assertEquals(Inference.MINIMUM_PROBABILITY, analyser.computeConditional(node, input1, input2), Inference.MINIMUM_PROBABILITY);
    }


    @Test
    public void testComputeConditional6() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.family,"Fabaceae");
        Observation input1 = this.makeFact(SimpleLinnaeanFactory.genus,"Acacia");
        assertEquals(Inference.MAXIMUM_PROBABILITY, analyser.computeConditional(node, input1), Inference.MINIMUM_PROBABILITY);
    }

    @Test
    public void testComputeConditional7() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.order,"Fabales");
        Observation input1 = this.makeFact(SimpleLinnaeanFactory.family,"Fabaceae");
        assertEquals(Inference.MAXIMUM_PROBABILITY, analyser.computeConditional(node, input1), Inference.MINIMUM_PROBABILITY);
    }

    // Homonym
    @Test
    public void testComputeConditional8() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.family,"Braconidae");
        Observation input1 = this.makeFact(SimpleLinnaeanFactory.genus,"Agathis");
        assertEquals(0.6666666666666666, analyser.computeConditional(node, input1), Inference.MINIMUM_PROBABILITY);
    }

    // Positive input
    @Test
    public void testComputeConditional9() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.family,"Braconidae");
        Observation input1 = this.makeFact(SimpleLinnaeanFactory.taxonId,"ID-6");
        Observation input2 = this.makeFact(SimpleLinnaeanFactory.genus,"Agathis");
        assertEquals(Inference.MAXIMUM_PROBABILITY, analyser.computeConditional(node, input1, input2), Inference.MINIMUM_PROBABILITY);
    }

    // Negative input
    @Test
    public void testComputeConditional10() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.family,"Braconidae");
        Observation input1 = this.makeNotFact(SimpleLinnaeanFactory.taxonId,"ID-6");
        Observation input2 = this.makeFact(SimpleLinnaeanFactory.genus,"Agathis");
        assertEquals(Inference.MINIMUM_PROBABILITY, analyser.computeConditional(node, input1, input2), Inference.MINIMUM_PROBABILITY);
    }

    // Negative input
    @Test
    public void testComputeConditional11() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.family,"Braconidae");
        Observation input1 = this.makeNotFact(SimpleLinnaeanFactory.taxonId,"ID-2");
        Observation input2 = this.makeFact(SimpleLinnaeanFactory.genus,"Agathis");
        assertEquals(0.6666666666666666, analyser.computeConditional(node, input1, input2), Inference.MINIMUM_PROBABILITY);
    }

    // Negative input
    @Test
    public void testComputeConditional12() throws Exception {
        LuceneParameterAnalyser analyser = new LuceneParameterAnalyser(this.network, this.lucene.getSearcher(), this.weight, DEFAULT_WIEGHT, this.network.getInputs(), this.network.getOutputs(), 10000, false);
        Observation node = this.makeFact(SimpleLinnaeanFactory.family,"Braconidae");
        Observation input1 = this.makeNotFact(SimpleLinnaeanFactory.taxonId,"ID-5");
        Observation input2 = this.makeFact(SimpleLinnaeanFactory.genus,"Agathis");
        assertEquals(Inference.MAXIMUM_PROBABILITY, analyser.computeConditional(node, input1, input2), Inference.MINIMUM_PROBABILITY);
    }

}
