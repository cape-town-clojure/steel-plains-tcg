(ns sptcg.components.debug
  (:require [ankha.core :as ankha]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))

(defn debug [data owner]
  (reify
    om/IDisplayName (display-name [_] "Debug")
    om/IInitState
    (init-state [_]
      {:visible? false})
    om/IRenderState
    (render-state [_ {:keys [visible?]}]
      (html
       [:div
        [:button
         {:onClick #(om/update-state! owner :visible? not)}
         (if visible? "Hide" "Show State")]
        (when visible?
          (om/build ankha/inspector data))]))))
