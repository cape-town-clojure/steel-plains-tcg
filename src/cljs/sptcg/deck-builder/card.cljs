(ns sptcg.deck-builder.card
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop alt!)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.card-schema :as card-schema]
            [sptcg.model :as model]))

(defn mana [{:keys [amount colour]} owner]
  (reify
    om/IDisplayName (display-name [_] "Mana")
    om/IRenderState
    (render-state [_ state]
      (html
       [:span
        (if colour
          (for [i (range amount)]
            [:span {:class colour} "@"])
          [:span amount])]))))

(defn manas [data owner]
  (reify
    om/IDisplayName (display-name [_] "Manas")
    om/IRenderState
    (render-state [_ state]
      (html
       [:span.mana
        (if (set? data)
          (om/build-all mana (card-schema/sort-manas data))
          (om/build mana data))]))))

(defn land [{:keys [size sub-type produces]} owner]
  (reify
    om/IDisplayName (display-name [_] "LandCard")
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.land
        [:p (card-schema/card-type-label :land sub-type)]
        [:hr]
        [:p "Size: " size]
        (when produces
          [:p "Produces: " (om/build manas produces)])]))))

(defn spell [{:keys [type cost sub-type produces]} owner]
  (reify
    om/IDisplayName (display-name [_] "SpellCard")
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.spell
        [:p (card-schema/card-type-label type sub-type)]
        [:hr]
        [:p "Cost: " (om/build manas cost)]
        (when produces
          [:p "Produces: " (om/build manas produces)])]))))

(defn card [{:keys [selected? name type] :as data} owner {:keys [class action-label]}]
  (reify
    om/IDisplayName (display-name [_] "Card")
    om/IRenderState
    (render-state [_ {:keys [control-chan list-chan]}]
      (html
       [:div.card
        (cond-> {:onClick #(put! list-chan [:select-card data])}
                selected? (update-in [:class] #(str % " selected"))
                class     (update-in [:class] #(str % " " class)))
        [:h3 name]
        (om/build (if (= :land type) land spell) data)
        (when selected?
          [:button.pure-button {:onClick #(put! control-chan [:action-card data])}
           (or action-label "Use")])]))))

(defn deck-card [{card* :card amount :amount :as data} owner opts]
  (reify
    om/IDisplayName (display-name [_] "DeckCard")
    om/IRenderState
    (render-state [_ {:keys [control-chan list-chan]}]
      (html
       [:div.deck-card
        (for [i (range amount)]
          (om/build card card* {:init-state {:control-chan control-chan
                                             :list-chan list-chan}
                                :opts (assoc opts :class (str "card" i))}))]))))

(defn card-list [data owner {:keys [item-component display-name]} opts]
  (reify
    om/IDisplayName (display-name [_] (str display-name "CardList"))
    om/IInitState   (init-state   [_] {:list-chan (chan)
                                       :selected-card nil})
    om/IWillMount
    (will-mount [_]
      (let [list-chan (om/get-state owner :list-chan)]
        (go (while true
              (when-let [[op value] (<! list-chan)]
                (prn value (om/get-state owner :selected-card)
                     (= value (om/get-state owner :selected-card)))
                (condp = op
                  :select-card
                  (om/set-state! owner :selected-card
                                 (if (= value (om/get-state owner :selected-card))
                                   nil value))))))))
    om/IRenderState
    (render-state [_ {:keys [control-chan list-chan selected-card]}]
      (html
       [:div.card-list
        (if (seq data)
          (om/build-all item-component data
                        {:fn (fn [card]
                               (cond-> card
                                       (= card selected-card) (assoc :selected? true)))
                         :init-state {:control-chan control-chan
                                      :list-chan list-chan
                                      :selected selected-card}
                         :opts opts})
          "No cards.")]))))

