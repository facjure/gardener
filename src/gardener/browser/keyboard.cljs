(ns gardener.browser.keyboard
  (:require
    [goog.userAgent :as ua]
    [goog.events :as events]
    [goog.events.EventType]
    [clojure.string :as string]))

(def BACKSPACE 8)
(def ENTER 13)
(def ESC 27)
(def TAB 9)

(def UP_ARROW 38)
(def DOWN_ARROW 40)

(defn key-event->keycode [e]
  (.-keyCode e))
