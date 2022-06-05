# Mthl's personal website

This repository contains the source for generating mthl's
[website](https://reuz.fr).  The pages are generated using custom
[Clojure](https://clojure.org) code.

## How to build

```sh
clojure -X:gen
clojure -T:build compile
clojure -T:build package
```

## How to serve the website locally

```sh
clojure -X:gen
clojure -M:run
```
