(ns sptcg.components.draggable-window
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [clojure.string :as string]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :as html :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:import [goog.events EventType]))

(defmulti window-drag-event
  (fn [message args state] message))

(defmethod window-drag-event :grabbed
  [message initial-mouse-pos window-state]
  (let [[mx my] initial-mouse-pos
        [px py] (:position window-state)
        offset [(- mx px) (- my py)]]
    (assoc window-state
      :dragging? true
      :offset offset)))

(defmethod window-drag-event :released
  [message data window-state]
  (assoc window-state :dragging? false))

(defmethod window-drag-event :mouse-moved
  [message mouse-position window-state]
  (if (:dragging? window-state)
    (let [[mx my] mouse-position
          [off-x off-y] (:offset window-state)
          [tnx tny] [(- mx off-x) (- my off-y)]
          min-x -150
          max-x (- (.-clientWidth js/document.body) 50)
          min-y 0
          max-y (- (.-clientHeight js/document.body) 50)
          new-position [(cond
                         (> min-x tnx) min-x
                         (> tnx max-x) max-x
                         :else tnx)
                        (cond
                         (> min-y tny) min-y
                         (> tny max-y) max-y
                         :else tny)]]
      (assoc window-state :position new-position))
    window-state))

(def local-dragging? (atom false))

(defn listen [el type]
  (let [out (chan)]
    (events/listen el type (fn [event]
                             (when @local-dragging?
                               (.preventDefault event))
                             (put! out event)))
    out))

(defn vectorise-client-coords
  [obj]
  [(.-clientX obj) (.-clientY obj)])

(defn draggable-window [data owner opts]
  (reify
    om/IDisplayName (display-name [_] "DraggableWindow")
    om/IWillMount
    (will-mount [_]
      (let [mouse-move-chan (async/map vectorise-client-coords
                                       [(listen js/window "mousemove")])
            mouse-up-chan (async/map vectorise-client-coords
                                     [(listen js/window "mouseup")])
            comm (:comm data)
            name (:name data)]
        (go (while true
              (alt!
                mouse-move-chan ([position]
                                   (put! comm [:draggable [:mouse-moved {:name name
                                                                         :position position}]]))
                mouse-up-chan ([position]
                                 (put! comm [:draggable [:released {:name name
                                                                    :position position}]])
                                 (reset! local-dragging? false)))))))
    om/IRenderState
    (render-state [_ {:keys [draggable-chan]}]
      (html/html
       (let [{:keys [comm name window]} data]
         [:div.draggable-window
          {:style (when-let [pos (:position window)]
                    #js {:position "fixed"
                         :top (last pos)
                         :left (first pos)})}
          [:div.row.modal-header
           [:div.col-lg-12
            (merge {:style (clj->js
                            (merge {:background-color (if (:dragging? window)
                                                        "#050"
                                                        "#500")
                                    :color "white"
                                    :text-align "center"}))
                    :onMouseDown #(do
                                    (reset! local-dragging? true)
                                    (put! comm [:draggable [:grabbed {:name name
                                                                      :position (vectorise-client-coords %)}]]))})
            (:title data)]]
          [:div.row
           (om/build (:content-com data) (:content-data data) (:content-opts data))]])))))
