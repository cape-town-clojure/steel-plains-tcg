(ns sptcg.sente
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [taoensso.sente :as sente :refer (cb-success?)]))

(declare chsk-send!)

(defn- event-handler [[id data :as ev] _]
  (println "<!" id)
  (chsk-send! [id data] 5000 #(println "Hello" %&))
  ;; Commented out for now till we have the event handling sorted.
  #_(match [id data]

   ;; An event from `ch-ui` that our UI has generated:
   [:on.keypress/div#msg-input _] (do-something!)

   ;; A channel socket event pushed from our server:
   [:chsk/recv [:my-app/alert-from-server payload]]
   (do (logf "Pushed payload received from server!: %s" payload)
       (do-something! payload))

   [:chsk/state [:first-open _]] (logf "Channel socket successfully established!")

   [:chsk/state new-state] (logf "Chsk state change: %s" new-state)
   [:chsk/recv  payload]   (logf "From server: %s"       payload)
   :else (logf "Unmatched <!: %s" id)))

(defn start! []
  (let [{:keys [chsk ch-recv send-fn]}
        (sente/make-channel-socket! "/chsk" {} {:type :auto})]
    (def chsk       chsk)
    (def ch-chsk    ch-recv)
    (def chsk-send! send-fn))

  (let [ch-chsk   ch-chsk ; Chsk events (incl. async events from server)
        ch-ui     (chan) ; Channel for your own UI events, etc. (optional)
        ch-merged (async/merge [ch-chsk ch-ui])]

    ;; Will start a core.async go loop to handle `event`s as they come in:

    (sente/start-chsk-router-loop! event-handler ch-merged)))
