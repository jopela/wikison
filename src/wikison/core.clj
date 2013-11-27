(ns wikison.core
  (:gen-class)
  (:require [clojure.tools.cli :as c]
            [clojure.pprint :as p]
            [wikison.filters :as filters-func]
            [wikison.request :as request]
            [wikison.eval :as weval]
            [wikison.extract :as extract]))

(defn article
  "return a document based on information fetched from the given url"
  ([filters eval-func user-agent url]
   (let [raw-result (request/raw-article user-agent url)
         simple-properties (extract/simple-prop-extract raw-result)
         lang (extract/languages-extract raw-result)
         thumb (extract/thumbnail-extract raw-result) 
         text  (extract/text-extract filters eval-func raw-result)]
    (apply merge [simple-properties lang thumb text])))

  ([user-agent url]
   (article [filters-func/del-empty-sec filters-func/del-unwanted-sec]
            weval/tree-eval-clj
            user-agent url)))

(defn -main
  "json artcile from (media)wiki urls"
  [& args]
  (let [ [options args banner]
         (c/cli args
             ["-h" "--help" "print this help banner and exit" :flag true]
             ["-n" "--name" "the name that will be put in the request to the
                            media wiki API (typically yours)"]
             ["-u" "--user" "user-agent heder. Should include your mail"])]

    (when (options :help)
      (println banner)
      (System/exit 0))
    
    (when-not (options :user)
      (println "User agent is required! See --help for details")
      (System/exit 1))

    (let [user-agent (:user options)
          articles (map (partial article user-agent) args) ]
      (doseq [art articles]
        (p/pprint art)))))

