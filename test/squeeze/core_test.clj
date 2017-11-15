(ns squeeze.core-test
  (:require [clojure.test :refer :all]
            [squeeze.core :refer :all]
            [schema.core :as s]))

(defmacro coerce-ok [schema data expected]
  `(do
     (is (= (coerce-config ~schema ~data) ~expected))
     ;; Additionally check if the coercion is idempotent
     (is (= (coerce-config ~schema ~expected) ~expected))))

(deftest coercer
  (testing "Basic coercions"
    (coerce-ok s/Int "1" 1)
    (coerce-ok s/Num "1.1" 1.1)
    (coerce-ok s/Bool "true" true)
    (coerce-ok s/Keyword "kw" :kw)
    (coerce-ok (s/enum :kw-enum) "kw-enum" :kw-enum))
  (testing "String as YAML coercions"
    (coerce-ok [s/Str] "[str,str]" ["str" "str"])
    (coerce-ok [s/Keyword] "[kw,kw]" [:kw :kw])
    (coerce-ok {s/Keyword s/Str} "kw: str" {:kw "str"})
    (coerce-ok {s/Str s/Keyword} "str: kw" {"str" :kw})
    (coerce-ok {s/Keyword [{s/Keyword s/Keyword}]} "kw: [kw: kw]" {:kw [{:kw :kw}]}))
  (testing "Corner cases"
    (coerce-ok {} {} {})))

(deftest utils
  (is (= {:A 1} (map-remap {:A :a} {:a 1 :b 2})))
  (is (= {:port 1234} (remove-key-prefix :db- {:db-port 1234}))))
