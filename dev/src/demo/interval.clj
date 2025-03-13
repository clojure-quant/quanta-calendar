(ns demo.interval
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.db.interval :refer [current-or-next]]))

(def dt (t/instant))

(def fx-d (current-or-next [:forex :d] dt))
(def fx-h (current-or-next [:forex :h] dt))

(take 10 (i/next-seq fx-d))
(take 10 (i/next-seq fx-h))