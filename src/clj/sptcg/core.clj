(ns sptcg.core
  (:require
   [ring.util.response :refer [file-response]]
   [ring.middleware.edn :refer [wrap-edn-params]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [compojure.handler :as handler]
   [datomic.api :as d]
   [taoensso.sente :as sente]
   [org.httpkit.server :as hk]
   [clojure.tools.nrepl.server :as nrepl]
   [sptcg.util :as util]))

(defn index [{db :db}]
  (file-response "public/html/index.html" {:root "resources"}))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn))

(defroutes routes
  (GET "/" [] index)
  (GET  "/chsk" [] #'ring-ajax-get-or-ws-handshake)
  (POST "/chsk" [] #'ring-ajax-post)
  (route/files "/" {:root "resources/public"}))

;;(def dburi "datomic:free://localhost:4334/sptcg")
(def dburi "datomic:mem://sptcg")

(defn wrap-tx [handler]
  (fn [req]
    (let [conn (d/connect dburi)
          db (d/db conn)
          response (handler (assoc req :db db))]
      (if (:dbtx response)
        (d/transact conn (:dbtx response)))
      response)))

(def app
  (-> routes
      wrap-tx
      wrap-edn-params))

(def server (atom nil))

(defn -main [& args]
  (println "Starting REPL at 127.0.0.1 port 4005")
  (nrepl/start-server :port 4005 :bind "127.0.0.1")
  (println "Starting Datomic at " dburi)
  (util/init-db dburi)
  (println "Starting Http-kit http://localhost:8080")
  (reset! server (hk/run-server app {:port 8080}))
  (println "Started Server."))
