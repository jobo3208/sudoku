(ns sudoku.read
  "Functions for reading puzzle data from strings")

(defn parse-int [s]
  #?(:clj (Integer/parseInt s)
     :cljs (let [n (js/parseInt s 10)]
             (if (js/isNaN n)
               (throw (ex-info "not an integer" {}))
               n))))

(defn string->puzzle
  "Read a puzzle from a string. String should be 81 characters long, consisting of numbers and spaces only"
  [s]
  {:pre [(= (count s) 81)]}
  (mapv #(if (= % \space) nil (parse-int (str %))) s))
