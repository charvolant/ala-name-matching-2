package au.org.ala.bayesian;

import au.org.ala.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * A condition that can be applied to a modifier
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Condition {
    /**
     * Test a classifier to see whether it meets the condition.
     *
     * @param classifier The classifier
     *
     * @return True if the classifier meets the condition
     *
     * @throws BayesianException if there is a problem with the classifier or condition
     */
    abstract public boolean check(Classifier classifier) throws BayesianException;

    /**
     * Generate a positive check that matches this condition for a classification.
     *
     * @param compiler The network compiler
     * @param var The classification variable to test
     *
     * @return The built condition, null for no condition
     */
    abstract public String buildCheck(NetworkCompiler compiler, String var);

    /**
     * Generate a positive check that matches this condition.
     *
     * @param compiler The network compiler
     * @param var The variable that holds the classifier
     * @param observables The class that holds observable definitions
     *
     * @return The built condition, null for no condition
     */
    abstract public String buildClassifierCheck(NetworkCompiler compiler, String var, String observables);

    /**
     * Load a condition from a URL.
     * <p>
     * Note that observables need to be copied into the URL, rather than inherited from the network.
     * This is a work in progress ...
     * </p>
     *
     * @param url The source URL
     *
     * @return The condition
     *
     * @throws IOException if unable to read the condition
     */
    public static Condition load(URL url) throws IOException {
        ObjectMapper mapper = JsonUtils.createMapper();
        return mapper.readValue(url, Condition.class);
    }
}
