(ns sudoku.scrape
  "Functions for scraping puzzles from the web"
  (:require [clojure.string :as string]
            [sudoku.core :as core]))

(defn parse-page [html-str]
  (let [[_ level set-id] (re-find #"level=(\d)&set_id=(\d+)" html-str)
        solution (re-find #"\d{81}" html-str)
        mask (re-find #"[01]{81}" html-str)
        puzzle (mapv #(if (= %2 \1) nil (Integer. (str %1))) solution mask)]
    (with-meta puzzle {:level level :set-id set-id})))

(defn scrape-puzzle
  ([level]
   (scrape-puzzle level nil))
  ([level set-id]
   (let [url (str "https://four.websudoku.com/?level=" level
                  (when set-id
                    (str "&set_id=" set-id)))
         html-str (slurp url)]
     (if (string/includes? html-str "This IP address has been blocked")
       (throw (ex-info "IP blocked!" {}))
       (parse-page html-str)))))

; NOTE: websudoku doesn't like scraping, so you probably shouldn't use this function.
(defn scrape-and-solve-forever [level]
  (let [puzzle (scrape-puzzle level)
        result (core/solve-puzzle puzzle)
        result (-> result
                   (assoc :level level :set-id (:set-id (meta puzzle)))
                   (dissoc :solution))]
    (println result)
    (Thread/sleep 30000)
    (recur level)))

(defn -main [[level]]
  (scrape-and-solve-forever level))
