# LearnLib

[![Build Status](https://travis-ci.org/LearnLib/learnlib.svg?branch=develop)](https://travis-ci.org/LearnLib/learnlib)
[![Coverage Status](https://coveralls.io/repos/github/LearnLib/learnlib/badge.svg?branch=develop)](https://coveralls.io/github/LearnLib/learnlib?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.learnlib/learnlib-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.learnlib/learnlib-parent)

LearnLib is a free, open source ([Apache License, v2.0][1]) Java library for automata learning algorithms.


## About

LearnLib is mainly developed at the [Dortmund University of Technology, Germany][2].
Its original purpose is to provide a framework for research on automata learning algorithms as well as for their application in practice.

The public version is a re-implemented version of the former closed-source version of LearnLib.
While certain features have been stripped for improved modularity, development has since then extended the features offered.
Currently the following learning algorithms with respective target models are supported:


Algorithm (active)  | Target models        || Algorithm (passive) | Models
--- | --- | --- | --- | ---
ADT                 | `Mealy`              || RPNI                | `DFA` `Mealy`
DHC                 | `Mealy`              || RPNI (EDSM)         | `DFA`
Discrimination Tree | `DFA` `Mealy` `VPDA` || RPNI (MDL)          | `DFA`
Kearns & Vazirani   | `DFA` `Mealy`
L* (incl. variants) | `DFA` `Mealy`
NL*                 | `NFA`
TTT                 | `DFA` `Mealy` `VPDA`


Additionally, LearnLib offers a variety of tools to ease the practical application of automata learning on real-world systems.
This includes drivers and mappers for interfacing software systems with the LearnLib API as well as caches and parallelization for improving the overall performance of the learning setup.

While we strive to deliver code at a high quality, please note, that there exist parts of the library that still need thorough testing.
Contributions -- whether it is in the form of new features, better documentation or tests -- are welcome.

## Build Instructions

For simply using LearnLib, you may use the Maven artifacts which are available in the [Maven Central repository][maven-central].
It is also possible to download a bundled [distribution artifact][maven-central-distr], if you want to use LearnLib without Maven support.
Note, that LearnLib requires Java 8.

#### Building development versions

If you intend to use development versions of LearnLib, you can either use the deployed SNAPSHOT artifacts from the continuous integration server (see [Using Development Versions](https://github.com/LearnLib/learnlib/wiki/Using-Development-Versions)), or build them yourself.
Simply clone the development branch of the repository

```
git clone -b develop --single-branch https://github.com/LearnLib/learnlib.git
```

and run a single `mvn clean install`.
This will build all the required maven artifacts and will install them in your local Maven repository, so that you can reference them in other projects.

If you plan to use a development version of LearnLib in an environment where no Maven support is available, simply run `mvn clean package -Pbundles`.
The respective JARs are then available under `distribution/target/bundles`.

**Note: Development versions of LearnLib usually depend on development versions of [AutomataLib][7].**
For building development versions of AutomataLib, see the corresponding documentation on the project's README.

#### Developing LearnLib

For developing the code base of LearnLib, it is suggested to use one of the major Java IDEs, which come with out-of-the-box Maven support.

* For [IntelliJ IDEA][intellij]:
  1. Select `File` -> `New` -> `Project from existing sources` and select the folder containing the development checkout.
  1. Choose "Import Project from external model", select "Maven" and click `Next`.
  1. Configure the project to your liking, but make sure to check "Import Maven projects automatically" and have "Generated sources folders" set to "Detect automatically".
  1. Click `Next` until the project is imported (no Maven profile needs to be selected).
  1. In order to have both development versions of AutomataLib and LearnLib available at once, continue to import AutomataLib as documented in the project's README, but choose `File` -> `New` -> `Module from existing sources` as the first step.

* For [Eclipse][eclipse]:
  1. **Note**: LearnLib uses annotation processing on several occasions throughout the build process.
  This is usually handled correctly by Maven, however, for Eclipse you need to install the [m2e-apt-plugin](https://marketplace.eclipse.org/content/m2e-apt) and activate annotation processing afterwards (see the [issue #32](https://github.com/LearnLib/learnlib/issues/32)).
  1. Select `File` -> `Import...` and select "Existing Maven Projects".
  1. Select the folder containing the development checkout as the root directory and click `Finish`.
  1. In order to have both development versions of AutomataLib and LearnLib available at once, continue to import AutomataLib as documented in the project's README.


## Documentation

* **Maven Project Site:** [latest release](http://learnlib.github.io/learnlib/maven-site/latest/) | [older versions](http://learnlib.github.io/learnlib/maven-site/)
* **API Documentation:** [latest release](http://learnlib.github.io/learnlib/maven-site/latest/apidocs/) | [older versions](http://learnlib.github.io/learnlib/maven-site/)


## Mailing Lists

  * [Q&A @ Google Groups][learnlib-qa] -- General questions regarding the usage of LearnLib.
  * [Discussion @ Google Groups][learnlib-discussion] -- Discussions about the internals of LearnLib.
  * [Internal (private) @ Google Groups][learnlib-internal] -- Discussions about future development plans.


## Maintainers

* [Markus Frohme][6] (2017 - )
* [Falk Howar][5] (2013 - )
* [Malte Isberner][4] (2013 - 2015)


[1]: http://www.apache.org/licenses/LICENSE-2.0
[2]: http://www.cs.tu-dortmund.de
[3]: http://www.learnlib.de
[4]: https://github.com/misberner
[5]: https://github.com/fhowar
[6]: https://github.com/mtf90
[7]: https://github.com/LearnLib/automatalib

[learnlib-qa]: https://groups.google.com/d/forum/learnlib-qa
[learnlib-discussion]: https://groups.google.com/d/forum/learnlib-discussion
[learnlib-internal]: https://groups.google.com/d/forum/learnlib-internal

[maven-central]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.learnlib%22
[maven-central-distr]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.learnlib.distribution%22
[intellij]: https://www.jetbrains.com/idea/
[eclipse]: https://www.eclipse.org/
