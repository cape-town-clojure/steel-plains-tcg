(ns sptcg.deck-builder.enum-toggle-buttons
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop alt!)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.card-schema :as card-schema]
            [sptcg.model :as model]))

(defn enum-toggle-button [{:keys [enum selected?]} owner {:keys [op]}]
  (reify
    om/IDisplayName (display-name [_] "EnumToggleButton")
    om/IRenderState
    (render-state [_ {:keys [select-chan]}]
      (html
       [:button {:class (if selected?
                          "pure-button pure-button-active"
                          "pure-button")
                 :onClick #(put! select-chan [op enum])}
        [:small (card-schema/labelise-enum enum)]]))))

(defn enum-toggle-buttons [{:keys [enums selected]} owner {:keys [op]}]
  (reify
    om/IDisplayName (display-name [_] "EnumToggleButtons")
    om/IRenderState
    (render-state [_ {:keys [select-chan]}]
      (html
       [:div.emum-toggles
        (om/build-all enum-toggle-button (card-schema/sorted-enum-list enums)
                      {:fn (fn [enum]
                             (cond-> {:enum enum}
                                     (= enum selected) (assoc :selected? true)))
                       :init-state {:select-chan select-chan}
                       :opts {:op op}})]))))
