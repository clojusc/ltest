# ltest

*A custom test runner for clojure.test with detailed, coloured output and summaries*

## Usage

### Running Tests

Collections of tests may be run with the `(ltest/run-tests)` function:

```clj
(ltest/run-tests 'ltest.group1.samples.sample2)
```

Here's is a screenshot of this call's result in the ltest dev environment:

[![][screen1-thumb]][screen1]


## License

Copyright © 2017, Clojure-Aided Enrichment Center

Distributed under the Apache License, Version 2.0.


<!-- Named page links below: /-->

[screen1-thumb]: resources/images/ns-test-thumb.png
[screen1]: resources/images/ns-test.png
