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


(defn prevent-browser-back!
  "Disable the back button: user doesn't navigate away from page"
  []
  (.pushState js/history nil nil (.-pathname (.-location js/window))))

(defn alert-after-attempted-refresh! []
  (set! (.-onbeforeunload js/window) (fn [_] true)))

(defn popstate-mod! []
  (set! (.-onpopstate js/window)
        (fn [_] (prevent-browser-back!))))

(defn scroll-to-elem
  "Enable the page to scroll to a element with a `target-id`"
  [target-id]
  (let [elem (.getElementById js/document target-id)]
    (.scrollIntoView elem #js{:behavior "smooth"})))

