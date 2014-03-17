(ns sptcg.core
  (:require
   [clojure.core.async :as async :refer (<! <!! >! >!! put! chan go go-loop)]
   [ring.util.response :refer [file-response]]
   [ring.middleware.edn :refer [wrap-edn-params]]
   [ring.middleware.keyword-params :as keyword-params]
   [ring.middleware.nested-params :as nested-params]
   [ring.middleware.params :as params]
   [ring.middleware.anti-forgery :as ring-anti-forgery]
   [ring.middleware.session :as session]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [compojure.handler :as handler]
   [datomic.api :as d]
   [taoensso.sente :as sente]
   [org.httpkit.server :as hk]
   [clojure.tools.nrepl.server :as nrepl]
   [sptcg.util :as util]))

(defn index [{db :db :as req}]
  (assoc (file-response "public/html/index.html" {:root "resources"})
    :session (assoc (:session req) :uid (java.util.UUID/randomUUID))))

(defn register [{db :db {:keys [name pwd email]} :params :as req}]
  (let [existing-player (first (d/q '[:find ?e :in $ ?email :where [?e :player/email ?email]] db email))
        player {:db/id (d/tempid :db.part/user) :player/name name :player/pwd pwd :player/email email}]
    (if existing-player
      {:body "Player already exists!"}
      {:body (str email " registered.")
       :dbtx [player]})))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn))

(defn- event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (let [session (:session ring-req)
        uid     (:uid session)
        [id data :as ev] event]
    (?reply-fn {:app/login :done})))

;; Will start a core.async go loop to handle `event-msg`s as they come in:
(sente/start-chsk-router-loop! event-msg-handler ch-chsk)

(defroutes routes
  (GET "/" [] index)
  (GET "/register" [] register)
  (GET  "/chsk" [] #'ring-ajax-get-or-ws-handshake)
  (POST "/chsk" [] #'ring-ajax-post)
  (route/files "/" {:root "resources/public"}))

;;(def dburi "datomic:free://localhost:4334/sptcg")
(def dburi "datomic:mem://sptcg")
;; This is a convenience function for REPL use only...

(defn db [] (d/db (d/connect dburi)))

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
      keyword-params/wrap-keyword-params
      nested-params/wrap-nested-params
      params/wrap-params
      session/wrap-session
      (ring-anti-forgery/wrap-anti-forgery
       {:read-token (fn [req] (-> req :params :csrf-token))})
      wrap-edn-params))

(def server (atom nil))

(defn -main [& args]
  (println "Starting REPL at 127.0.0.1 port 4005")
  (nrepl/start-server :port 4005 :bind "127.0.0.1")
  (println "Starting Datomic at " dburi)
  (util/init-db dburi)
  (println "Starting Http-kit http://localhost:8080")
  (reset! server (hk/run-server #'app {:port 8080}))
  (println "Started Server."))
