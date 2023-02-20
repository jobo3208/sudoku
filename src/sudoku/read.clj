(ns sudoku.read)

(defn string->puzzle
  "Read a puzzle from a string. String should be 81 characters long, consisting of numbers and spaces only"
  [s]
  {:pre [(= (count s) 81)]}
  (mapv #(if (= % \space) nil (Integer. (str %))) s))
