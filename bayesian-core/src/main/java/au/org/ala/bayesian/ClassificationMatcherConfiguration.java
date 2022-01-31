package au.org.ala.bayesian;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * Configuration information for a {@link au.org.ala.bayesian.ClassificationMatcher}
 */
@Value
@Builder
public class ClassificationMatcherConfiguration {
    /** Make the classification matcher visible through JMX */
    @JsonProperty
    @Builder.Default
    private boolean enableJmx = false;
    /** Instrument the classification matcher and collect statistics */
    @JsonProperty
    @Builder.Default
    private boolean instrument = false;
    /** Collect detailed statistics on each classification */
    @JsonProperty
    @Builder.Default
    private boolean statistics = false;

}