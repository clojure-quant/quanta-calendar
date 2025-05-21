(ns demo.util
  (:require
   [clojure.string :as str]
   [tablecloth.api :as tc]
   [tick.core :as t]
   [clojure.java.io :as java-io]
   [tech.v3.io :as io]
   [tech.v3.dataset.print :as p])
  (:import (java.io FileNotFoundException)))

(defn save-ds [asset ds]
  (let [filename (str "../data/" asset ".nippy.gz")
        s (io/gzip-output-stream! filename)]
    (io/put-nippy! s ds)))

(defn load-ds [asset]
  (let [filename (str "../data/" asset ".nippy.gz")
        s (io/gzip-input-stream filename)
        ds (io/get-nippy s)]
    ds))

(defn print-ds [ds]
  (-> ds
      (p/print-range :all)))

(comment
  (load-ds "EURCHF")
  (load-ds "GBPJPY")
  (load-ds "USDJPY")

;  
  )