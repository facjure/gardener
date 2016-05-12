(ns gardener.browser.keyboard
  (:require-macros
    [cljs.core.async.macros :refer [go]]
    [clojure.core.match :refer [match]])
  (:require
    [goog.userAgent :as ua]
    [goog.events :as events]
    [goog.events.EventType]
    [clojure.string :as string]
    [cljs.core.match]
    [cljs.core.async :refer [>! <! alts! chan sliding-buffer put! to-chan]]
    [facjure.ui.dom :as dom]
    [facjure.ui.utils :as h]
    [facjure.ui.reactive :as r]))

(def BACKSPACE 8)
(def ENTER 13)
(def ESC 27)
(def TAB 9)

(def UP_ARROW 38)
(def DOWN_ARROW 40)

(defn key-event->keycode [e]
  (.-keyCode e))
