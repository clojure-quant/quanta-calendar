(ns demo.env
  (:require
   [missionary.core :as m]
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.env :refer [create-calendar-env get-calendar set-dt get-calendar-close-date]]))

(def env (create-calendar-env))

env

(def crypto (get-calendar env {:calendar [:crypto :m]}))

crypto

(def stop!
  ((m/reduce (fn [s v]
               (println "dt: " (i/current v))
               (spit "bongo.txt" (str "\ndt: " (i/current v)) :append true)) nil crypto)
   #(prn "success " %)
   #(prn "crash " %)))

;; historic

(set-dt env (t/instant))

(set-dt env (t/instant "2025-02-01T00:13:00Z"))

(set-dt env nil)

(stop!)

; date

(def crypto2 (get-calendar-close-date env {:calendar [:crypto :m]}))

(def stop2!
  ((m/reduce (fn [s v]
               (println "dt: " v)
               (spit "bongo.txt" (str "\ndt: " v) :append true)) nil crypto2)
   #(prn "success " %)
   #(prn "crash " %)))

(set-dt env (t/instant))

(set-dt env (t/instant "2025-02-01T00:13:00Z"))

(set-dt env nil)

(stop2!)






