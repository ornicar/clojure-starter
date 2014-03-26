(ns prismic-starter.routes
  (:use compojure.core)
  (:require [noir.response :refer [redirect]]
            [prismic-starter.views :as views]
            [io.prismic.api :as prismic]))

(defn- api [] (prismic/get-api "http://lesbonneschoses.wroom.dev/api"))
(defn- parse-page [string] (max 1 (try (Integer/parseInt string) (catch NumberFormatException e 1))))

(defn home [page]
  (views/home (prismic/search (api) :everything {:page page :pageSize 10})))

(defn search [query page]
  (let [q (str "[[:d = fulltext(document, \"" query \"")]]")]
    (views/search query (prismic/search (api) :everything {:q q :page page :pageSize 10}))))

(defn doc [id slug]
  (let [d (prismic/get-by-id (api) id)]
    (if (= slug (-> d :slugs first)) (views/doc d) (redirect (views/doc-url d)))))

(defroutes app-routes
  (GET "/" [page] (home (parse-page page)))
  (GET "/search" [query page] (search query (parse-page page)))
  (GET "/:typ/:id/:slug" [typ id slug] (doc id slug)))
