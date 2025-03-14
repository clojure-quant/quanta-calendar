(ns quanta.calendar.interval)

(defprotocol interval
  (current [this])
  (move-next [this])
  (move-prior [this]))

(defn next-seq [this]
  (->> (iterate move-next this)
       (map current)))

(defn prior-seq [this]
  (->> (iterate move-prior this)
       (map current)))

