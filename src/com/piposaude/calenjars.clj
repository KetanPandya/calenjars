(ns com.piposaude.calenjars
  (:require [tick.alpha.api :as t]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [com.piposaude.calenjars.date-rules :as dr]
            [com.piposaude.calenjars.calendars :as cal]))

(def WEEKEND-FILE-NAME "WEEKEND")

(def read-calendar
  (memoize
   (fn [calendar]
     (let [holiday (io/resource calendar)
           holiday-strings (some-> holiday
                                   slurp
                                   (str/split #"\n"))]
       (when holiday-strings
         (map t/date holiday-strings))))))

(def read-calendars
  (memoize
   (fn [calendars]
     (->> calendars
          (keep read-calendar)
          flatten
          sort
          dedupe))))

(defn relative-date-add
  "Adds n 'unit's to date and returns a new date
  date must be an instance of java.time.LocalDate
  or java.time.LocalDateTime, n must be an integer
  and valid units are found in the units set"
  [date n unit & calendars]
  (cal/relative-date-add date n unit (read-calendars (set (conj calendars WEEKEND-FILE-NAME)))))


(defn date-range
  "Returns a vector of all dates starting at start-date (adjusted by unit)
  to end-date"
  [start-date end-date unit & calendars]
  (cal/date-range start-date end-date unit (read-calendars (set (conj calendars WEEKEND-FILE-NAME)))))

(defn weekend?
  "Returns true only if date is in a weekend"
  [date]
  (cal/weekend? date (read-calendar WEEKEND-FILE-NAME)))

(defn holiday?
  "Returns true only if date is a holiday in the given calendar"
  [date calendar]
  (cal/holiday? date (read-calendar calendar)))

(defn non-business-day?
  "Returns true only if date is whether a weekend
  or a holiday in one of the given calendars"
  [date & calendars]
  (cal/non-business-day? date (read-calendars (set (conj calendars WEEKEND-FILE-NAME)))))

(defn business-day?
  "Returns true only if date is not in a weekend
  and also not a holiday in any of the given calendars"
  [date & calendars]
  (not (apply non-business-day? date calendars)))

(defn apply-date-rules
  "Returns a date after applying date rules to the supplied
  date using the supplied calendars. A single date rule can be passed or
  as a vector of date rules that are applied one after another left to right"
  [date date-rule & calendars]
  (dr/apply-date-rules date date-rule (read-calendars (set (conj calendars WEEKEND-FILE-NAME)))))
