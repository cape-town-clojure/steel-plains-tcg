(ns sptcg.deck-builder.deck
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop alt!)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.card-schema :as card-schema]
            [sptcg.deck-builder.card :as card]
            [sptcg.deck-builder.enum-toggle-buttons :as enum-toggle-buttons]
            [sptcg.model :as model]))

(defn deck [{:keys [type cards] :as data} owner]
  (reify
    om/IDisplayName (display-name [_] "Deck")
    om/IInitState   (init-state   [_] {:select-chan (chan)
                                       :selected-card nil})
    om/IWillMount
    (will-mount [_]
      (let [deck-chan (om/get-state owner :deck-chan)]
        (go (while true
              (when-let [value (<! deck-chan)]
                (cond
                 (= :remove-card value) (om/transact! cards #(disj % value))
                 :otherwise            (om/transact! cards #(conj % value)))))))
      (let [control-chan (om/get-state owner :control-chan)
            select-chan (om/get-state owner :select-chan)]
        (go (while true
              (when-let [[op value] (<! select-chan)]
                (condp = op
                  :select-card (put! control-chan [:selected-deck-card [type value]])))))))
    om/IRenderState
    (render-state [_ {:keys [select-chan
                             selected-card]}]
      (html
       [:div.pure-u-1-4
        [:div.deck
         [:h2 (card-schema/labelise-enum type) " Deck"]
         [:p (card-schema/deck-card-count-label type cards)]
         [:hr]
         (om/build card/card-list {:cards cards
                                   :selected selected-card}
                   {:init-state {:select-chan select-chan}
                    :opts {:op :select-card}})]]
       [:div.pure-u-1-4
        [:div.deck
         [:h2 (card-schema/labelise-enum type) " Deck"]
         [:p (card-schema/deck-card-count-label type cards)]
         [:hr]
         (om/build card/card-list {:cards cards
                                   :selected selected-card}
                   {:init-state {:select-chan select-chan}
                    :opts {:op :select-card}})]]))))

