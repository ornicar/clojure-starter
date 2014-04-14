(ns prismic-starter.config)

(defn- check-config[key value]
  (if (nil? value) (throw (ex-info (str "Missing configuration key: " key) {})) value))

(defn- load-config[]
  (with-open [r (clojure.java.io/reader "config.clj")]
    (read (java.io.PushbackReader. r))))

(def config
  (load-config))

(defn api-endpoint[]
  (check-config "api-endpoint" (:api-endpoint config)))

(defn client-id[]
  (check-config "client-id" (:client-id config)))

(defn client-secret[]
  (check-config "client-secret" (:client-secret config)))

(defn token[]
  (:token config))
