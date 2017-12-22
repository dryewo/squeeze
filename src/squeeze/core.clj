(ns squeeze.core
  (:require [schema.coerce :as sc]
            [schema.core :as s]
            [schema.utils :as su]
            [clojure.string :as str]
            [clj-yaml.core :as yaml])
  (:import (clojure.lang PersistentArrayMap PersistentHashMap PersistentTreeMap Named)))

;;; Special matcher to remove unknown keys from the map
;;; From: http://stackoverflow.com/a/31597277

(defn filter-schema-keys [m schema-keys extra-keys-walker]
  (reduce-kv (fn [m k v]
               (if (or (contains? schema-keys k)
                       (and extra-keys-walker
                            (not (su/error? (extra-keys-walker k)))))
                 m
                 (dissoc m k)))
             m
             m))

(defn parse-yaml-str-keys [s]
  (yaml/parse-string s))

(defn map-filter-matcher [s]
  (when (or (instance? PersistentArrayMap s)
            (instance? PersistentHashMap s)
            (instance? PersistentTreeMap s))
    (let [extra-keys-schema (#'s/find-extra-keys-schema s)
          extra-keys-walker (when extra-keys-schema (s/checker extra-keys-schema))
          explicit-keys     (some->> (dissoc s extra-keys-schema)
                                     keys
                                     (mapv s/explicit-schema-key)
                                     (into #{}))]
      (when (or extra-keys-walker (seq explicit-keys))
        (fn [x]
          (let [coerced-x ((sc/safe parse-yaml-str-keys) x)]
            (if (map? coerced-x)
              (filter-schema-keys coerced-x explicit-keys extra-keys-walker)
              coerced-x)))))))

(defn array-matcher [s]
  (when (sequential? s)
    (sc/safe parse-yaml-str-keys)))

(defn ->string [x]
  (if (instance? Named x)
    (name x)
    (str x)))

(def stringable-matcher
  {s/Str ->string})

(defn config-coercion-matcher
  "A matcher that coerces keywords, keyword eq/enums, s/Num and s/Int,
     and long and doubles (JVM only) from strings, also removes unknown keys
     and parses strings as YAML where map is expected."
  [schema]
  (or (map-filter-matcher schema)
      (array-matcher schema)
      (stringable-matcher schema)
      (sc/+string-coercions+ schema)
      (sc/keyword-enum-matcher schema)
      (sc/set-matcher schema)))

(defn coerce-config
  "Validates and transforms, if possible, `data` against `schema`, dropping unknown keys."
  [schema data]
  (if (and (map? schema) (empty? schema))
    {}
    (let [coercer-fn (sc/coercer! (s/maybe schema) config-coercion-matcher)]
      (coercer-fn data))))

(defn map-remap
  "(map-remap {:A :a} {:a 1 :b 2}) => {:A 1}"
  [keymap m]
  (into {} (for [[new old] keymap]
             [new (get m old)])))

(defn remove-key-prefix
  "(remove-key-prefix :db- {:db-port 1234}) => {:port 1234}"
  [prefix m]
  (let [re (re-pattern (str "^" (name prefix)))]
    (into {} (for [[k v] m]
               [(keyword (str/replace (name k) re "")) v]))))
