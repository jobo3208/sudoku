(ns sudoku.cli
  (:require [sudoku.core :as c]
            [sudoku.draw :as d]
            [sudoku.read :as r]))

(defn -main [puzzle-str]
  (let [puzzle (r/string->puzzle puzzle-str)
        t1 (System/currentTimeMillis)
        result (c/solve-puzzle puzzle)
        t2 (System/currentTimeMillis)
        duration (/ (float (- t2 t1)) 1000)]
    (d/drawp (:solution result))
    (println "Done:     " (:done? result))
    (println "Solved:   " (:solved? result))
    (println "Duration: " duration)))
