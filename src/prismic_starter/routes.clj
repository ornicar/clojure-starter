(ns prismic-starter.routes
  (:use compojure.core)
  (:require [noir.response :refer [redirect]]
            [prismic-starter.views :as views]
            [prismic-starter.ctx :as ctx]
            [prismic-starter.config :as config]
            [clj-http.client :as http]
            [noir.cookies :as cookies]
            [io.prismic.api :as prismic]))

(defn- api []
  (prismic/get-api (config/api-endpoint) (ctx/get-access-token)))

(defn- parse-page [string]
  (max 1 (try (Integer/parseInt string) (catch NumberFormatException e 1))))

(defn- callback-url [host referer]
  (str "http://" host "/oauth_callback?redirect_uri=" referer))

(defn home [page ctx]
  (views/home ctx (prismic/search (:api ctx) (:ref ctx) :everything {:page page :pageSize 10})))

(defn search [query page ctx]
  (let [q (str "[[:d = fulltext(document, \"" query \"")]]")]
    (views/search ctx query (prismic/search (:api ctx) (:ref ctx) :everything {:q q :page page :pageSize 10}))))

(defn doc [id slug ctx]
  (let [d (prismic/get-by-id (:api ctx) (:ref ctx) id)
        link-resolver (-> ctx :resolver :link)]
    (if (= slug (-> d :slugs first)) (views/doc ctx d) (redirect (link-resolver d)))))

(defn- clear-token-cookie []
  (cookies/put! :access_token {:value "" :max-age 0}))

(defn signin [host referer]
  (let [oauth-initiate-base (:oauth_initiate (api))
        client-id (config/client-id)
        scope "master+releases"
        referer (if (nil? referer) host referer)
        params (ring.util.codec/form-encode {:client_id client-id :redirect_uri (callback-url host referer) :scope scope})]
    (redirect (str oauth-initiate-base "?" params))))

(defn signout []
  (clear-token-cookie)
  (redirect "/"))

(defn oauth-callback [code redirect-uri]
  (let [oauth-token (:oauth_token (api))
        client-id (config/client-id)
        client-secret (config/client-secret)
        response (http/post oauth-token
                            {:form-params {
                               :grant_type "authorization_code"
                               :code code
                               :redirect_uri redirect-uri
                               :client_id client-id
                               :client_secret client-secret}
                             :as :json})
        access-token (get-in response [:body :access_token])]
    (cookies/put! :access_token access-token)
    (redirect redirect-uri)))

(defn- with-ctx [r action]
  (try
    (let [api (api)
          refs (:refs api)
          master (first (filter (fn [ref] (true? (:isMasterRef ref))) refs))
          releases (filter (fn [ref] (nil? (:isMasterRef ref))) refs)
          ref (if (nil? r) (:ref master) r)
          doc-resolver (fn [doc] (str "/" (:type doc) "/" (:id doc) "/" (or (:slug doc) (-> doc :slugs first)) "?ref=" ref))
          link-resolver (fn [link] (doc-resolver (-> link :value :document)))]
      (action {:api api
               :ref ref
               :master master
               :releases releases
               :is-ref-is-master (= ref (:ref master))
               :resolver {:document doc-resolver :link link-resolver}
               }))
    (catch clojure.lang.ExceptionInfo e
      (let [data (:object (ex-data e))]
        (if (not (= (:type data) "UnexpectedError"))
          (do (clear-token-cookie) (redirect "/signin")) (throw e))))))

(defroutes app-routes
  (GET "/" [page ref] (with-ctx ref (fn [ctx] (home (parse-page page) ctx))))
  (GET "/search" [query page ref] (with-ctx ref (fn[ctx] (search query (parse-page page) ctx))))
  (GET "/:typ/:id/:slug" [typ id slug ref] (with-ctx ref (fn[ctx] (doc id slug ctx))))
  (GET "/signin" [:as {headers :headers}]
       (let [referer (get headers "referer")
             host (get headers "host")]
         (signin host referer)))
  (GET "/signout" [] (signout))
  (GET "/oauth_callback" [code redirect_uri] (oauth-callback code redirect_uri)))
