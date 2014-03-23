(ns prismic-starter.routes
  (:use compojure.core)
  (:require [noir.response :refer [redirect]]
            [prismic-starter.views :as views]
            [prismic-starter.util :refer [doc-url]]
            [io.prismic.api :refer :all]))

(defn- api [] (get-api "https://lesbonneschoses.prismic.io/api"))

(defn home []
  (views/home (search (api) :everything {})))

(defn doc [id slug]
  (let [d (get-by-id (api) id)]
    (if (= slug (-> d :slugs first))
      (views/doc d)
      (redirect (doc-url d)))))

(defroutes app-routes
  (GET "/" [] (home))
  (GET "/docs/:id/:slug" [id slug] (doc id slug)))

