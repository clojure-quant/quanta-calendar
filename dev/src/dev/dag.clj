(ns dev.dag
  (:require
   [missionary.core :as m]
   [tick.core :as t]
   [quanta.calendar.dag :refer [get-calendar]]))

(defn get-val [cell]
  (->> cell
       (m/eduction (take 1))
       (m/reduce (fn [_s v] v) nil)
       m/?))

;; historic

(def dag-historic {:opts (atom {:dt (t/instant)})})

(def dt-cell-historic
  (get-calendar dag-historic [:crypto :m]))

(get-val dt-cell-historic)

;; live

(def dag-live {:opts (atom {})})

(def dt-cell-live
  (get-calendar dag-live [:crypto :m]))

(get-val dt-cell-live)

(defn get-vals [cell n]
  (->> cell
       (m/eduction (take n))
       (m/reduce (fn [s v] (conj s v)) [])
       m/?))

(get-vals dt-cell-live 2)









