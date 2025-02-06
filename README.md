# LearnLib

[![CI](https://github.com/LearnLib/learnlib/actions/workflows/ci.yml/badge.svg)](https://github.com/LearnLib/learnlib/actions/workflows/ci.yml)
[![Coverage](https://coveralls.io/repos/github/LearnLib/learnlib/badge.svg?branch=develop)](https://coveralls.io/github/LearnLib/learnlib?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.learnlib/learnlib-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.learnlib/learnlib-parent)

LearnLib is a free, open-source ([Apache License, v2.0][1]) Java library for automata learning algorithms.


## About

LearnLib is mainly developed at [TU Dortmund University, Germany][2].
Its original purpose is to provide a framework for research on automata learning algorithms as well as for their application in practice.

The public version is a re-implemented version of the former closed-source version of LearnLib.
While certain features have been stripped for improved modularity, development has since then extended the features offered.
Currently, the following learning algorithms with respective target models are supported:


| Algorithm (active)  | Target models               |     | Algorithm (passive)   | Models                |
|---------------------|-----------------------------|-----|-----------------------|-----------------------|
| AAAR                | `DFA` `Mealy` `Moore`       |     | OSTIA                 | `SST`                 |
| ADT                 | `Mealy`                     |     | RPNI (incl. variants) | `DFA` `Mealy` `Moore` |
| DHC                 | `Mealy`                     |     |                       |                       |
| Kearns & Vazirani   | `DFA` `Mealy`               |     |                       |                       |
| Lambda              | `DFA` `Mealy`               |     |                       |                       |
| L#                  | `Mealy`                     |     |                       |                       |
| L* (incl. variants) | `DFA` `Mealy` `Moore`       |     |                       |                       |
| NL*                 | `NFA`                       |     |                       |                       |
| Observation Pack    | `DFA` `Mealy` `Moore` `VPA` |     |                       |                       |
| Procedural          | `SPA` `SBA` `SPMM`          |     |                       |                       |
| TTT                 | `DFA` `Mealy` `Moore` `VPA` |     |                       |                       |

Additionally, LearnLib offers a variety of tools to ease the practical application of automata learning on real-world systems.
This includes drivers and mappers for interfacing software systems with the LearnLib API as well as caches and parallelization for improving the overall performance of the learning setup.
Also, more nuanced setups such as Black-Box-Checking (via [LTSmin][ltsmin]) or inferring partial machines are possible.

While we strive to deliver code at a high quality, please note that there exist parts of the library that still need thorough testing.
Contributions -- whether it is in the form of new features, better documentation or tests -- are welcome.

## Build Instructions

For simply using LearnLib you may use the Maven artifacts which are available in the [Maven Central repository][maven-central].
It is also possible to download a bundled [distribution artifact][maven-central-distr] if you want to use LearnLib without Maven support.
Note that LearnLib requires Java 11 (or newer) to build but still supports Java 8 at runtime.

#### Building development versions

If you intend to use development versions of LearnLib, you can either use the deployed SNAPSHOT artifacts from the continuous integration server (see [Using Development Versions](https://github.com/LearnLib/learnlib/wiki/Using-Development-Versions)), or build them yourself.
Simply clone the development branch of the repository

```
git clone -b develop --single-branch https://github.com/LearnLib/learnlib.git
```

and run a single `mvn clean install`.
This will build all the required maven artifacts and will install them in your local Maven repository so that you can reference them in other projects.

If you plan to use a development version of LearnLib in an environment where no Maven support is available, simply run `mvn clean package -Pbundles`.
The respective JARs are then available under `distribution/target/bundles`.

**Note: Development versions of LearnLib usually depend on development versions of [AutomataLib][7].**
For building development versions of AutomataLib, see the corresponding documentation on the project's README.

#### Developing LearnLib

For developing the code base of LearnLib it is suggested to use one of the major Java IDEs which come with out-of-the-box Maven support.

* For [IntelliJ IDEA][intellij]:
  1. Select `File` -> `New` -> `Project from existing sources` and select the folder containing the development checkout.
  1. Choose "Import Project from external model", select "Maven" and click `Create`.
  1. In order to have both development versions of AutomataLib and LearnLib available at once, continue to import AutomataLib as documented in the project's README, but choose `File` -> `New` -> `Module from existing sources` as the first step.

* For [Eclipse][eclipse]:
  1. **Note**: LearnLib uses annotation processing on several occasions throughout the build process.
  This is usually handled correctly by Maven.
  However, for Eclipse, you may need to manually enable annotation processing under `Preferences` -> `Maven` -> `Annotation Processing`.
  1. Select `File` -> `Import...` and select "Existing Maven Projects".
  1. Select the folder containing the development checkout as the root directory and click `Finish`.
  1. In order to have both development versions of AutomataLib and LearnLib available at once, continue to import AutomataLib as documented in the project's README.


## Documentation

* **Maven Project Site:** [latest release](https://learnlib.github.io/learnlib/maven-site/latest/) | [older versions](https://learnlib.github.io/learnlib/maven-site/)
* **API Documentation:** [latest release](https://learnlib.github.io/learnlib/maven-site/latest/apidocs/) | [older versions](https://learnlib.github.io/learnlib/maven-site/)


## Questions?

If you have any questions regarding the usage of LearnLib or if you want to discuss new and exciting ideas for future contributions, feel free to use the [Discussions](https://github.com/LearnLib/learnlib/discussions) page to get in touch with the LearnLib community.


## Maintainers

* [Markus Frohme][6] (2017 - )
* [Falk Howar][5] (2013 - )
* [Malte Isberner][4] (2013 - 2015)


[1]: http://www.apache.org/licenses/LICENSE-2.0
[2]: https://cs.tu-dortmund.de
[3]: https://learnlib.de
[4]: https://github.com/misberner
[5]: https://github.com/fhowar
[6]: https://github.com/mtf90
[7]: https://github.com/LearnLib/automatalib

[maven-central]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.learnlib%22
[maven-central-distr]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.learnlib.distribution%22
[intellij]: https://www.jetbrains.com/idea/
[eclipse]: https://www.eclipse.org/
[ltsmin]: https://ltsmin.utwente.nl/
