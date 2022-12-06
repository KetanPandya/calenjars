(ns com.piposaude.calenjars.date-rules
  (:require [tick.alpha.api :as t]
            [com.piposaude.calenjars.calendars :refer [business-day? relative-date-add]]))

(defn- same-month? [d1 d2]
 (= (t/month d1) (t/month d2)))
(defn following [date non-business-days]
  (loop [sd date] (if (business-day? sd non-business-days) sd (recur (relative-date-add date 1 :business-days non-business-days)))))

(defn previous [date non-business-days]
  (loop [sd date] (if (business-day? sd non-business-days) sd (recur (relative-date-add date -1 :business-days non-business-days)))))

(defn modified-following [date non-business-days]
  (let [f (following date non-business-days)]
  (if (same-month? date f) f (previous date non-business-days))))

(defn modified-previous [date non-business-days]
  (let [p (previous date non-business-days)]
  (if (same-month? date p) p (following date non-business-days))))

(defn- previous-months
  ([d n] (let [pd (t/- d (t/new-period n :months))] {:year (t/int (t/year pd)) :month (t/int (t/month pd))}))
  ([d] (previous-months d 1)))

(defn- day-of-month [d n]
  (t/new-date (t/int (t/year d)) (t/int (t/month d)) n))

(defn- first-of-month [d] (day-of-month d 1))

(defn- yesterday [d]
  (relative-date-add d -1 :days []))

(defn- end-of-last-month [d] (yesterday (first-of-month d)))

(defn- next-day [d day]
  (loop [d (relative-date-add d 1 :days [])] (if (= (t/day-of-week d) day) d (recur (relative-date-add d 1 :days [])))))

(defn- nth-day-in-month [d day n]
  (loop [d (end-of-last-month d) n n] (if (= n 0) d (recur (next-day d day) (dec n)))))

(defn- fourth-thursday [d] (nth-day-in-month d t/THURSDAY  4))

(defmulti date-rule (fn [rule _ _] rule))

(defmethod date-rule "4THU" [_ date non-business-days] (fourth-thursday date ))
(defmethod date-rule "F" [_ date non-business-days] (following date non-business-days))
(defmethod date-rule "P" [_ date non-business-days] (previous date non-business-days))
(defmethod date-rule "MF" [_ date non-business-days] (modified-following date non-business-days))
(defmethod date-rule "MP" [_ date non-business-days] (modified-previous date non-business-days))

(defn- apply-rule [date rule non-business-days]
  (date-rule rule date non-business-days))

(defn apply-date-rules [date rules non-business-days]
  ; (println date rules non-business-days)
 (loop [r (vec (flatten [rules])) d date] (if (empty? r) d (recur (rest r) (apply-rule d (first r) non-business-days)))))
