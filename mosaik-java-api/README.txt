Martin Schvarcbacher comment
===================
This is the Mosaik Java API taken from https://bitbucket.org/mosaik/mosaik-api-java
The original repository is using ANT, the content of this folder is remade to use Maven 


Mosaik API for Java
===================

This is an implementation of the mosaik API for simulators written in Java.
It hides all the messaging and networking related stuff and provides a simple
base class that you can implement.


Setup
-----

Clone or download this repository onto you computer.

Install Apache Ant 1.9 or higher (http://ant.apache.org/) and build a
distribution::

   $ cd mosaik-api-java
   $ ant

As soon as Ant is finished, you can copy the *.jar* files from the *dist/*
folder into your project.


Testing
-------

To run the test cases, you will need Apache Ant 1.9 or higher, Python 3 and
the virtualenv package for your current Python environment installed.
You can then, if you are working on Linux or OS X, run the tests via::

   $ ./runtests.sh [-s]

Or, if you are working on a Windows machine::

   $ runtests.bat "path-to-Python-3-executable" [-s]

This will build the API and the test simulators using Ant, create a new
Python 3 virtualenv with all required packages installed, let Pytest run the
test cases, and finish with some cleanup. You can optionally pass the
parameter -s to activate Pytests console output for additional debug
information.


Documentation
-------------

You can find general information about the API in mosaik's docs
(https://mosaik.readthedocs.org/en/latest/mosaik-api/). Also, all public
classes and methods also have docstrings (there is no pre-built Java doc
yet).

The *tests/* directory contains an example simulator (ExampleSim) and an
example control strategy (ExampleMAS) that may give you an idea of what to do
and how things work.


Support
-------

If you need help or want to give feedback, you are welcome to post something
to our mailing list (https://mosaik.offis.de/mailinglist). You can also
browse the archives (https://lists.offis.de/pipermail/mosaik-users/).