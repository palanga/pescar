PESCAR MONO REPO
================

TEST
----

```shell script
./sbt
sbt:pescar> coverage
sbt:pescar> test
```

Currently, running tests from intellij doesn't work because of a ClassNotFound exception that's being thrown because it
can't find, apparently, the scoverage classes.


To run individual tests (for instance, for the analytics module):

```sbtshell
sbt:pescar> analyticsApi/test
```
