(ns quanta.calendar.env.mixed
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.calendar.env.scheduler :refer [get-calendar-flow]])
  (:import [missionary Cancelled]))



; demo

(def dt (atom (t/instant)))

(def mix (switch dt [:crypto :m]))


(def stop!
  ((m/reduce (fn [r v]
               (println "val: " v)) nil mix)
   #(prn ::client-success %)
   #(prn ::client-crash %)))

(reset! dt (t/instant))

(reset! dt nil)

(stop!)



