(ns sptcg.deck-builder.collection
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop alt!)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.card-schema :as card-schema]
            [sptcg.deck-builder.enum-toggle-buttons :as enum-toggle-buttons]
            [sptcg.deck-builder.card :as card]
            [sptcg.model :as model]))

(defn collection [{:keys [selected-card-type selected-colour cards] :as data} owner]
  (reify
    om/IDisplayName (display-name [_] "Collection")
    om/IInitState   (init-state   [_] {:collection-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (let [control-chan (om/get-state owner :control-chan)
            collection-chan (om/get-state owner :collection-chan)]
        (go (while true
              (when-let [[op value] (<! collection-chan)]
                (condp = op
                  :select-card-type (om/update! data :selected-card-type value)
                  :select-colour    (om/update! data :selected-colour value)
                  :action-card      (put! control-chan [:use-collection-card value])))))))
    om/IRenderState
    (render-state [_ {:keys [collection-chan]}]
      (html
       [:div.collection
        [:h2 "Collection"]
        (om/build enum-toggle-buttons/enum-toggle-buttons
                  card-schema/card-types
                  {:init-state {:select-chan collection-chan}
                   :state {:selected selected-card-type}
                   :opts {:op :select-card-type}})
        (om/build enum-toggle-buttons/enum-toggle-buttons
                  card-schema/colours
                  {:init-state {:select-chan collection-chan}
                   :state {:selected selected-colour}
                   :opts {:op :select-colour}})
        [:hr]
        (om/build card/card-list (->> cards
                                      (filter (partial card-schema/matches-type? selected-card-type))
                                      (filter (partial card-schema/matches-colour? selected-colour)))
                  {:init-state {:control-chan collection-chan}
                   :opts {:item-component card/card
                          :display-name "Collection"
                          :action-label "add to deck"}})]))))

