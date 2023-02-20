(ns sudoku.scrape
  (:require [clojure.string :as string]
            [sudoku.core :as core]))

; NOTE: websudoku doesn't like scraping, so you probably shouldn't use this code.

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

(defn scrape-and-solve-forever [level]
  (let [puzzle (scrape-puzzle level)
        sg (core/puzzle->scope-grid puzzle)
        t-start (System/currentTimeMillis)
        sg' (try
              (core/solve sg)
              (catch Exception e sg))
        t-end (System/currentTimeMillis)
        duration (/ (float (- t-end t-start)) 1000)
        result {:level level
                :set-id (:set-id (meta puzzle))
                :duration duration
                :done? (core/done? sg')
                :solved? (core/solved? (core/scope-grid->puzzle sg'))}]
    (println result)
    (Thread/sleep 30000)
    (recur level)))

(defn -main [[level]]
  (scrape-and-solve-forever level))
