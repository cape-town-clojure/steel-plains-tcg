(ns sptcg.deck-builder.components
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

(defn card [{:keys [selected? name type] :as data} owner {:keys [op]}]
  (reify
    om/IDisplayName (display-name [_] "Card")
    om/IRenderState
    (render-state [_ {:keys [select-chan]}]
      (html
       [:div.card
        (cond-> {}
                selected? (assoc :class "selected")
                select-chan (assoc :onClick #(put! select-chan [op (if selected? nil data)])))
        [:h3 name]
        (om/build (if (= :land type)
                    land spell)
                  data)]))))

(defn card-list [{:keys [cards selected]} owner {:keys [op]}]
  (reify
    om/IDisplayName (display-name [_] "CardList")
    om/IRenderState
    (render-state [_ {:keys [select-chan]}]
      (html
       [:div.card-list
        (if (seq cards)
          (om/build-all card cards
                        {:fn (fn [card]
                               (cond-> card
                                       (= card selected) (assoc :selected? true)))
                         :init-state {:select-chan select-chan}
                         :opts {:op op}})
          "No cards.")]))))

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

(defn collection [data owner]
  (reify
    om/IDisplayName (display-name [_] "Collection")
    om/IInitState   (init-state   [_] {:select-chan (chan)
                                       :selected-card-type :all
                                       :selected-colour :all
                                       :selected-card nil})
    om/IWillMount
    (will-mount [_]
      (let [select-chan (om/get-state owner :select-chan)]
        (go (while true
              (when-let [[key value] (<! select-chan)]
                (om/set-state! owner key value))))))
    om/IRenderState
    (render-state [_ {:keys [add-to-deck-chan
                             select-chan
                             selected-card-type
                             selected-colour
                             selected-card]}]
      (html
       [:div.collection
        [:h2 "Collection"]
        (om/build enum-toggle-buttons
                  {:enums card-schema/card-types
                   :selected selected-card-type}
                  {:init-state {:select-chan select-chan}
                   :opts {:op :selected-card-type}})
        (om/build enum-toggle-buttons
                  {:enums card-schema/colours
                   :selected selected-colour}
                  {:init-state {:select-chan select-chan}
                   :opts {:op :selected-colour}})
        [:hr]
        [:button.pure-button.button-xlarge
         {:onClick #(put! add-to-deck-chan selected-card)
          :disabled (not selected-card)}
         "Add to Deck"]
        [:hr]
        (om/build card-list {:cards (->> data
                                         (card-schema/filter-by-type selected-card-type)
                                         (card-schema/filter-by-colour selected-colour))
                             :selected selected-card}
                  {:init-state {:select-chan select-chan}
                   :opts {:op :selected-card}})]))))

(defn deck [{:keys [type cards] :as data} owner]
  (reify
    om/IDisplayName (display-name [_] "Deck")
    om/IInitState   (init-state   [_] {:select-chan (chan)
                                       :selected-card nil})
    om/IWillMount
    (will-mount [_]
      (let [select-chan (om/get-state owner :select-chan)]
        (go (while true
              (when-let [[key value] (<! select-chan)]
                (prn key value)
                (om/set-state! owner key value)))))
      (let [add-to-deck-chan (om/get-state owner :add-to-deck-chan)]
        (go (while true
              (when-let [card (<! add-to-deck-chan)]
                (prn "add to deck" type (:name card))
                (when (or (= :land type (:type card))
                          (and (not= :land type)
                               (not= :land (:type card))))
                  (om/transact! cards #(conj % card))))))))
    om/IRenderState
    (render-state [_ {:keys [select-chan
                             selected-card]}]
      (html
       [:div.deck
        [:h2 (card-schema/labelise-enum type) " Deck"]
        [:p (card-schema/deck-card-count-label type cards)]
        #_(
         [:hr]
         [:button.pure-button.button-xlarge
          {:onClick #(do
                       (put! select-chan [:selected-card nil])
                       (om/transact! cards (fn [cards] (disj cards card))))
           :disabled (not selected-card)}
          "Remove from Deck"])
        [:hr]
        (om/build card-list {:cards cards})
        #_(om/build card-list {:cards cards
                             :selected selected-card}
                  {:init-state {:select-chan select-chan}
                   :opts {:op :selected-card}})]))))
