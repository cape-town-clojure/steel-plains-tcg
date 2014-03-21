(ns sptcg.components.inspector
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [ankha.core :as ankha]
            [cljs.reader :as reader]
            [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.components.draggable-window :as draggable-window]
            [sptcg.components.key-queue :as key-queue]))

(defn change-path!
  [data controls-ch]
  (let [inspector-path   (get-in @data [:settings :inspector :path])
        inspector-path-s (pr-str inspector-path)
        path-string      (js/prompt "New path (must be edn-compatible)"
                                    inspector-path-s)]
    (try
      (put! controls-ch [:inspector-path-updated (reader/read-string path-string)])
      (catch js/Error e
        (print "Not edn-compatible: " path-string)))))

(defn toggle!
  [data controls-ch]
  (put! controls-ch [:toggle-inspector-key-pressed]))

(defn inspector [data owner]
  (reify
    om/IDisplayName (display-name [_] "Inspector")
    om/IRenderState
    (render-state [_ state]
      (let [controls-ch (get-in data [:comms :controls])]
        (html
         [:div.inspector
          (om/build key-queue/KeyboardHandler data
                    {:opts {:keymap (atom {"ctrl+esc" #(toggle! data controls-ch)
                                           "ctrl+1"   #(change-path! data controls-ch)})
                            :error-ch (get-in data [:comms :error])}})
          (when (get-in data [:windows :window-inspector :open])
            (when-let [path (get-in data [:settings :inspector :path])]
              (om/build draggable-window/draggable-window
                        {:title        (str "Data Inspector: " (pr-str path))
                         :name         :window-inspector
                         :window       (get-in data [:windows :window-inspector])
                         :comm         (get-in data [:comms :controls])
                         :content-com  ankha/inspector
                         :content-data (get-in data path)
                         :content-opts {}})))])))))

