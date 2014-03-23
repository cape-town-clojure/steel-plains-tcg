(ns sptcg.deck-builder.collection
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop alt!)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.card-schema :as card-schema]
            [sptcg.deck-builder.enum-toggle-buttons :as enum-toggle-buttons]
            [sptcg.deck-builder.card :as card]
            [sptcg.model :as model]))

(defn collection [data owner]
  (reify
    om/IDisplayName (display-name [_] "Collection")
    om/IInitState   (init-state   [_] {:collection-chan (chan)
                                       :selected-card-type :all
                                       :selected-colour :all})
    om/IWillMount
    (will-mount [_]
      (let [control-chan (om/get-state owner :control-chan)
            collection-chan (om/get-state owner :collection-chan)]
        (go (while true
              (when-let [[op value] (<! collection-chan)]
                (condp = op
                  :select-card-type (om/set-state! owner :selected-card-type value)
                  :select-colour    (om/set-state! owner :selected-colour value)
                  :action-card      (put! control-chan [:use-collection-card value])))))))
    om/IRenderState
    (render-state [_ {:keys [collection-chan
                             selected-card-type
                             selected-colour]}]
      (html
       [:div.collection
        [:h2 "Collection"]
        (om/build enum-toggle-buttons/enum-toggle-buttons
                  {:enums card-schema/card-types
                   :selected selected-card-type}
                  {:init-state {:select-chan collection-chan}
                   :opts {:op :select-card-type}})
        (om/build enum-toggle-buttons/enum-toggle-buttons
                  {:enums card-schema/colours
                   :selected selected-colour}
                  {:init-state {:select-chan collection-chan}
                   :opts {:op :select-colour}})
        [:hr]
        (om/build card/card-list
                  (->> data
                       (card-schema/filter-by-type selected-card-type)
                       (card-schema/filter-by-colour selected-colour))
                  {:init-state {:control-chan collection-chan}
                   :opts {:item-component card/card
                          :display-name "Collection"}})]))))

