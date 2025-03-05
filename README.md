# Redesign of the ala-name-matching library [![Build Status](https://app.travis-ci.com/AtlasOfLivingAustralia/ala-name-matching-2.svg?token=t1YBnegG9jPq6jUYJY5W&branch=master)](https://app.travis-ci.com/AtlasOfLivingAustralia/ala-name-matching-2)

See [here](doc/index.md) for documentation.

**This is very much a work in progress. You have been warned**

The approach taken is to treat taxonomic information as a vector of evidence and try to
find the taxon that has the highest probability of matching all the supplied evidence.
To do this, a Bayesian network is used to identify how the various pieces of taxonomic
information interact and build a conditional probability graph based on the network.
Since handling every possible combination of pieces of evidence would result in a 
combinatorial explosion, the network is "compiled" into a graph of cause and effect,
narrowing conditional probabilities to the smallest possible set of antecendents.

## Test indexes

To use the test cases in ala-linnaean module, you will need the corresponding
Linnaean and vernacular name indexes. You can get:

* [linnaean-20230725-5.zip](https://ala-nameindexes.s3.amazonaws.com/20230725-5/linnaean-20230725-5.zip)
* [vernacular-20230725-5.zip](https://ala-nameindexes.s3.amazonaws.com/20230725-5/vernacular-20230725-5.zip)
* [location-20230725-5.zip](https://ala-nameindexes.s3.amazonaws.com/20230725-5/location-20230725-5.zip)

These need to be unzipped into `/data/lucene`

The name matching system will build a suggestion index from the other indexes  when it is first needed.
This can take some time.
If you don't want the system to build a suggestion index,
there is a pre-built one at [suggester-20230725-5.zip](https://ala-nameindexes.s3.amazonaws.com/20230725-5/suggester-20230725-5.zip)
which can be placed in `/data/tmp`.

Every time the network definition changes, the indexes change, since the underlying
inference model will have also changed.
If you get exceptions or errors, you may need a new copy of the index.

## Name Matching Libraries

### Generic Libraries

These libraries are subject-matter agnostic and can be used to build
matching systems for any domain that can be structured into a
graph of cause and effect.

* [Bayesian Core](bayesian-core/README.md)
  Core classes for describing observable properties and how they can
  be derived, stored and matched.
* [Bayesian Lucene](bayesian-lucene/README.md)
  A storage implementation using the [lucene](https://lucene.apache.org/)
  index and search system.
* [Bayseian Builder](bayesian-builder/README.md)
  Builder software that will take a Bayesian network and "compile" it into a
  set of java classes that implement the deductive framework specified by
  the network.
  Also, a generic index builder that takes source data and builds a
  store that can be used to search for matches.
* [Bayesian Maven Plugin](bayesian-maven-plugin/README.md)
  A maven plugin that allows you to embed network building and
  compilation into your maven build cycle.

### Taxonomy-Specific Libraries

These libraries are oriented towards handling  generic biological nomenclature,
wihout insisting on a specific model.
These libraries draw extensively on [Darwin Core](https://dwc.tdwg.org/terms/)
and the [Global Biodiversity Information Facility](https://www.gbif.org/)
suite of tools and software.

* [Taxonomic Tools](taxonomic-tools/README.md)
  Utility vocabularies and processing designed to handle biological taxonomy.
* [Taxonomic Tools Builder](taxonomic-tools-builder/README.md)
  Builder processing designed to complement the taxonomic tools.

### ALA-Specific Libraries

Libraries that contain the ALA-specific implementation of taxonomy matching.
There are three netorks:

* The [Linnaean](ala-linnaean/src/main/resources/ala-linnaean.json)
  network models scientific names based on the Linnaean hierarchy.
* The [Vernacular](ala-linnaean/src/main/resources/ala-vernacular.json)
  network models vernacular (common) names.
* The [Location](ala-linnaean/src/main/resources/ala-location.json)
  network models localities.


* [ALA Linnaean](ala-linnaean/README.md)
  The classes needed to implement and analyse the Linnaean and vernacular networks
  and match a search against candidates.
  A library that allows a client to build a template of known information
  about a name, search an index built by the ALA builder library and
  return a "most likely" match to a specific taxon.
  In particular, it includes more sophisticated post-processing of results
  to take care of oddities such as parent-child synonyms, misapplied names, etc.
  This is the library that an application would use to implement
  name searching.
* [ALA Linnaean Builder](ala-linnaean-builder/README.md)
  The classes needed to build name indexes for both networks.
  This also includes some useful tools for testing and analysis.
* [ALA Taxonomic Tools](ala-taxonomic-tools/README.md)
  Useful tools for analysis and comparison
* [ALA Distribution](ala-distribution/README.md)
  A packaged distribution of the name index builders and tools.
