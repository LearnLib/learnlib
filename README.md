LearnLib
===========
[![Build Status](https://travis-ci.org/LearnLib/learnlib.svg?branch=develop)](https://travis-ci.org/LearnLib/learnlib)
[![Coverage Status](https://coveralls.io/repos/github/LearnLib/learnlib/badge.svg?branch=develop)](https://coveralls.io/github/LearnLib/learnlib?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.learnlib/learnlib-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.learnlib/learnlib-parent)

LearnLib is a free, open source ([Apache License, v2.0][1]) Java library for automata learning algorithms.

About
-----
LearnLib is mainly developed at the [Dortmund University of Technology, Germany][2]. Its original purpose is to
provide a framework for research on automata learning algorithms as well as for their application in practice.

Please note that the development of LearnLib is still in a very early stage. The public version is a re-implemented
version of the former closed-source version of LearnLib. It does not yet have the complete functionality of the
original version. Features will be added and made available as time permits. On the other hand, everyone is
invited to contribute.

Also please note that many parts of the library have not yet been thoroughly tested.

Build Instructions
------------------
Following are build instructions for [IntelliJ IDEA](https://www.jetbrains.com/idea/) with JDK 1.8. You need the automatalib library to build LearnLib. You can clone [automatalib][7] and follow the instructions [here](https://github.com/LearnLib/automatalib/blob/develop/README.md) to build it.

1- Start IntelliJ. Go to File -> New -> Project from existing sources -> select the project folder.  
2- Choose "Import Project from external model" -> Maven -> Next.  
3- Check "Import Maven projects automatically". Keep the rest as default and click "Next" until the project is imported.  
4- Build the porject. If build fails, then it is probably due to automatalib library missing. Go to File -> Project Structure -> Libraries, and remove all Maven automatalib dependenceis; then click Add (the plus sign) -> Java -> browse to automatalib jar -> select all modules and click OK. Now build again and it should work.  
5- To produce the jar file, go to File -> Project Structure -> Artifacts -> Add (the plus sign) -> JAR ->  From modules with dependenceis -> OK -> On the right hand side, check box "Include in project build".  
6- Now build the project and it should produce a new directory "out" containing the JAR artifact.  

Maintainers
-----------
* [Markus Frohme][6] (2017 - )
* [Falk Howar][5] (2013 - )
* [Malte Isberner][4] (2013 - 2015)

Resources
---------
* **[LearnLib Web-page][3]**
* **Maven Project Site:** [snapshot](http://learnlib.github.io/learnlib/maven-site/latest-snapshot/) | [latest release](http://learnlib.github.io/learnlib/maven-site/latest-release/)
* **API Documentation:** [snapshot](http://learnlib.github.io/learnlib/maven-site/latest-snapshot/apidocs/) | [latest release](http://learnlib.github.io/learnlib/maven-site/latest-release/apidocs/)

[1]: http://www.apache.org/licenses/LICENSE-2.0
[2]: http://www.cs.tu-dortmund.de
[3]: http://www.learnlib.de
[4]: https://github.com/misberner
[5]: https://github.com/fhowar
[6]: https://github.com/mtf90
[7]: https://github.com/LearnLib/automatalib
