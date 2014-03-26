(ns prismic-starter.routes
  (:use compojure.core)
  (:require [noir.response :refer [redirect]]
            [prismic-starter.views :as views]
            [prismic-starter.util :refer [doc-url]]
            [io.prismic.api :refer :all]))

(defn- api [] (get-api "http://lesbonneschoses.wroom.dev/api"))

(defn home [page]
  (views/home (search (api) :everything {:page (max 1 (or page 1))
                                         :pageSize 10})))

(defn doc [id slug]
  (let [d (get-by-id (api) id)]
    (if (= slug (-> d :slugs first))
      (views/doc d)
      (redirect (doc-url d)))))

(defn- parse-int [string] (try
                            (Integer/parseInt string)
                            (catch NumberFormatException e 0)))

  (defroutes app-routes
    (GET "/" [page] (home (parse-int page)))
    (GET "/:typ/:id/:slug" [typ id slug] (doc id slug)))

