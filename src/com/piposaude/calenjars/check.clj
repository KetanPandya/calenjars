(ns com.piposaude.calenjars.check
  (:require [instaparse.core :as insta]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [com.piposaude.calenjars.common :as common]
            [com.piposaude.calenjars.constants :refer [PARSER-GRAMMAR-FILENAME]])
  (:import (java.io FileNotFoundException)))

(defn leap-year? [[_ year-str]]
  (let [year (edn/read-string year-str)]
    (common/leap-year? year)))

(defn leap-day? [[_ day month]]
  (and (= "29" day)
       (= "Feb" month)))

(defn contains-bad-leap-date [holiday]
  (let [[_ _ [type & data]] holiday]
    (if (= :ddmmmyyyy type)
      (and (leap-day? (first data)) (not (leap-year? (second data))))
      false)))

(defn contains-bad-leap-dates? [result]
  (some true? (map contains-bad-leap-date result)))

(defn included-holiday-exists? [result including-file]
  (let [included-holiday-name (second (first result))
        included-filename (common/included-filename including-file included-holiday-name)]
    (if (common/holiday-was-included? result)
      (try
        (boolean (io/reader included-filename))
        (catch FileNotFoundException _
          false))
      true)))

(defn valid-holiday-file? [filename]
  (let [parser (insta/parser (io/resource PARSER-GRAMMAR-FILENAME))
        result (parser (slurp filename))]
    (and
     (not (insta/failure? result))
     (not (contains-bad-leap-dates? (common/drop-include result)))
     (included-holiday-exists? result filename)
     (if-not (common/holiday-was-included? result)
       true
       (valid-holiday-file? (common/included-filename filename (second (first result))))))))

(defn get-errors [filename]
  (let [parser (insta/parser (io/resource PARSER-GRAMMAR-FILENAME))
        result (parser (slurp filename))]
    (merge
     {:parse-errors             (insta/get-failure result)
      :contains-bad-leap-dates? (contains-bad-leap-dates? (common/drop-include result))
      :included-holiday-exists? (included-holiday-exists? result filename)}
     (when (common/holiday-was-included? result)
       {:included-holiday-valid? (valid-holiday-file? (common/included-filename filename (second (first result))))}))))
