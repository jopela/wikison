(ns wikison.filters
  "Collection of filter functions (and associated helper functions) applied 
  before the evaluation of the article tree into a concrete representation 
  (e.g: html, json etc.). A filter function could be, for example, a function 
  to remove unwanted sections or to remove certain words from the text."
  (:require [clojure.core.match :as match]
            [clojure.string :as string]
            [instaparse.core :as insta]
            [clojure.zip :as zip]))

; helper functions
; title we commonly want to remove
(def removable
  #{" References "
    " See also "})

; the article generation process is described by the following diagram
; raw-article --> parsing --> pre-process --> filtering --> post-filtering 
; --> evaluation.

; parsing: parses the article into an abstract syntax tree.
; pre-processing: perform operation such as merging sentences into text in 
; order to prepare for the filtering stage.
; filtering: prune the syntax tree of it's unwanted nodes.
; post-filtering: operations to be performed before evaluation.
; turns the resulting syntax tree into a concrete representation (json, html,
; text etc.)

; pre-process transform
(defn merge-sentence
  "merge sentences into text nodes"
  [syntax-tree]
  (insta/transform {:sentence str
                    :text (fn [& args] [:text (apply str args)] )}
                   syntax-tree))


;(defn subsections?
;  "Returns true if the loc points to a section that has subsection. nil if
;  not."
;  [loc]
;  (let [siblings (rights loc)]


(defn del-sec-with-title
  "entirely delete the section node that have a title that belong to one of the
  title set."
  [article ts]
  (loop [current (zip/vector-zip article)]
    (if (zip/end? current)
      (zip/root current)
      (let [current-node (zip/node current)]
        (if (and (= current-node :title) (-> current zip/right zip/node ts))
          (recur (-> current zip/up zip/up zip/remove))
          (recur (zip/next current)))))))

(defn del-empty-sec
  "remove section that have blank text and no child"
  [article]
  article)


