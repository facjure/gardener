(ns gardener.browser.history)

(def history
  (atom [@app-state]))

(add-watch
 app-state :history
 (fn [_ _ old new]
   (println old new history)
   (when-not (= (last @history) new)
     (swap! history conj new))))

(defn undo! []
  (when (> (count @history) 1)
    (swap! history pop)
    (reset! app-state (last @history))))

