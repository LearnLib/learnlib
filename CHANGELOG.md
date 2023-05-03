# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [0.17.0-SNAPSHOT] - Unreleased

[Full changelog](https://github.com/LearnLib/learnlib/compare/learnlib-0.16.0...HEAD)

### Added

* Added the OSTIA passive learning algorithm, thanks to [Aleksander Mendoza-Drosik](https://github.com/aleksander-mendoza).
* Added the OML (optimal-MAT-learner) active learning algorithm, thanks to [Falk Howar](https://github.com/fhowar).
* Added a new learning algorithm for systems of procedural automata (SPAs).
* Added Moore versions of the learners `DT`, `TTT`, `LStar`, thanks to [Mohamad Bayram](https://github.com/mohbayram).

### Changed

* `PassiveLearningAlgorithm#comuteModel` did not specify whether repeated calls to the method should yield identical models. It is now explicitly left open to the respective implementation to support this behavior. `BlueFringeRPNI{DFA,Mealy}` explicitly does not support this behavior, as the internal prefix-tree acceptor is now constructed on-the-fly as samples are added via the `addSample` methods. This allows to drop the previously redundant caching of samples and reduce memory pressure. `BlueFringeEDSMDFA` and `BlueFringeMDLDFA` still have to cache the samples internally and therefore still support repeated model construction.  
* `PTA`s now read their sample inputs as `IntSeq`s
* Renamed `PassiveLearnerVariantTICase` to `PassiveLearnerVariantITCase`
* The `Resumable` semantics have changed: the returned state object no longer implements `Serializable`. We never fully supported the semantics of the interface and never intended to do so. In fact, the old approach failed miserably if any class was involved where we missed an "implements Serializable" statement. In order to prevent confusion by promising false contracts, implementing this markup interface has been removed. Serialization should now be done in user-land via one of the many external (and more optimizable) serialization frameworks such as FST, XStream, etc. See the in-tree `ResumableExample` for reference.
* The `ADT` class is no longer initialized with a `leafSplitter` but the `extendLeaf` and `splitLeaf` methods take an additional argument. This allows for a more customizable behavior.
* The `{DFA,Mealy}CacheOracle`s and the `SULCache` are no longer thread-safe because the intended pipeline of a parallel setup (as suggested by the LearnLib factory methods) consists of a single-threaded cache that delegates to parallel (non-cached) oracles. Here, the synchronization logic only adds unnecessary overhead. In case you want a shared, thread-safe cache (which was currently not conveniently possible to setup) the `learnlib-parallelism` module now contains the `ThreadSafe{DFA,Mealy,SUL}Caches` factories which allow one to construct parallel oracles (whose parameters and return types are tailored towards using our `ParallelOracleBuilders` factory) with a shared cache. See the in-tree `ParallelismExample2` for reference.
* `SymbolQueryCache` now needs to be created via the `MealyCaches` factory.
* `AbstractTTTHypothesis` has received an additional type parameter for its state type.
* Removed the (protected) `exposeInternalHypothesis` method on `AbstractAutomatonLStar`. Sub-classes should directly implement the `getHypothesisModel` method.


### Removed

* Removed (unused) `de.learnlib.datastructure.pta.pta.PropertyConflictException`, `de.learnlib.datastructure.observationtable.InvalidRowException`.


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
* The `initialPrefixes` and `initialSuffixes` methods of `AbstractExtensibleAutomatonLStar` are now `final` since these values can be provided via the constructor of the class. This allows one to simplify sub-classes.
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
