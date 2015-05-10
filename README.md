Bazel + Import.io Java Client Example
=====================================

I'm planning to use [import.io](http://import.io) for a side project (tl;dr
house hunting) and decided to use the newly open sourced
[bazel](http://bazel.io) build system from Google to build this project.

I thought publishing an example of how to use the import.io client from Maven
would be useful for others. I've also worked around google/bazel#89 (tl;dr
`maven_jar` does not bring in transitive dependencies) which is one less thing
for people to worry about.

Usage
-----

  1. [Install bazel](http://bazel.io/docs/getting-started.html).
  2. `bazel run -- //src/java:ImportIOExample "<YOUR GUID>" "<YOUR API KEY>"`.
  3. Profit.
