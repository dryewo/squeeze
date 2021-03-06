(defproject squeeze "0.3.3"
  :description "Library for config coercion"
  :url "https://github.com/dryewo/squeeze"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[prismatic/schema "1.1.10"]
                 [circleci/clj-yaml "0.6.0"]]
  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-changelog "0.3.2"]
            [lein-ancient "0.6.15"]]
  :deploy-repositories [["releases" :clojars]]
  :aliases {"update-readme-version" ["shell" "sed" "-i" "s/\\\\[squeeze \"[0-9.]*\"\\\\]/[squeeze \"${:version}\"]/" "README.md"]}
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]]}})
