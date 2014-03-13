(ns sptcg.core
  (:require [ring.util.response :refer [file-response]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [compojure.core :refer [defroutes GET PUT]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [datomic.api :as d]
            [taoensso.sente :as sente]
            [org.httpkit.server :as hk]))

(def uri "datomic:free://localhost:4334/sptcg")
(def conn (d/connect uri))

(defn index []
  (file-response "public/html/index.html" {:root "resources"}))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn))

(defroutes routes
  (GET "/" [] (index))
  (GET  "/chsk" req (#'ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (#'ring-ajax-post                req))
  (route/files "/" {:root "resources/public"}))

(def app
  (-> routes
      wrap-edn-params))

(defonce server
  (hk/run-server app {:port 8080}))
