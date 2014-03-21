(ns sptcg.deck-builder
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop alt!)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.components.editor :as editor]
            [sptcg.components.inspector :as inspector]
            [sptcg.components.draggable-window :as draggable-window]
            [sptcg.model :as model]))

(def state (atom {:deck-builder {:cardbase model/cardbase}
                  :comms {:controls (chan)
                          :error (chan)}
                  :settings {:inspector {:path [:deck-builder]}}
                  :windows {:window-inspector {:open false}}}))

(defn deck-builder [data owner]
  (reify
    om/IDisplayName (display-name [_] "DeckBuilder")
    om/IRenderState
    (render-state [_ state]
      (html
       [:div "Deck builder"
        (om/build inspector/inspector data)]
       ))))

(defmulti control-event
  (fn [target message args state] message))

(defmethod control-event :toggle-inspector-key-pressed
  [target message args state]
  (update-in state [:windows :window-inspector :open] not))

(defmethod control-event :inspector-path-updated
  [target message path state]
  (assoc-in state [:settings :inspector :path] path))

(defmethod control-event :draggable
  [target message [sub-message {:keys [name] :as args}] state]
  (update-in state [:windows name]
             #(draggable-window/window-drag-event sub-message (:position args) %)))

(defn instrument
  [f cursor m]
  (if (= f deck-builder)
    (om/build* editor/editor (om/graft [f cursor m] cursor))
    ::om/pass))
(defn start!
  []
  (let [comms (:comms @state)
        target (.getElementById js/document "deck-builder")]
    (om/root deck-builder state {:target target})
    (go (while true
          (alt!
            (:controls comms)
            ([[message args]]
               (swap! state (partial control-event target message args)))
            (:error comms)
            ([[message args]]
               (print "ERROR" message args)))))))
