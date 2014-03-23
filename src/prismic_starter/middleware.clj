(ns prismic-starter.middleware
  (:require [taoensso.timbre :as timbre]
            [environ.core :refer [env]]))

(defn log-request [handler]
  (if (env :dev)
    (fn [req]
      (timbre/debug req)
      (handler req))
    handler))
