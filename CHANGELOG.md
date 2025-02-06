# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [0.19.0-SNAPSHOT] - Unreleased

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.18.0...HEAD)


## [0.18.0] - 2025-02-06

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.17.0...learnlib-0.18.0)

### Added

* LearnLib now supports JPMS modules. All artifacts now provide a `module-info` descriptor except of the distribution artifacts (for Maven-less environments) which only provide an `Automatic-Module-Name` due to non-modular dependencies. Note that while this is a Java 9+ feature, LearnLib still supports Java 8 byte code for the remaining class files.
* Added the L# active learning algorithm (thanks to [Tiago Ferreira](https://github.com/tiferrei)).
* The `ADTLearner` has been refactored to no longer use the (now-removed) `SymbolQueryOracle` but a new `AdaptiveMembershipOracle` instead which supports answering queries in parallel (thanks to [Leon Vitorovic](https://github.com/leonthalee)).
* The `ADTLearner` can now be parameterized in its counterexample analysis method.
* Added an `InterningMembershipOracle` (including refinements) to the `learnlib-cache` artifact that interns query responses to reduce memory consumption of large data structures. This exports the internal concepts of the DHC learner (which no longer interns query responses automatically).
* `StaticParallelOracleBuilder` now supports custom executor services.

### Changed

* The JPMS support introduces several changes:
  * You now require at least a JDK 11 to build LearnLib.
  * We use modules to better structure the aggregated JavaDoc. Since there exist breaking changes between Java 8 and Java 9 regarding documentation (see package-list vs. element-list), you can no longer link against the LearnLib documentation on JDK 8 builds.
  * Split packages had to be refactored.
    * The `de.learnlib.oracle.parallelism` interfaces in the `learnlib-api` artifact have been moved to the `de.learnlib.oracle` package.
    * The `ThreadSafe` caches have been moved from the `learnlib-parallelism` artifact to the `learnlib-cache` artifact.
    * The `GrowingAlphabet` and `Resumable` tests from the `learnlib-learner-it-support` artifact been moved to the `learnlib-test-support` artifact.
    * The `OTUtils` class no longer provides the `displayHTMLInBrowser` methods in order to not depend on `java.desktop`. If you relied on this functionality, use the `writeHTMLToFile` methods instead and call `Desktop.getDesktop().open(file.toURI())` yourself.
    * The classes in the `learnlib-learning-examples` artifact have their package renamed to `de.learnlib.testsupport.example`.
* The `AbstractVisualizationTest` has been refactored into the `VisualizationUtils` factory.
* Various counters (especially `*Counter*SUL`s) have been streamlined. In most cases there now exists a single counter that tracks multiple properties.
* The `ReuseOracleBuilder` and `ReuseTreeBuilder` classes are now auto-generated and therefore reside in the respective packages of their previously enclosing classes.
* The `TTTLearnerMealy#createTransition` method no longer queries for its transition output directly, but instead requires a call to `initTransitions` now.
* With the removal of the `learnlib-annotation-processor` artifact (see below), the `learnlib-build-config` artifact is now part of the `de.learnlib` group again.
* The `learnlib-datastructure-ot`, `learnlib-datastructure-dt`, `learnlib-datastructure-list`, and `learnlib-datastructure-pta` artifacts have been merged into a new `learnlib-datastructures` artifact.
* The `learnlib-oml` artifact (including its packages and class names) has been renamed to `learnlib-lambda`.
* Switched to [AutomataLib 0.12.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.12.0).

### Removed

* The `de.learnlib.tooling:learnlib-annotation-processor` artifact has been dropped. The functionality has been moved to a [standalone project](https://github.com/LearnLib/build-tools).
* The `de.learnlib:learnlib-rpni-edsm` and `de.learnlib:learnlib-rpni-mdl` artifacts have been dropped. The code has been merged with the `de.learnlib:learnlib-rpni` artifact.
* `MQUtil` has been stripped of unused methods. Especially the `query` method can be simulated by the respective oracles themselves.
* `PropertyOracle`s can no longer set a property. This value is now immutable and must be provided during instantiation. Previously, the internal state wasn't updated accordingly if a property was overridden.
* `SymbolQueryOracle`s (and related code such as the respective caches, counters, etc.) have been removed without replacement. Equivalent functionality on the basis of the new `AdaptiveMembershipOracle`s is available instead.

### Fixed

* Improved query batching of `TTT` learner (both the regular and visibly push-down version).


## [0.17.0] - 2023-11-15

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.16.0...learnlib-0.17.0)

### Added

* Migrated the AAAR algorithm from the old closed-source LearnLib (thanks to [Markus Frohme](https://github.com/mtf90)).
* Added Moore versions of the `OP`, `TTT`, and `LStar` learners (thanks to [Mohamad Bayram](https://github.com/mohbayram)).
* Added the OML (optimal-MAT-learner) active learning algorithm (thanks to [Falk Howar](https://github.com/fhowar)).
* Added the OSTIA passive learning algorithm (thanks to [Aleksander Mendoza-Drosik](https://github.com/aleksander-mendoza)).
* The `RPNI` learner now supports `MooreMachine`s (thanks to [Markus Frohme](https://github.com/mtf90)).
* Added new learning algorithms for procedural systems such as SPAs, SBAs, and SPMMs (thanks to [Markus Frohme](https://github.com/mtf90)).

### Changed

* Refactorings
  * Many LearnLib packages have been refactored from plural-based keywords to singular-based keywords. Some examples are
    * renamed all learning algorithm packages from `de.learnlib.algorithms.*` to `de.learnlib.algorithm.*`.
    * renamed `de.learnlib.counterexamples.*` to `de.learnlib.counterexample.*`.
    * renamed `de.learnlib.drivers.*` to `de.learnlib.driver.*`.
    * renamed `de.learnlib.util.statistics.*` to `de.learnlib.util.statistic.*`.
    * etc.
    
    While this may cause some refactoring, it should only affect import statements as the names of most classes remain identical.
  * Some actual re-namings concern
    * All code concerning visibly push-down automata now uses the "vpa" acronym (previously "vpda"). This includes package names, class names and (Maven) module names.
    * The "discrimination-tree" learner has been renamed to "observation-pack". This includes classes (`DTLearnerDFA` -> `OPLearnerDFA`, etc.), package names, and Maven modules. The same refactoring happened for the VPA-based version of the learner.
    * The `learnlib-acex` Maven module has been merged with the `learnlib-counterexamples` module.
    * Classes in the  `learnlib-api` have been moved from `de.learnlib.api` to `de.learnlib`.
    * Refactored the package `de.learnlib.datastructure.pta.pta.*` to `de.learnlib.datastructure.pta.*`.
    * Refactored the package `de.learnlib.driver.util.*` to `de.learnlib.driver.simulator.*`.
    * Moved classes from the package `de.learnlib.mapper.api.*` to `de.learnlib.sul.*`.
    * Renamed `PassiveLearnerVariantTICase` to `PassiveLearnerVariantITCase`.
* `AbstractTTTHypothesis` has received an additional type parameter for its state type.
* `AutomatonOracle#accepts` no longer has a `length` parameter. Provide a correctly sized `input` iterable instead.
* Classes revolving around the `ContextExecutableInputSUL` have been moved from the `learnlib-mapper` module to the `learnlib-drivers-basic` module.
* The `CounterOracle` and `JointCounterOracle` have been merged. Now there only exists a single `CounterOracle` that counts both the number of queries and the number of symbols therein.
* The `{DFA,Mealy}CacheOracle`s and the `SULCache` are no longer thread-safe because the intended pipeline of a parallel setup (as suggested by the LearnLib factory methods) consists of a single-threaded cache that delegates to parallel (non-cached) oracles. Here, the synchronization logic only adds unnecessary overhead. In case you want a shared, thread-safe cache (which was currently not possible to set up conveniently) the `learnlib-parallelism` module now contains the `ThreadSafe{DFA,Mealy,SUL}Caches` factories which allow one to construct parallel oracles (whose parameters and return types are tailored towards using our `ParallelOracleBuilders` factory) with a shared cache. See the in-tree `ParallelismExample2` for reference.
* `PTA`s now read their sample inputs as `IntSeq`s.
* `PassiveLearningAlgorithm#computeModel` did not specify whether repeated calls to the method should yield identical models. It is now explicitly left open to the respective implementation to support this behavior. `BlueFringeRPNI{DFA,Mealy,Moore}` explicitly does not support this behavior, as the internal prefix-tree acceptor is now constructed on-the-fly as samples are added via the `addSample` methods. This allows to drop the previously redundant caching of samples and reduce memory pressure. `BlueFringeEDSMDFA` and `BlueFringeMDLDFA` still have to cache the samples internally and therefore still support repeated model construction.
* The `Resumable` semantics have changed: the returned state object no longer implements `Serializable`. We never fully supported the semantics of the interface and never intended to do so. In fact, the old approach failed miserably if any class was involved where we missed an "implements Serializable" statement. In order to prevent confusion by promising false contracts, implementing this markup interface has been removed. Serialization should now be done in user-land via one of the many external (and more optimizable) serialization frameworks such as FST, XStream, etc. See the in-tree `ResumableExample` for reference.
* The `ADT` class is no longer initialized with a `leafSplitter` but the `extendLeaf` and `splitLeaf` methods take an additional argument. This allows for a more customizable behavior.
* The automaton-specific `SimulatorOracle`s are now generated automatically and therefore reside in the package `de.learnlib.oracle.membership` rather that being an inner-class of the `SimulatorOracle`.
* `SymbolQueryCache` now needs to be created via the `MealyCaches` factory.
* `SimplePOJOTestDriver` no longer uses a mapper to suppress `SULException`s but instead operates directly on the POJO with simplified inputs/outputs and propagates any exceptions thrown. To complement this change, the old `SimplePOJODataMapper` has been renamed to `SimplePOJOExceptionMapper` and only deals with mapping exceptions now. The old behavior can be restored by combining the two classes manually via `SULMappers#apply`.
* Switched to [AutomataLib 0.11.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.11.0).

### Removed

* Removed (unused) `de.learnlib.datastructure.pta.pta.PropertyConflictException`, `de.learnlib.datastructure.observationtable.InvalidRowException`.
* Removed the (protected) `exposeInternalHypothesis` method on `AbstractAutomatonLStar`. Subclasses should directly implement the `getHypothesisModel` method.
* Removed the `EquivalenceQueries` factory. All provided equivalence checkers are available via public constructors which allow for more flexible parameterization.
* Removed the `Filter` interface and `FilterChain` class. Instantiating the filters that are required for constructing a filter chain already requires setting the delegate oracles. Therefore, the only effect of this interface is that the attributes of the filters cannot be final.
* Removed `LearnLogger`. All code was migrated to use the native SLF4j facade and now uses the markers provided in the `Category` class to accentuate the different log messages and provide client code with a means to handle the different log messages (similar to the previous purpose-specific log methods).


### Fixed

* Fixed a bug when adding new alphabet symbols to LStar-based learners which use a counterexample handler that requires consistency checks.


## [0.16.0](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.16.0) - 2020-10-12

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.15.0...learnlib-0.16.0)

### Added

* A `StateLocalInputSULSymbolQueryOracle` to wrap a `StateLocalInputSUL`in a `SymbolQueryOracle` (e.g. to learn partial systems with the ADT learner).
* Added an example for parallel setups (`ParallelismExample1`).

### Changed

* The `SULSymbolQueryOracle` now better handles the `pre` and `post` cycles of a `SUL` (e.g. calls to the `reset` method now allow to close the embedded `SUL` from outside).
* Reworked parallel oracles
  * Several `SULOracle` variants are no longer thread-safe. This reduces overhead for scenarios where no parallelism is required.
  * The `ParallelOracleBuilders` factory now offers builder methods for `SUL`s, `ObservableSUL`s, `StateLocalInputSUL`s, `MembershipOracles`s and `OmegaMembershipOracle`s to allow an easy (and correct) construction of parallel setups given one of the mentioned implementations.
* Refactored the following packages/classes:
  * `de.learnlib.oracle.parallelism.ParallelOracleInterruptedException` -> `de.learnlib.api.oracle.parallelism.BatchInterruptedException`
* The `initialPrefixes` and `initialSuffixes` methods of `AbstractExtensibleAutomatonLStar` are now `final` since these values can be provided via the constructor of the class. This allows one to simplify subclasses.
* Updated to [AutomataLib 0.10.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.10.0)

### Removed

* Removed the `learnlib.queries.parallel.threshold` property. Learning setups that want to use parallelism now need to explicitly setup parallel oracles.
* Removed `MQUtil#answerQueries{Auto,Parallel}` and `MQUtil#answerOmegaQueries{Auto,Parallel}`.
* `LassoOracle#isOmegaCounterExample(boolean)` has been removed. This decision can be directly integrated into the `#findCounterExample` method which has more information available.

### Fixed

* Fixed a bug where NL* would create non-canonical hypotheses ([#70](https://github.com/LearnLib/learnlib/issues/70), thanks to [Joshua Moerman](https://github.com/Jaxan))


## [0.15.0](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.15.0) - 2020-02-06

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.14.0...learnlib-0.15.0)

### Changed

* The `{DFA,Mealy,}W{p,}MethodEQOracle(MembershipOracle, int, int)` constructor no longer interprets its second `int` parameter as the batch size, but as an estimate for the expected SUL size. In order to explicitly set the batch size of the oracle, use the `{DFA,Mealy,}W{p,}MethodEQOracle(MembershipOracle, int, int, int)` constructor. Now, the two parameters `lookahead` and `expectedSize` will determine the length of the *middle part* via `Math.max(lookahead, expectedSize - hypothesis.size())`. This allows to dynamically adjust the length of the *middle part* throughout the learning process. See [LearnLib/automatalib#32](https://github.com/LearnLib/automatalib/issues/32).
* Several DFA/Mealy specific (oracle) subclasses are now automatically generated. As a result they are no longer an inner class but an independent top-level class. This requires to update the import statements.
* JSR305 annotations have been replaced with checker-framework annotations.
  * LearnLib (incl. AutomataLib) now follows checker-framework's convention that (non-annotated) types are usually considered non-null unless explicitly annotated with `@Nullable`.
  * LearnLib (incl. AutomataLib) no longer has a (runtime-) dependency on JSR305 (and other `javax.*`) annotations or includes them in the distribution artifact. This now makes LearnLib (incl. AutomataLib) compliant with [Oracle's binary code license](https://www.oracle.com/downloads/licenses/binary-code-license.html) and allows LearnLib (incl. AutomataLib) artifacts as-is to be bundled in binary distributions with Oracle's JDKs/JREs.
* A lot of code for inferring partial Mealy machines (esp. `PartialLStarMealy` and `PartialObservationTable`) has been removed/refactored. The concept of state local inputs is now implemented as a SUL filter and introduces a special `StateLocalInputSULOracle` which early-answers queries that would traverse unavailable inputs with a previously specified symbol. This way, queries that would traverse undefined input symbols still won't be executed on the SUL but the SUL appears as a 'total' Mealy system to the learner, allowing one to use every currently existing Mealy learner as-is. See the in-tree examples for more information.
* `SULCache` no longer implements `MembershipOracle`.
* Updated to [AutomataLib 0.9.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.9.0)

### Removed

* As a remainder of its initial implementation, the `TTTEventListener` (and the corresponding event feature in the TTT algorithm) has been removed due to the lack of usage.


## [0.14.0](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.14.0) - 2019-02-18

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.13.1...learnlib-0.14.0)

### Added

* Added support for black-box-checking (thanks to [Jeroen Meijer](https://github.com/Meijuh)).
* Added support for learning partial Mealy Machines with state-local inputs via L* (thanks to [Maren Geske](https://github.com/mgeske)).
* Added support for resumable caches.
* `DynamicParallelOracle`s can now be constructed from a collection of independent oracles.
* Support for Java 11. **Note:** LearnLib/AutomataLib still targets Java 8, and thus needs classes provided by this environment (specifically: annotations from `javax.annotation`). If you plan to use LearnLib/AutomataLib in a Java 11+ environment, make sure to provide these classes. They are not shipped with LearnLib/AutomataLib.

### Changed

* Refactored the following packages/classes:
  * `de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet` -> `net.automatalib.SupportsGrowingAlphabet`
  * `de.learnlib.api.algorithm.feature.ResumableLearner` -> `de.learnlib.api.Resumable`
* Some runtime properties for dynamically configuring LearnLib have been renamed. There now exists the `LearnLibProperty` enum as a single reference point for all available properties.
* The node iterators for discrimination trees are now hidden behind the `DiscriminationTreeIterators` factory.
* Parallel Oracles: 
  * The `withDefault*` methods have been removed from the `{Dynamic,Static}PrallelOracleBuilders`. If needed, use the regular `with*` methods and supply the public default values from `{Dynamic,Static}PrallelOracle`.
  * The `new*ParallelOracle` methods from the `ParallelOracleBuilders` factory no longer interpret a single membership oracle parameter as a supplier to a shared oracle, but rather as a single oracle (and thus return a builder for a parallel oracle with fixed pool size).
* Adding new symbols to learning algorithms (via the `SupportsGrowingAlphabet` interface) now requires the learner to be initialized with a `GrowingAlphabet` instance. This is to make sure that the user has full control over which alphabet instance should be used instead of LearnLib making decisions on behalf of the user.
* Discrimination-Tree based Learners (DT, KV, TTT) now batch queries whenever possible, thus allowing to fully utilize parallel oracles.
* Also, see the [changes in AutomataLib](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.8.0)

### Fixed

* Several bugs detected by our ongoing efforts to write tests.


## [0.13.1](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.13.1) - 2018-05-11

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.13.0...learnlib-0.13.1)

### Fixed

* Fixed an out-of-bounds error in a cache implementation
* Fixed visibility issues with exportable classes used for the `ResumableLearner` interface
* Fixed an issue when adding a new symbol to a learner and the initial alphabet was already an instance of `GrowableAlphabet`
* General consolidations (typos, wrong documentation, etc.)


## [0.13.0](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.13.0) - 2018-02-08

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.12.0...learnlib-0.13.0)

### Added

* Added randomized version of W(p)-Method based equivalence oracles (see [#40](https://github.com/LearnLib/learnlib/pull/40))
* Added the ADT (adaptive distinguishing tree) active learning algorithm
* Added two active learning algorithms for visibly pushdown languages.
* Added the RPNI (regular positive-negative inference) passive learning algorithm, including EDSM (evidence-driven state merging) and MDL (minimum description length) variants.
* Many active learning algorithms now support adding additional alphabet symbols after initial instantiation/starting of the learning process.
* Added support for suspending the learning process to a savable / serializable state. The learning process may be resumed from this state at a later point in time.
* Added the `AbstractTestWordEQOracle` class, which allows one to implement custom equivalence oracles solely based on lazy (stream-based) test-word generation. Existing equivalence oracles (as far as possible) have been reworked to extend this class and thus profit from its built-in laziness and batch (parallelization) support.

### Changed

* Refactored the Maven artifact and Java package structure. Have a look at the [List of LearnLib Artifacts](https://github.com/LearnLib/learnlib/wiki/List-of-LearnLib-Artifacts) for an updated overview of available artifacts. In general, no functionality should have been removed (except of code marked with `@Deprecated`). The easiest way to migrate your code to the new version is probably by using the Auto-Import feature of your IDE of choice.

  The non-trivial refactorings include:
  * API methods no longer use wildcards in generic return parameters. This allows your code to not having to deal with them.
  * [Changes to AutomataLib](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.7.0).

* Replaced `System.out` and JUL logging, with calls to a SLF4j facade.
* Code improvements due to employment of several static code-analysis plugins (findbugs, checkstyle, PMD, etc.) as well as setting up continuous integration at [Travis CI](https://travis-ci.org/LearnLib/learnlib).

### Fixed

* Several bugs detected either by our newly employed static code-analysis toolchain or by our ongoing efforts to write tests.


## [0.12.0](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.12.0) - 2015-06-04

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.11.2...learnlib-0.12.0)


## [0.11.2](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.11.2) - 2015-04-26

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.11.1...learnlib-0.11.2)


## [0.11.1](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.11.1) - 2015-01-16

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.11.0...learnlib-0.11.1)


## [0.11.0](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.11.0) - 2015-01-13

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.10.1...learnlib-0.11.0)


## [0.10.1](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.10.1) - 2014-06-08

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.10.0...learnlib-0.10.1)


## [0.10.0](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.10.0) - 2014-04-16

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.9.1...learnlib-0.10.0)


## [0.9.1-ase2013-tutorial-r1](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.9.1-ase2013-tutorial-r1) - 2013-12-13


## [0.9.1](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.9.1) - 2013-11-07

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.9.0...learnlib-0.9.1)


## [0.9.1-ase2013-tutorial](https://github.com/LearnLib/learnlib/releases/tag/0.9.1-ase2013-tutorial) - 2013-11-06


## [0.9.0](https://github.com/LearnLib/learnlib/releases/tag/learnlib-0.9.0) - 2013-06-25

[Full changelog](https://github.com/LearnLib/learnlib/compare/86fce036ed7c659aaf649ad4a772af1341f80c61...learnlib-0.9.0)
