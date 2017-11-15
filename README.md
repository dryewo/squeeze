# squeeze

[![Build Status](https://travis-ci.org/dryewo/squeeze.svg?branch=master)](https://travis-ci.org/dryewo/squeeze)

A Clojure library for config coercion.

## Rationale

[The Twelve-Factor App] recommends to only use environment variables to pass configuration to applications.
That means, no files, no web-services that application calls to receive its config, only reading from the environment.

Process environment is platform-independent and immutable.

This brings several challenges:

1. Environment is a flat map (hash, dictionary) from string to string.
   Configuration values, on the other hand, can be not only strings, but also numbers, booleans, as well
   as lists and maps of primitive values.
2. Accessing the environment directly is error-prone: if some configuration value is used during program execution,
   it will only be discovered when it's first read from the environment and we try to convert it from string
   to something useful. This can be avoided by loading and validating all the configuration when the application starts up.
 
This library does not:

- Manage multiple sources of configuration, merge them together, track various application profiles etc. Only transformation from 
  String->String to user-defined schema.
- Distribute parts of the application configuration to corresponding components. It does not rely on [component],
  [mount] or any other state management library.

## Examples

All examples assume the following imports:

```clj
(ns my-app.core
  (:require [squeeze.core :as squeeze]
            [environ.core :as env]
            [schema.core :as s]))
```

Simple example:

```clj
(def default-config
  {:http-port   8090
   :http-bind   "0.0.0.0"})

(s/defschema Config
  {(s/optional-key :http-port)   s/Int
   (s/optional-key :http-bind)   s/Str
   (s/optional-key :http-public) s/Bool}

(squeeze/coerce-config Config (merge default-config environ/env))
```

Given the following process environment:

```
LC_ALL=en_US.UTF-8
LANG=en_US.UTF-8
HTTP_PORT=4200
TERM=xterm-256color
SHELL=/bin/bash
```

The example would yield the following:

```clj
{:http-port 4200
 :http-bind "0.0.0.0"}
```

1. Unknown keys are removed.
2. Values are coerced to the schema, if possible.

### Data structures

Environment variables are perfectly suited for passing big documents in them:
* they are not limited in size
* they can contain arbitrary text characters, including new lines

```yaml
# whitelist.yaml
- 1.1.1.1
- 2.2.2.2
- 3.3.3.3
```
When run as:
```sh
HTTP_IP_WHITELIST=$(cat http.yaml) java -jar ...
```
Using schema:
```clj
(s/defschema HttpConfig
  {:http-ip-whitelist [s/Str]})
(squeeze/coerce-config HttpConfig environ/env)
```
Would yield:
```clj
{:http-ip-whitelist ["1.1.1.1" "2.2.2.2" "3.3.3.3"]}
```

### Helper functions

If you need to get rid of the name prefix, like `:http-`, use `remove-key-prefix`:

```clj
(squeeze/remove-key-prefix :db- {:db-port 1234})
; => {:port 1234}
```

In order to remap some keys in a map and only keep the remapped ones, use `remap-keys`:

```clj
(squeeze/remap-keys {:whitelist :http-ip-whitelist} {:http-ip-whitelist ["1.1.1.1"] :http-port 8000})
; => {:whitelist ["1.1.1.1"]}
```

## Dependencies

Squeeze relies on [Prismatic Schema] and [clj-yaml].

## License

Copyright © 2017 Dmitrii Balakhonskii

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[The Twelve-Factor App]: https://12factor.net/config
[component]: https://github.com/stuartsierra/component
[mount]: https://github.com/tolitius/mount
[Prismatic Schema]: https://github.com/plumatic/schema
[clj-yaml]: https://github.com/circleci/clj-yaml
