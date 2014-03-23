(ns prismic-starter.views
  (:use hiccup.core)
  (:use hiccup.page)
  (:require [prismic-starter.util :as util]
            [io.prismic.render :as render]))

(defn- resolver [link]
  (let [document (-> link :value :document)]
    (str "http://localhost/" (:type document) "/" (:id document))))

(defn- layout [& args]
  (html5
    [:head
     [:title "prismic.io Clojure starter"]
     (include-css "/css/bootstrap.min.css")
     (include-css "/css/bootstrap-theme.min.css")
     (include-css "/css/screen.css")]
    [:body
     [:div.navbar.navbar-inverse.navbar-fixed-top
      [:div.container
       [:div.navbar-header
        [:a.navbar-brand {:href "/"} "prismic.io Clojure starter"]]]]
     [:div.container (html args)]]))

(defn doc [d]
  (prn d)
  (layout
    (render/document d resolver)))

(defn docs [ds]
  (html
    [:ul
     (for [d ds]
       [:li
        [:a {:href (util/doc-url d)} (-> d :slugs first)]])]))

(defn home [res]
  (layout
    [:h2 "Homepage"]
    [:h3 (case (:results_size res)
           0 "No documents found"
           1 "One document found"
           (str "Showing " (:results_size res)
                " out of " (:total_results_size res) " documents"))]
    (docs (:results res))))
