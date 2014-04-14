(ns prismic-starter.views
  (:use hiccup.core hiccup.page hiccup.form)
  (:require [io.prismic.render :as render]
            [prismic-starter.ctx :as ctx]))

; components

(defn- toolbar [ctx]
  (let [ref (:ref ctx)
        master (:master ctx)
        releases (:releases ctx)
        label-select (html (label :release_selector "See this website:"))
        master-option (select-options [["As currently seen by guest visitors" (:ref master)]] ref)
        releases-options (select-options (map (fn [ref] [(:label ref) (:ref ref)]) releases) ref)]
    (html "<form method=\"GET\">" label-select "&nbsp;<select id=\"release_selector\" name=\"ref\" onchange=\"this.form.submit()\">" master-option releases-options "</select></form>")))

(defn- signout-link []
  (if (ctx/has-privileged-access)
    (html [:a {:href "/signout"} "Disconnect"])))

(defn- layout [ctx & args]
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
        [:a.navbar-brand {:href "/"} "prismic.io Clojure starter"]](when (ctx/has-privileged-access) (toolbar ctx))]]
     [:div.container (html args)]]))

(defn- docs [ctx ds] (html [:ul (for [d ds] [:li [:a {:href ((-> ctx :resolver :document) d)} (-> d :slugs first)]])]))

(defn- with-ref-paramater [ctx url]
  (if (true? (:is-ref-is-master ctx)) (str "?" url) (str "?ref=" (:ref ctx) url)))

(defn- paginate [ctx res url]
  (when (> (:total_pages res) 0)
    (let [page (:page res)
          pager (html [:ul.pagination
                       [:li (when (:prev_page res) [:a {:href (url (- page 1))} "Previous"])]
                       [:li [:a (str "Page " page)]]
                       [:li (when (:next_page res) [:a {:href (url (+ page 1))} "Next"])]])]
      (str pager (docs ctx (:results res)) pager))))

(defn- nb-results [res]
  (case (:results_size res)
    0 "No documents found"
    1 "One document found"
    (str "Showing " (:results_size res) " out of " (:total_results_size res) " documents")))

(defn- search-form [ctx q]
  (form-to [:get "/search"]
           (when (not (:is-ref-is-master ctx)) [:input {:type "hidden" :name "ref" :value (:ref ctx)}])
           [:input {:name "query" :value q :placeholder "Search"}]))

; pages

(defn doc [ctx d] (layout ctx (render/document d (-> ctx :resolver :link))))

(defn- signin-link []
  (if-not (ctx/has-privileged-access)
  (let [link (html [:a {:href "/signin"} "Sign in to preview changes"])]
    (str "<hr/>" link))))

(defn home [ctx, res]
  (layout
    ctx
    [:h2 "Homepage"]
    (search-form ctx "")
    [:h3 (nb-results res)]
    (paginate ctx res #(str (with-ref-paramater ctx "&page=") %))
    (signin-link)
    ()))

(defn search [ctx query res]
  (layout
    ctx
    [:h2 "Search"]
    (search-form ctx query)
    [:h3 (nb-results res)]
    (paginate ctx res #(str "/search" (with-ref-paramater ctx (str "&query=" query "&page=")) %))
    (signin-link)))
