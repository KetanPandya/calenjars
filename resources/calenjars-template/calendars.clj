(ns lib-ns.lib-name
  (:require [com.piposaude.calenjars :as calenjars]
            [tick.alpha.api :as t]
            [clojure.edn :as edn]))

(def date t/date)

(defn relative-date-add [date n unit & calendars]
  (apply calenjars/relative-date-add  date n unit calendars))

(defn date-range [start-date end-date unit & calendars]
  (apply calenjars/date-range  start-date end-date unit calendars))

(defn weekend? [date]
  (calenjars/weekend? date))

(defn holiday? [date calendar]
  (calenjars/holiday? (t/date date) calendar))

(defn non-business-day? [date & calendars]
  (apply calenjars/non-business-day? date calendars))

(defn business-day? [date & calendars]
  (apply calenjars/business-day? date calendars))

(defn apply-date-rules [date date-rules & calendars]
  (apply calenjars/apply-date-rules date date-rules calendars))


