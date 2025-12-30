# Using the Libraries

The following notes on use the [ALA Linnnaean Model](../ala-linnaean/README.md) as an example.
However, the principles outlined will work for any network constructed from this system.

## Installation

At the moment, these artefacts at not attached to a repository.
You will need to install the libraries and indexes before using them.

### Indexes

You can access pre-built search indexes at https://ala-nameindexes.s3.amazonaws.com/

The current indexes are in `2025-1-3`: 
`linnanen-2025-1-3.zip`, `vernacular-2025-1-3.zip` and `location-22025-1-3.zip`
Generally, these are expected to be unzipped into the `/data/lucene` directory.

#### Suggester Index

The suggester index is used to provide auto-suggest options.
If not present, the suggester index is built from search indexes
on-demand, a process that can take 20 minutes or more.
If the file `/data/tmp/suggester-2025-1-3/suggester-metadata.json` exists and shows a later date than the source data,
then the already built index in `/data/tmp/suggester-2025-1-3` is used.

### Building

You will need to do a `mvn install` in the root directory to make the libraries available.
Do this *after* you have installed the supplied indexes, since they will be needed to
run tests.

### Building an Index

If you don't like the pre-built indexes, you can build a new index by using the
`ala-distribution` archive and the following command:

```shell
generate-linnaean.sh -o /data/lucene/linnaean-XXX file:/data/combined-XXX
generate-vernacular.sh -o /data/lucene/vernacular-XXX file:/data/combined-XXX
```

Use the `--help` option to see what options are available.
In particular, the `-t n` will limit parallel processing to *n* threads;
you can use something like `-t 6` to stop processing from taking over your computer.

## Searching

To use the matching library, you need to include `ala-linnaean` as a dependency.
Using maven:

```xml
<dependency>
  <groupId>au.org.ala.names</groupId>
  <artifactId>ala-linnaean</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

You can build a searcher by

```java
import au.org.ala.bayesian.ClassificationMatcherConfiguration;
import au.org.ala.names.ALANameSearcher;
import au.org.ala.names.lucene.LuceneClassifierSearcherConfiguration;

File index = new File("/data/lucene/linnaean-2025-1-3");
File vernacular = new File("/data/lucene/vernacular-2025-1-3");
File suggester = new File("/data/tmp/suggester-2025-1-3");
LuceneClassifierSearcherConfiguration sConfig = LuceneClassifierSearcherConfiguration.builder()
        .queryLimit(10)
        .cacheSize(20000)
        .build();
ClassificationMatcherConfiguration cConfig = ClassificationMatcherConfiguration.builder()
        .enableJmx(true)
        .statistics(true)
        .build();
this.searcher = new ALANameSearcher(index, vernacular, suggester, sConfig, cConfig);
```

The `index` and `vernacular` variables give the location of the lucene index files for
scientific name search and vernacular name search, respectively.
The `sConfig` configuration describes how the lucene indexes will be searched and
the level of accuracy and cahcing required.
These configuration values have sensible defaults if not specified and the
entire condfiguration can be null for a default configuration.
The `cConfig` configuration describes how the classification matcher will perform.
Enabling JMX and statistics will allow you to view the performance of the matcher,
while it is running, using a tool such as `jconsole`.
Again, these configuration values have sensible defaults if not specified and the
configuration can be null.

To make a search, create a *template* `ALALinnaeanClassification`, fill out the parts
of the classification where you have data and submit it to the searcher.

```java
import au.org.ala.bayesian.Match;
import au.org.ala.bayesian.MatchMeasurement;
import au.org.ala.names.ALALinnaeanClassification;

AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
classification.scientificName = "Favolus princeps";
classification.kingdom = "Fungi";
Match<AlaLinnaeanClassification, MatchMeasurement> match = this.searcher.search(classification);
if (match.isValid()) {
        ...
}
```

The resulting match returns a number of interesting bits of information:

* `isValid()` Returns true if a match has been found, false if matching has failed.
  `null` is not returned, so that problems can be listed in the issues property.
* `getActual()` The actual classification that was used in the search.
  The actual classification may be different to the provided template.
  The searcher may have to modify the template to remove incorrect information,
  the template may have information cleaned up (eg. a name-author pair in the 
  scientific name separated out into scientific name and authorship) and inferred
  information, such as soundex names, may be included.
* `getCandidiate()` The candidate classifier for the match.
  Classifiers represent information stored in the index and may contain multiple
  variants of values to match and additional information.
* `getMatch()` A classification that matches the actual classification.
* `getAcceptedCandidate()` If the match is a synonym, the classifier for the accepted value.
  If the match is not a synonym, this is the same as the candidate.
* `getAccepted()` If the match is a synonym, the classification for the accepted value.
  If the match is not a synonym, this is the same as the match.
* `getProbability()` The probability that the match matches the actual classification.
  The probability is a composite value, showing the elements of the Bayesian calculation.
  The posterior probability, the probability that the match is a correct match to the
  template given the information in the actual classification, is the match probability.
  Generally, the posterior probability should be above 0.9.
  However, there are some cases where it is impossible to pick between multiple
  classifiers and the probability will be lower.
* `getFidelity()` The fidelity is a measure of how close the actual classification
  is to the original template classification.
  If this value is 1.0 then there is no difference between the template and the actual classification.
  Lower values indicate that the search has made a number of modifications to the initial
  template to find a suitable match.
  Modifications are usually the result of the supplied data being incorrect
  (eg misspellings, out of date higher classifications, swapped values etc) and
  a lower fidelity tends to correspond to more modifications.
* `getIssues()` A set of URIs that indicate problems with the match (or information
  about clean-up and modification).
  The issues correspond to values in `AlaLinnaeanFactory` and the
  `BayesianTerm` vocabulary.
* `getMeasurement()` If configured, this will return performance information
  about the match: time taken, number of searches, etc.
  This may be null if measurement is not configured.
* `getTrace()` If requested, a detailed trace of the search and inference is returned.
  The trace can be used to debug odd-looking results.
  Beware. Traces are very large - 30k or more of JSON for a simple search and slow
  the search down considerably.

### Options

Match options can be used to control the search, turning on or off aspects of  the search.
The standard match options are:

* `normaliseTemplate` Clean up the template, canoncialise names and generally
  make the search template consistent with what is expected by the index.
  (default: true)
* `canoncialDerivations` Include derivations that are directly inferrable
  from the source data. (default: true)
  For example, a scientific name of *Acacia dealbata* imples a rank of species,
  a genus of *Acacia* and a specificEpithet of *dealbata*.
* `fuzzyDerivations` Include derivations that provide a fuzzy or soundexed way
  of matching. (default: true)
* `modifyTemplate` Allow large-scale modifications to the template classification.
  For example, higher-order matches where a genus search is used if a species search
  does not find a match. (default: true)
* `modifyConsistency` Allow the removal of potentially inconsistent information
  from a template classification.
  For example, if the scientific name is *Acacia dealbata* and the family is
  *Poaecae* try removing the family. (default: true)
* `useHints` Use hints, such as an inferred kingdom or nomenclatural code to
  disambiguate searches. (default: true)
* `measure` Measure matching performance - see `getMeasurement()` above. (default: false)
* `trace` Trace the match - see `getTrace()` above. (default: false)

The `MatchOptions.ALL` and `MatchOptions.NONE` provide default and minimal templates.
These can be modified by using the `withXXX()` methods.

Match options can be included as a search argument.
For example:

```java
MatchOptions options = MatchOptions.ALL.withFuzzyDerivations(false).withTrace(true);
Match<AlaLinnaeanClassification, MatchMeasurement> match = this.searcher.search(classification. options);
```
### Issues

There are a number of issues that can be appended to a match.
Some are informational and indicate the clean-up and normalisation of the
data supplied in the template.
Others indicate problems when searching for data.
The complete set of network-specific issues can be found in the 
[network definition](../ala-linnaean/src/main/resources/ala-linnaean.json)
(compiled into the `AlaLinnaeanFactory`)
and, for generic issues in the [Bayesian vocabulary](../bayesian-core/src/main/java/au/org/ala/vocab/BayesianTerm.java)
Some common or important issues are:

* `BayesianTerm.invalidMatch` A matching classifer could not be found.
* `BayesianTerm.illformedData` The template contains illegal or invalid data.
  This may be eliminated or corrected and the search continued.
* `AlaLinnaeanFactory.CANONICAL_NAME` The supplied scientific name, or
  author has been adjusted to make it more in line with expectations.
  This is generally not an error but an adjustment to formatting.
* `AlaLinnaeanFactory.MISSPELLED_SCIENTIFIC_NAME` The supplied name is
  not a direct match but it is close enough to an existing name to match.
  This is often just the result of slight changes in latinate endings.
  However, sometimes this will lead to a name not recognised by the index being
  mapped onto something that is recognised by the index.
* `AlaLinnaeanFactory.REMOVED_*` Indications that the matching system had to remove
  higher-order information to achieve a match.
* `AlaLinnaeanFactory.HIGHER_ORDER_MATCH` A match at the rank supplied was unsuccessful.
  If higher-order information is available, the matcher will try higher-order
  classifications, for example moving from species to genus to family, when searching
  for a matchable taxon.
* `AlaLinnaeanFactory.SYNTHETIC_MATCH` This is usually the result of multiple synonyms.
  The match has mulitple possible accepted taxa from multiple matching synonyms.
  The least upper bound of all accepted taxa is used as a synthetic accepted taxon
  for the match.
* `AlaLinnaeanFactory.UNRESOLVED_HOMONYM` A homonym in the supplied names that
  cannot be resolved by using additional information such as nomenclatural code
  or kingdom.