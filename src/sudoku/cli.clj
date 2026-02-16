(ns sudoku.cli
  (:require [sudoku.draw :as d]
            [sudoku.read :as r]
            [sudoku.solve :as s]))

(defn solve [args]
  (let [puzzle (r/string->puzzle (:puzzle args))
        t1 (System/currentTimeMillis)
        result (s/solve-with-containment-finder s/find-containment puzzle)
        t2 (System/currentTimeMillis)
        duration (/ (float (- t2 t1)) 1000)]
    (d/drawp (:puzzle result))
    (println "Done:     " (:done result))
    (println "Solved:   " (:solved result))
    (println "Steps:    " (:steps result))
    (println "Duration: " duration)))
