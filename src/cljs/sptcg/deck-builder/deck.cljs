(ns sptcg.deck-builder.deck
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop alt!)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.card-schema :as card-schema]
            [sptcg.deck-builder.card :as card]
            [sptcg.model :as model]))

(defn deck [{:keys [land spell] :as data} owner]
  (reify
    om/IDisplayName (display-name [_] "Deck")
    om/IRenderState
    (render-state [_ {:keys [control-chan]}]
      (html
       [:div.pure-g
        [:div.pure-u-1-2
         [:div.deck
          [:h2 (card-schema/labelise-enum :land) " Deck"]
          [:p (card-schema/deck-card-count-label :land land)]
          [:hr]
          (om/build card/card-list land
                    {:init-state {:control-chan control-chan}
                     :opts {:item-component card/deck-card
                            :display-name "LandDeck"}})]]
        [:div.pure-u-1-2
         [:div.deck
          [:h2 (card-schema/labelise-enum :spell) " Deck"]
          [:p (card-schema/deck-card-count-label :spell spell)]
          [:hr]
          (om/build card/card-list spell
                    {:init-state {:control-chan control-chan}
                     :opts {:item-component card/deck-card
                            :display-name "SpellDeck"}})]]]))))
