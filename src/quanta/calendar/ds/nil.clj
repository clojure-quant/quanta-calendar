(ns quanta.calendar.ds.nil)

(defn fill-forward
  "link-col is a column of int representing idx locations.
   all nil values will be filled with the last prior index.
   will not fill missing values in the begin"
  [link-col]
  (reduce (fn [acc x]
            (conj acc (if (nil? x) (peek acc) x)))
          []
          link-col))

(defn fill-start-backward
  "link-col is a column of int representing idx locations.
   all nil values in the start will be filled with the first 
   available index. later missing values  will not filled"
  [link-col]
  (let [v (vec link-col)
        first-non-nil (some #(when (some? (second %)) %) (map-indexed vector v))]
    (if (nil? first-non-nil)
      v
      (let [[first-idx fill] first-non-nil]
        (into [] (map-indexed (fn [i x]
                                (if (and (< i first-idx) (nil? x))
                                  fill
                                  x))
                              v))))))

(defn fill-forward-start-backward [link-col]
  (-> link-col
      fill-forward
      fill-start-backward))