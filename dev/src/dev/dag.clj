(ns dev.dag
  (:require
   [missionary.core :as m]
   [tick.core :as t]
   [quanta.calendar.env :refer [get-calendar]]))

(defn get-val [cell]
  (->> cell
       (m/eduction (take 1))
       (m/reduce (fn [_s v] v) nil)
       m/?))

;; historic

(def dag-historic {:dag {:opts (atom {:dt (t/instant)})}})

(def dt-cell-historic
  (get-calendar dag-historic {:calendar [:crypto :m]}))

(get-val dt-cell-historic)

;; live

(def dag-live {:dag {:opts (atom {})}})

(def dt-cell-live
  (get-calendar dag-live {:calendar [:crypto :m]}))

(get-val dt-cell-live)

(defn get-vals [cell n]
  (->> cell
       (m/eduction (take n))
       (m/reduce (fn [s v] (conj s v)) [])
       m/?))

(get-vals dt-cell-live 2)









