(defproject squeeze "0.1.1"
  :description "Library for config coercion"
  :url "https://github.com/dryewo/squeeze"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.7"]
                 [circleci/clj-yaml "0.5.6"]]
  :plugins [[lein-cloverage "1.0.9"]
            [lein-shell "0.5.0"]]
  :deploy-repositories [["releases" :clojars]]
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]])
