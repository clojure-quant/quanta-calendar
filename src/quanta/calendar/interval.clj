(ns quanta.calendar.interval)

(defprotocol interval
  (current [this])
  (move-next [this])
  (move-prior [this]))

;; seq are in window,
;; but in window we do not apply i/current, 
;; because we want to be able to modify the window still.

#_(defn next-seq [this]
    (->> (iterate move-next this)
         (map current)))

#_(defn prior-seq [this]
    (->> (iterate move-prior this)
         (map current)))

