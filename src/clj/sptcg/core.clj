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
   [compojure.core :refer [defroutes GET POST context]]
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
    (cond
     (nil? name) "Please supply your name"
     (nil? pwd) "Please supply a password"
     (nil? email) "Please supply your email address"
     existing-player "Player already exists!"
     :else
     {:body (str email " registered.")
      :dbtx [player]})))

;;(def dburi "datomic:free://localhost:4334/sptcg")
(def dburi "datomic:mem://sptcg")
;; This is a convenience function for REPL use only...

(defn db [] (d/db (d/connect dburi)))

(defn wrap-tx [handler]
  (fn [req]
    (let [conn (d/connect dburi)
          db (d/db conn)
          response (handler (assoc req :db db))]
      (println "Response at wrap-tx:" response)
      (if (:dbtx response)
        (d/transact conn (:dbtx response)))
      response)))

(defmulti socket-event (fn [{event :event}] (first event)))

(defmethod socket-event :chsk/uidport-open [event] (println "> Socket open!"))
(defmethod socket-event :chsk/state [event] (println "> Socket state!"))
(defmethod socket-event :chsk/ping [event] (println "> Socket ping!"))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn))

(defn wrap-chsk-send [handler]
  (fn [req]
    (let [{:keys [websocket-send] :as response} (handler req)]
      (doseq [[uid msg] websocket-send]
        (chsk-send! uid msg))
      response)))

(def event-app
  (->
   (fn [r] (socket-event r))
   wrap-chsk-send
   wrap-tx))

(defn event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (let [{:keys [websocket-response] :as response} (event-app (assoc ring-req :event event))]
    (doseq [m websocket-response]
      (?reply-fn m))
    response))

;; Will start a core.async go loop to handle `event-msg`s as they come in:
(sente/start-chsk-router-loop! event-msg-handler ch-chsk)


(defroutes txroutes
  (GET "/register" [] register))

(def txapp
  (-> txroutes
      wrap-chsk-send
      wrap-tx))

(defroutes routes
  (GET "/" [] index)
  (context "/tx" [] #'txapp)
  (GET  "/chsk" [] #'ring-ajax-get-or-ws-handshake)
  (POST "/chsk" [] #'ring-ajax-post)
  (route/files "/" {:root "resources/public"}))




(def app
  (-> routes
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
