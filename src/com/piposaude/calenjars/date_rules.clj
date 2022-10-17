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

(defmulti date-rule (fn [rule _ _] rule))
(defmethod date-rule "F" [_ date non-business-days] (following date non-business-days))
(defmethod date-rule "P" [_ date non-business-days] (previous date non-business-days))
(defmethod date-rule "MF" [_ date non-business-days] (modified-following date non-business-days))
(defmethod date-rule "MP" [_ date non-business-days] (modified-previous date non-business-days))

(defn- apply-rule [date rule non-business-days]
  (date-rule rule date non-business-days))

(defn apply-date-rules [date rules non-business-days]
  ; (println date rules non-business-days)
 (loop [r (vec (flatten [rules])) d date] (if (empty? r) d (recur (rest r) (apply-rule d (first r) non-business-days)))))
