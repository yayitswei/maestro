(ns user
  (:require [maestro.core :as maestro])
  (:use [serial-port]))

;;(def portname "/dev/cu.usbmodemfa131")
;;(def portname "/dev/ttyACM0")
(def portname "/dev/cu.usbmodemfd121")

(defn start! [] (maestro/start! portname))

(defn stop! [] (maestro/stop!))
