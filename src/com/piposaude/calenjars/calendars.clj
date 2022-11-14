(ns com.piposaude.calenjars.calendars
  (:require
   [tick.alpha.api :as t])
  (:import
   (java.time LocalDate LocalDateTime)))

(def units #{:days :weeks :months :years :business-days})

(defn- validate-input [date n unit]
  (when-not (or (instance? LocalDate date) (instance? LocalDateTime date))
    (throw (IllegalArgumentException. (str "Illegal date: " date))))
  (when-not (integer? n)
    (throw (IllegalArgumentException. (str "Illegal n: " n))))
  (when-not (contains? units unit)
    (throw (IllegalArgumentException. (str "Unrecognized unit: " unit)))))

(defn- sign [n]
  (if (pos? n) 1 -1))

(defn- get-step [n]
  (if (zero? n)
    0
    (sign n)))

(defn- absolute [x]
  (if pos? x (- x)))

(defn- is-date-in-list? [date list]
  (boolean (some #{(t/date date)} list)))

(defn- inc-unless-holiday [date non-business-days days-added n]
  (if (is-date-in-list? date non-business-days)
    days-added
    (+ days-added (sign n))))

(defn- add-with-calendars [date n non-business-days]
  (let [step (t/new-period (get-step n) :days)]
    (if (= n 0)
      (if (is-date-in-list? date non-business-days)
        (add-with-calendars date 1 non-business-days)
        date)
      (loop [candidate date
             days-added 0]
        (if (= (absolute n) days-added)
          candidate
          (let [new-date (t/+ candidate step)
                m (inc-unless-holiday new-date non-business-days days-added n)]
            (recur new-date m)))))))

(defn relative-date-add
  "Adds n 'unit's to date and returns a new date
  date must be an instance of java.time.LocalDate
  or java.time.LocalDateTime, n must be an integer
  and valid units are found in the units set"
  [date n unit non-business-days]
  (validate-input date n unit)
  (if (= unit :business-days)
    (add-with-calendars date n non-business-days)
    (t/+ date (t/new-period n unit))))

(defn date-range
  "Returns a vector of all dates starting at start-date (adjusted by unit)
  to end-date"
  [start-date end-date unit non-business-days]
  (let [start-date (relative-date-add start-date 0 unit non-business-days)]
    (loop [d start-date dates [start-date]]
      (let [nd (relative-date-add d 1 unit non-business-days)]
        (if (t/> nd end-date)
          dates
          (recur nd (conj dates nd)))))))

(defn weekend?
  "Returns true only if date is in a weekend"
  [date weekend-days]
  (is-date-in-list? date weekend-days))

(defn holiday?
  "Returns true only if date is a holiday in the given calendar"
  [date holidays]
  (is-date-in-list? date holidays))

(defn non-business-day?
  "Returns true only if date is whether a weekend
  or a holiday in one of the given calendars"
  [date non-business-days]
  (is-date-in-list? date non-business-days))

(defn business-day?
  "Returns true only if date is not in a weekend
  and also not a holiday in any of the given calendars"
  [date non-business-days]
  (not (non-business-day? date non-business-days)))
