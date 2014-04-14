(ns prismic-starter.ctx
  (:require [noir.cookies :as cookies]
            [prismic-starter.config :as config]))

(defn get-access-token []
  (let[from-cookie (cookies/get :access_token)
       from-config (config/token)]
    (if (nil? from-cookie) from-config from-cookie)))

(defn has-privileged-access []
  (not (nil? (get-access-token))))
