(ns com.piposaude.calenjars.holidays
  (:require [tick.alpha.api :as t]
            [tick.format :as tf]
            [instaparse.core :as insta]
            [clojure.java.io :as io]
            [com.piposaude.calenjars.date-rules :refer [apply-date-rules]]
            [com.piposaude.calenjars.common :as common]
            [com.piposaude.calenjars.check :as check]
            [com.piposaude.calenjars.types.ddmmm :as ddmm]
            [com.piposaude.calenjars.types.ddmmmyyyy :as ddmmyyyy]
            [com.piposaude.calenjars.types.expression :as expression]
            [com.piposaude.calenjars.constants :refer [PARSER-GRAMMAR-FILENAME MIN-YEAR MAX-YEAR]]))

(defn format-YYYYMMDD [holiday]
  (t/format (tf/formatter "yyyy-MM-dd") holiday))

(defn get-weekends [year]
  (let [start (t/new-date year 1 1)
        days (if (common/leap-year? year) 366 365)
        interval (iterate #(t/+ % (t/new-period 1 :days)) start)
        weekends (filter #(#{t/SATURDAY t/SUNDAY} (t/day-of-week %)) (take days interval))]
    weekends))

(defn get-formatted-weekends [year]
  (map format-YYYYMMDD (get-weekends year)))

(defn gen-bracketed-formatted-weekends [year bracket-size]
  (let [years (range (- year bracket-size) (+ year (inc bracket-size)))]
    (flatten (map get-formatted-weekends years))))

(defn gen-bracketed-weekends [year bracket-size]
  (let [years (range (- year bracket-size) (+ year (inc bracket-size)))]
    (flatten (map get-weekends years))))

(defn valid-year? [year]
  (and (integer? year) (<= MIN-YEAR year MAX-YEAR)))

(defn get-holiday [year [_ name [type & args]]]
  (condp = type
    :ddmmm (ddmm/get-holiday-ddmm year name args)
    :ddmmmyyyy (ddmmyyyy/get-holiday-ddmmyyyy year name args)
    :expression (expression/get-holiday-expression year name args)
    nil))

(defn remove-exceptions [holidays]
  (let [exception-dates (set (map :date (filter :exception? holidays)))]
    (remove #(some #{(:date %)} exception-dates) holidays)))

(defn- holidays-for-year-with-exception-key [year holiday-file]
  (cond
    (not (valid-year? year))
    (throw (ex-info (str "Invalid year: " year) {:year year}))

    (not (check/valid-holiday-file? holiday-file))
    (throw (ex-info (str "Invalid holiday file: " holiday-file) {:holiday-file holiday-file :errors (check/get-errors holiday-file)}))

    :else
    (let [parser (insta/parser (io/resource PARSER-GRAMMAR-FILENAME))
          result (parser (slurp holiday-file))
          holidays (conj
                    (keep (partial get-holiday year) (common/drop-include result))
                    (when (common/holiday-was-included? result)
                      (holidays-for-year-with-exception-key year (common/included-filename holiday-file (second (first result))))))]
      (-> (remove nil? holidays)
          flatten
          remove-exceptions))))

(defn holidays-for-year [year holiday-file]
  (map #(select-keys % [:name :date]) (holidays-for-year-with-exception-key year holiday-file)))

(defn holidays-for-year-with-date-rule [year holiday-file]
  (map #(select-keys % [:name :date :date-rules]) (holidays-for-year-with-exception-key year holiday-file)))

(defn get-ruled-holidays [holiday-file year]
  (filter #(seq (:date-rules %)) (holidays-for-year-with-date-rule year holiday-file)))

(defn get-holiday-dates [holiday-file year]
  (map :date (holidays-for-year year holiday-file)))

(defn get-formated-holiday-dates [holiday-file year]
  (map format-YYYYMMDD (get-holiday-dates holiday-file year)))

(defn get-non-ruled-holiday-dates [holiday-file year]
  (map :date (filter #(empty? (:date-rules %)) (holidays-for-year-with-date-rule year holiday-file))))

(defn gen-bracketed-non-ruled-holiday-dates-for-years [holiday-file year bracket-size]
  (let [years (range (- year bracket-size) (+ year (inc bracket-size)))]
    (sort (flatten (map (partial get-non-ruled-holiday-dates holiday-file) years)))))

(defn gen-bracketed-ruled-holidays [holiday-file year bracket-size]
  (let [years (range (- year bracket-size) (+ year (inc bracket-size)))]
    (sort-by :date (flatten (map (partial get-ruled-holidays holiday-file) years)))))

(defn gen-bracketed-formated-holidays [holiday-file year bracket-size]
  (let [non-ruled-holiday-dates (gen-bracketed-non-ruled-holiday-dates-for-years holiday-file year bracket-size)
        weekends (gen-bracketed-weekends year bracket-size)]
    (loop [non-business-days (->> (concat non-ruled-holiday-dates weekends) sort dedupe)
           ruled-holidays (gen-bracketed-ruled-holidays holiday-file year bracket-size)
           holidays non-ruled-holiday-dates]
      (if (empty? ruled-holidays) (sort (map format-YYYYMMDD holidays))
          (let [ruled-holiday (first ruled-holidays)
                ruled-date (apply-date-rules (:date ruled-holiday) (:date-rules ruled-holiday) non-business-days)]
            (recur (->> (conj non-business-days ruled-date) sort dedupe) (rest ruled-holidays) (->> (conj holidays ruled-date) sort dedupe)))))))

(comment
  (let [year 2022
        parser (insta/parser (clojure.java.io/resource PARSER-GRAMMAR-FILENAME))
        holiday-file "./resources/calenjars-template/examples/BR.hol"
        result (parser (slurp holiday-file))
        holidays (conj
                  (keep (partial get-holiday year) (common/drop-include result))
                  (when (common/holiday-was-included? result)
                    (holidays-for-year-with-exception-key year (common/included-filename holiday-file (second (first result))))))]
    (-> (remove nil? holidays)
        flatten
        remove-exceptions))

  (holidays-for-year 2022 "./resources/calenjars-template/examples/BR.hol"))
