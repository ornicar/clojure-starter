(ns prismic-starter.util
  (:require [noir.io :as io]
            [markdown.core :as md]))

(defn doc-url [doc] (str "/docs/" (:id doc) "/" (-> doc :slugs first)))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (->>
    (io/slurp-resource filename)
    (md/md-to-html-string)))
