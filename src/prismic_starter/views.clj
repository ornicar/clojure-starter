(ns prismic-starter.views
  (:use hiccup.core hiccup.page hiccup.form)
  (:require [prismic-starter.util :as util]
            [io.prismic.render :as render]))

(defn- resolver [link]
  (let [document (-> link :value :document)]
    (str "/" (:type document) "/" (:id document) "/" (:slug document))))

; components

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

(defn- docs [ds]
  (html [:ul (for [d ds] [:li [:a {:href (util/doc-url d)} (-> d :slugs first)]])]))

(defn- paginate [res url]
  (when (> (:total_pages res) 0)
    (let [page (:page res)
          pager (html [:ul.pagination
                       [:li (when (:prev_page res) [:a {:href (url (- page 1))} "Previous"])]
                       [:li [:a (str "Page " page)]]
                       [:li (when (:next_page res) [:a {:href (url (+ page 1))} "Next"])]])]
      (str pager (docs (:results res)) pager))))

(defn- nb-results [res]
  (case (:results_size res)
    0 "No documents found"
    1 "One document found"
    (str "Showing " (:results_size res) " out of " (:total_results_size res) " documents")))

(defn- search-form [q]
  (form-to [:get "/search"] [:input {:name "query" :value q :placeholder "Search"}]))

; page views

(defn doc [d] (layout (render/document d resolver)))

(defn home [res]
  (layout
    [:h2 "Homepage"]
    (search-form "")
    [:h3 (nb-results res)]
    (paginate res #(str "/?page=" %))))

(defn search [query res]
  (layout
    [:h2 "Search"]
    (search-form query)
    [:h3 (nb-results res)]
    (paginate res #(str "/search?query=" query "&page=" %))))
